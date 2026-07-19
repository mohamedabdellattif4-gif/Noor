$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$wrapperVersion = "9.4.1"
$wrapperUrl = "https://services.gradle.org/distributions/gradle-$wrapperVersion-wrapper.jar"
$wrapperPath = Join-Path $PSScriptRoot "gradle/wrapper/gradle-wrapper.jar"
$expectedSha256 = "55243ef57851f12b070ad14f7f5bb8302daceeebc5bce5ece5fa6edb23e1145c"
$temporaryPath = "$wrapperPath.tmp.$PID"

function Get-LowerSha256([string] $Path) {
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

New-Item -ItemType Directory -Force -Path (Split-Path -Parent $wrapperPath) | Out-Null

try {
    if (Test-Path -LiteralPath $wrapperPath -PathType Leaf) {
        if ((Get-LowerSha256 $wrapperPath) -eq $expectedSha256) {
            Write-Host "Gradle Wrapper $wrapperVersion is already installed and verified."
            exit 0
        }
        Write-Warning "Existing Gradle Wrapper failed checksum verification; replacing it."
    }

    $request = @{
        Uri = $wrapperUrl
        OutFile = $temporaryPath
        MaximumRedirection = 5
        TimeoutSec = 120
    }
    Invoke-WebRequest @request

    $actualSha256 = Get-LowerSha256 $temporaryPath
    if ($actualSha256 -ne $expectedSha256) {
        throw "Gradle Wrapper checksum verification failed."
    }

    Move-Item -LiteralPath $temporaryPath -Destination $wrapperPath -Force
    Write-Host "Gradle Wrapper $wrapperVersion installed and verified."
}
finally {
    Remove-Item -LiteralPath $temporaryPath -Force -ErrorAction SilentlyContinue
}
