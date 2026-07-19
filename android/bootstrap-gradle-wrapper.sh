#!/bin/sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
WRAPPER_VERSION="9.4.1"
WRAPPER_URL="https://services.gradle.org/distributions/gradle-${WRAPPER_VERSION}-wrapper.jar"
WRAPPER_PATH="$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar"
EXPECTED_SHA256="55243ef57851f12b070ad14f7f5bb8302daceeebc5bce5ece5fa6edb23e1145c"
TEMP_FILE="${WRAPPER_PATH}.tmp.$$"

cleanup() {
    rm -f "$TEMP_FILE"
}
trap cleanup EXIT HUP INT TERM

sha256_of() {
    if command -v sha256sum >/dev/null 2>&1; then
        sha256sum "$1" | awk '{print $1}'
    elif command -v shasum >/dev/null 2>&1; then
        shasum -a 256 "$1" | awk '{print $1}'
    else
        echo "Error: sha256sum or shasum is required to verify the Gradle Wrapper." >&2
        exit 1
    fi
}

mkdir -p "$(dirname "$WRAPPER_PATH")"

if [ -f "$WRAPPER_PATH" ]; then
    CURRENT_SHA256=$(sha256_of "$WRAPPER_PATH")
    if [ "$CURRENT_SHA256" = "$EXPECTED_SHA256" ]; then
        echo "Gradle Wrapper $WRAPPER_VERSION is already installed and verified."
        exit 0
    fi
    echo "Existing Gradle Wrapper failed checksum verification; replacing it." >&2
fi

if command -v curl >/dev/null 2>&1; then
    curl \
        --fail \
        --location \
        --retry 3 \
        --retry-all-errors \
        --connect-timeout 15 \
        --max-time 120 \
        --silent \
        --show-error \
        "$WRAPPER_URL" \
        --output "$TEMP_FILE"
elif command -v wget >/dev/null 2>&1; then
    wget \
        --https-only \
        --tries=3 \
        --timeout=120 \
        --quiet \
        "$WRAPPER_URL" \
        --output-document="$TEMP_FILE"
else
    echo "Error: curl or wget is required to download the Gradle Wrapper." >&2
    exit 1
fi

ACTUAL_SHA256=$(sha256_of "$TEMP_FILE")
if [ "$ACTUAL_SHA256" != "$EXPECTED_SHA256" ]; then
    echo "Error: Gradle Wrapper checksum verification failed." >&2
    exit 1
fi

chmod 0644 "$TEMP_FILE"
mv -f "$TEMP_FILE" "$WRAPPER_PATH"
trap - EXIT HUP INT TERM
echo "Gradle Wrapper $WRAPPER_VERSION installed and verified."
