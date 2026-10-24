param(
    [ValidateSet('baseline', 'hpa')]
    [string]$Experiment = 'hpa',
    [string]$Profile = 'spring-microservices',
    [string]$Namespace = 'spring-microservices'
)

$ErrorActionPreference = 'Stop'
$MicroservicesRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$KubernetesRoot = Join-Path $MicroservicesRoot 'kubernetes'
$BuildScript = Join-Path $PSScriptRoot 'build-images.ps1'

foreach ($command in @('docker', 'kubectl', 'minikube')) {
    if (-not (Get-Command $command -ErrorAction SilentlyContinue)) {
        throw "Required command '$command' was not found. Install Docker Desktop, kubectl, and Minikube first."
    }
}

docker info --format '{{.ServerVersion}}' *> $null
if ($LASTEXITCODE -ne 0) {
    throw 'Docker is not available. Start Docker Desktop and retry.'
}

$minikubeStatus = minikube status -p $Profile --format '{{.Host}}' 2>$null
if ($LASTEXITCODE -ne 0 -or $minikubeStatus -ne 'Running') {
    minikube start -p $Profile --driver=docker --cpus=4 --memory=6144 --addons=metrics-server
    if ($LASTEXITCODE -ne 0) { throw 'Minikube failed to start.' }
} else {
    minikube addons enable metrics-server -p $Profile
    if ($LASTEXITCODE -ne 0) { throw 'Could not enable the Metrics Server addon.' }
}

& $BuildScript -Profile $Profile
if ($LASTEXITCODE -ne 0) { throw 'Building or loading the application images failed.' }

kubectl create namespace $Namespace --dry-run=client -o yaml | kubectl apply -f -
if ($LASTEXITCODE -ne 0) { throw 'Could not create the lab namespace.' }

kubectl create secret generic database-credentials -n $Namespace `
    --from-literal=postgres-password=root `
    --from-literal=mysql-root-password=12345678 `
    --dry-run=client -o yaml | kubectl apply -f -
if ($LASTEXITCODE -ne 0) { throw 'Could not create the local database credentials secret.' }

kubectl apply -n $Namespace -f (Join-Path $KubernetesRoot 'base\databases.yaml')
if ($LASTEXITCODE -ne 0) { throw 'Could not apply the database resources.' }

foreach ($deployment in @('db-inventory', 'db-products', 'db-orders')) {
    kubectl rollout status "deployment/$deployment" -n $Namespace --timeout=180s
    if ($LASTEXITCODE -ne 0) { throw "Database deployment '$deployment' did not become ready." }
}

$overlay = if ($Experiment -eq 'hpa') { 'overlays\hpa' } else { 'base' }
if ($Experiment -eq 'baseline') {
    kubectl delete hpa product-service -n $Namespace --ignore-not-found=true
    kubectl delete job hpa-load-generator -n $Namespace --ignore-not-found=true
}
kubectl apply -k (Join-Path $KubernetesRoot $overlay)
if ($LASTEXITCODE -ne 0) { throw "Could not apply the '$Experiment' configuration." }

foreach ($deployment in @('discovery-service', 'inventory-service', 'order-service', 'product-service', 'gateway-service')) {
    kubectl rollout status "deployment/$deployment" -n $Namespace --timeout=240s
    if ($LASTEXITCODE -ne 0) { throw "Application deployment '$deployment' did not become ready." }
}

Write-Host "Lab '$Experiment' is ready in namespace '$Namespace'."
Write-Host 'Run .\kubernetes\scripts\access.ps1 in another terminal to open local tunnels.'
if ($Experiment -eq 'hpa') {
    Write-Host 'Run the repeatable workload with: .\kubernetes\scripts\run-load.ps1'
}
