#!/bin/bash


mkdir -p build/

if [[ ! -f build/verifier-all.jar ]]; then
  echo "Downloading latest plugin verifier..."
  curl -s https://api.github.com/repos/JetBrains/intellij-plugin-verifier/releases/latest \
    | jq -r '.assets[].browser_download_url' \
    | xargs curl -L --output build/verifier-all.jar
fi

# Make sure the plugin is built into build/distributions
./gradlew buildPlugin

# Run the verifier
IDE_ROOT=${IDE_ROOT:-/opt/idea-IC-243.23654.189}
LATEST_PLUGIN=$(ls build/distributions/*.zip | tail -1)
java -jar build/verifier-all.jar \
     check-plugin \
     ${LATEST_PLUGIN} \
     ${IDE_ROOT}  \
     -verification-reports-dir build/verification-report

