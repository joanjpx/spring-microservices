param(
    [string]$Namespace = 'spring-microservices',
    [int]$GatewayLocalPort = 8081,
    [int]$EurekaLocalPort = 8762
)

$ErrorActionPreference = 'Stop'
$gatewayPortForward = '{0}:8080' -f $GatewayLocalPort
$eurekaPortForward = '{0}:8761' -f $EurekaLocalPort
$gateway = Start-Process kubectl -ArgumentList @('--context', 'spring-microservices', 'port-forward', '-n', $Namespace, 'service/gateway-service', $gatewayPortForward) -PassThru -WindowStyle Hidden
$eureka = Start-Process kubectl -ArgumentList @('--context', 'spring-microservices', 'port-forward', '-n', $Namespace, 'service/discovery-service', $eurekaPortForward) -PassThru -WindowStyle Hidden

Write-Host "Gateway: http://localhost:$GatewayLocalPort (PID $($gateway.Id))"
Write-Host "Eureka:  http://localhost:$EurekaLocalPort (PID $($eureka.Id))"
Write-Host 'Stop the tunnels with: Stop-Process -Id <PID> for each process.'
