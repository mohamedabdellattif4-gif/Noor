#!/bin/sh
set -eu

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
EXPECTED_WRAPPER_SHA256="55243ef57851f12b070ad14f7f5bb8302daceeebc5bce5ece5fa6edb23e1145c"

if [ ! -f "$CLASSPATH" ]; then
  echo "Gradle Wrapper JAR is missing. Run ./bootstrap-gradle-wrapper.sh first." >&2
  exit 1
fi

if command -v sha256sum >/dev/null 2>&1; then
  ACTUAL_WRAPPER_SHA256=$(sha256sum "$CLASSPATH" | awk '{print $1}')
elif command -v shasum >/dev/null 2>&1; then
  ACTUAL_WRAPPER_SHA256=$(shasum -a 256 "$CLASSPATH" | awk '{print $1}')
else
  echo "Unable to verify Gradle Wrapper integrity: sha256sum or shasum is required." >&2
  exit 1
fi

if [ "$ACTUAL_WRAPPER_SHA256" != "$EXPECTED_WRAPPER_SHA256" ]; then
  echo "Gradle Wrapper JAR checksum mismatch. Remove it and run ./bootstrap-gradle-wrapper.sh." >&2
  exit 1
fi

JAVA_CMD=${JAVA_HOME:+$JAVA_HOME/bin/}java
if [ ! -x "$JAVA_CMD" ]; then
  JAVA_CMD=java
fi

if ! command -v "$JAVA_CMD" >/dev/null 2>&1 && [ ! -x "$JAVA_CMD" ]; then
  echo "Java was not found. Install JDK 17 or set JAVA_HOME to a JDK 17 installation." >&2
  exit 1
fi

exec "$JAVA_CMD" \
  -Dorg.gradle.appname=gradlew \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain "$@"
