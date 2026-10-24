param(
    [ValidateSet('baseline', 'hpa')]
    [string]$Experiment = 'hpa',
    [string]$Namespace = 'spring-microservices'
)

$ErrorActionPreference = 'Stop'
$MicroservicesRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$JobManifest = Join-Path $MicroservicesRoot 'kubernetes\load\job.yaml'

$hpa = kubectl get hpa product-service -n $Namespace --ignore-not-found -o name
if ($Experiment -eq 'hpa' -and -not $hpa) {
    throw "The product-service HPA is not active in namespace '$Namespace'. Run up.ps1 -Experiment hpa first."
}
if ($Experiment -eq 'baseline' -and $hpa) {
    throw "The product-service HPA is active in namespace '$Namespace'. Run up.ps1 -Experiment baseline first."
}

kubectl delete job hpa-load-generator -n $Namespace --ignore-not-found=true --wait=true
if ($LASTEXITCODE -ne 0) { throw 'Could not remove the previous load job.' }

kubectl apply -n $Namespace -f $JobManifest
if ($LASTEXITCODE -ne 0) { throw 'Could not start the k6 load job.' }

Write-Host 'The load job is running: 12 VUs warm up for 60 seconds, then sample for 180 seconds.'
kubectl wait --for=condition=complete job/hpa-load-generator -n $Namespace --timeout=360s
$jobResult = $LASTEXITCODE
kubectl logs job/hpa-load-generator -n $Namespace
if ($jobResult -ne 0) { throw 'The k6 load job did not complete successfully; inspect its logs above.' }
