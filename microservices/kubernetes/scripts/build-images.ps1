param(
    [string]$Profile = 'spring-microservices',
    [string]$ImagePrefix = 'spring-microservices',
    [string]$Tag = 'dev'
)

$ErrorActionPreference = 'Stop'
$MicroservicesRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$PreviousJavaHome = $env:JAVA_HOME
$PreviousMavenHome = $env:MAVEN_USER_HOME

if (-not $env:JAVA_HOME) {
    $jdk = Get-ChildItem 'C:\Program Files\Java' -Directory -ErrorAction SilentlyContinue |
        Where-Object { Test-Path (Join-Path $_.FullName 'bin\javac.exe') } |
        Sort-Object Name -Descending |
        Select-Object -First 1
    if (-not $jdk) { throw 'Set JAVA_HOME to a JDK 17 or newer before building.' }
    $env:JAVA_HOME = $jdk.FullName
}

$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:MAVEN_USER_HOME = Join-Path $MicroservicesRoot '.maven-cache'
$repo = (Join-Path $MicroservicesRoot '.maven-cache\repository').Replace('\', '/')

Push-Location (Join-Path $MicroservicesRoot 'discovery_server')
try {
    .\mvnw.cmd -f ..\pom.xml "-Dmaven.repo.local=$repo" -DskipTests package
    if ($LASTEXITCODE -ne 0) { throw 'Maven package failed.' }
} finally {
    Pop-Location
    $env:JAVA_HOME = $PreviousJavaHome
    $env:MAVEN_USER_HOME = $PreviousMavenHome
}

$services = @(
    @{ Name = 'discovery-service'; Jar = 'discovery_server/target/discovery_server-0.0.1-SNAPSHOT.jar' },
    @{ Name = 'gateway'; Jar = 'gateway/target/gateway-0.0.1-SNAPSHOT.jar' },
    @{ Name = 'inventory-service'; Jar = 'inventory_service/target/inventory_service-0.0.1-SNAPSHOT.jar' },
    @{ Name = 'order-service'; Jar = 'order_service/target/order_service-0.0.1-SNAPSHOT.jar' },
    @{ Name = 'product-service'; Jar = 'product_service/target/product_service-0.0.1-SNAPSHOT.jar' }
)

foreach ($service in $services) {
    $image = "$ImagePrefix/$($service.Name):$Tag"
    docker build --file (Join-Path $MicroservicesRoot 'Dockerfile') --build-arg "JAR_FILE=$($service.Jar)" --tag $image $MicroservicesRoot
    if ($LASTEXITCODE -ne 0) { throw "Building image '$image' failed." }
    minikube image load $image -p $Profile
    if ($LASTEXITCODE -ne 0) { throw "Loading image '$image' into Minikube failed." }
}
