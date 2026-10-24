param(
    [string]$Namespace = 'spring-microservices',
    [string]$Profile = 'spring-microservices',
    [switch]$DeleteCluster
)

$ErrorActionPreference = 'Stop'
kubectl delete namespace $Namespace --ignore-not-found=true --wait=true
if ($LASTEXITCODE -ne 0) { throw 'Could not remove the lab namespace.' }

if ($DeleteCluster) {
    minikube delete -p $Profile
    if ($LASTEXITCODE -ne 0) { throw 'Could not delete the Minikube cluster.' }
}
