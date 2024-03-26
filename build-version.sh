#!/bin/bash

target_version=$1

if [[ -z $target_version ]]; then
  echo "USAGE: $0 {target-version}"
  echo ""
  echo "Example:"
  echo "   $0 2023.3.6"
  exit 1
fi

mkdir -p dist

echo "## Building with version ${target_version}, IDEA..."
./gradlew < /dev/null clean
./gradlew < /dev/null -DideaVersion="${target_version}" buildPlugin 

status=$?
if [[ $status -ne 0 ]]; then
  echo "## Build for version ${target_version} failed. Exiting." >&2
  exit 1
fi
mv build/distributions/vespa-yql-plugin-1.0.0.zip dist/vespa-yql-plugin-1.0.0-v${target_version}.zip


