#!/bin/sh
set -eu

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd -P)
cd "$ROOT"

python3 tools/verify_build_foundation.py --allow-missing-wrapper-jar
python3 tools/verify_final.py
python3 tools/verify_hardening.py
python3 tools/verify_phase2_premium_home.py
python3 tools/verify_phase3_premium_reader.py
python3 tools/verify_phase4_premium_search.py
python3 tools/verify_phase5_premium_bookmarks.py
python3 tools/verify_phase6_premium_audio.py
python3 tools/verify_phase7_premium_tafsir.py
python3 tools/verify_phase8_premium_adhkar.py
python3 tools/verify_flagship_review.py
python3 tools/release_preflight.py --require-signing --require-version

if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then
    ./bootstrap-gradle-wrapper.sh
fi
python3 tools/verify_build_foundation.py --require-wrapper-jar

./gradlew clean \
    :core:common:test \
    :core:media:test \
    :domain:test \
    :data:testDebugUnitTest \
    :app:testDebugUnitTest \
    :data:assembleAndroidTest \
    :app:assembleDebug \
    :app:assembleBenchmark \
    :app:assembleNonMinifiedRelease \
    :baseline-profile:assembleBenchmark \
    :baseline-profile:assembleNonMinifiedRelease \
    :app:lintDebug \
    :app:lintRelease \
    --warning-mode=all \
    --stacktrace

./gradlew :app:bundleRelease --stacktrace

python3 tools/verify_android_artifact.py \
    app/build/outputs/bundle/release/app-release.aab \
    --report .verification/release-artifact.json \
    --require-signature
