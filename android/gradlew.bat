@echo off
setlocal
set "APP_HOME=%~dp0"
set "CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar"
set "EXPECTED_WRAPPER_SHA256=55243ef57851f12b070ad14f7f5bb8302daceeebc5bce5ece5fa6edb23e1145c"

if not exist "%CLASSPATH%" (
  echo Gradle Wrapper JAR is missing. Run bootstrap-gradle-wrapper.ps1 first. 1>&2
  exit /b 1
)

set "NOOR_WRAPPER_JAR=%CLASSPATH%"
set "ACTUAL_WRAPPER_SHA256="
for /f "usebackq delims=" %%H in (`powershell.exe -NoLogo -NoProfile -NonInteractive -Command "(Get-FileHash -LiteralPath $env:NOOR_WRAPPER_JAR -Algorithm SHA256).Hash.ToLowerInvariant()"`) do set "ACTUAL_WRAPPER_SHA256=%%H"
if errorlevel 1 (
  echo Unable to verify Gradle Wrapper integrity with PowerShell. 1>&2
  exit /b 1
)
if /I not "%ACTUAL_WRAPPER_SHA256%"=="%EXPECTED_WRAPPER_SHA256%" (
  echo Gradle Wrapper JAR checksum mismatch. Remove it and run bootstrap-gradle-wrapper.ps1. 1>&2
  exit /b 1
)

if defined JAVA_HOME (
  set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVA_EXE=java.exe"
)

"%JAVA_EXE%" -version >nul 2>&1
if errorlevel 1 (
  echo Java was not found. Install JDK 17 or set JAVA_HOME to a JDK 17 installation. 1>&2
  exit /b 1
)

"%JAVA_EXE%" -Dorg.gradle.appname=gradlew -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
exit /b %ERRORLEVEL%
