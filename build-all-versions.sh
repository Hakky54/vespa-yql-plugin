#!/bin/bash
# Inspired from https://github.com/BashSupport/BashSupport/blob/master/build-all-branches.bash

function ideaBranch {
  if [[ "$1" == 233.* || "$1" == 2023.3.* ]]; then
    echo "233"
  elif [[ "$1" == 232.* || "$1" == 2023.2.* ]]; then
    echo "232"
  elif [[ "$1" == 231.* || "$1" == 2023.1.* ]]; then
    echo "231"
  fi
}

mkdir -p dist
for v in "2023.1.5" "2023.2.6" "2023.3.3"; do
  #branch="$(ideaBranch $v)"
  #echo "## Building with version $v, IDEA branch $branch..."
  # _JAVA_OPTIONS="" JAVA_OPTS=""
  # --stacktrace
  #-DideaBranch="$branch"

  echo "## Building with version $v, IDEA..."
  ./gradlew < /dev/null -DideaVersion="$v"  clean buildPlugin 

    status=$?
    if [[ $status -ne 0 ]]; then
        echo "## Build for version $v failed. Exiting." >&2
        exit 1
    fi
    mv build/distributions/vespa-yql-plugin-1.0.0.zip dist/vespa-yql-plugin-1.0.0-v${v}.zip
done
