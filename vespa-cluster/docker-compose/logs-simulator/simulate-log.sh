#!/bin/bash

usage() {
  echo "USAGE; $0 {cmd}"
  echo ""
  echo "where {cmd} is one of"
  echo "  reset        - Reset the log structure"
  echo "  add-line-10  - Add new logline to logs/logarchive/2024/02/03/10-0"
  echo "  add-line-11  - Add new logline to (new file) logs/logarchive/2024/02/03/11-0"
  echo "  gz-file-09   - Change 09-0 to 09-0.gz in logs/logarchive/2024/02/03"
}

setupLogsDir() {
  if [ ! -d $(pwd)/logs ]; then
    echo "Setting up logs directory"
    mkdir $(pwd)/logs
    cp -rf ../../../src/test/testData/logarchive $(pwd)/logs/.
  fi
}

setupLogsDir

logline() {
  ts=$(date +%s)
  printf "%s.0\tvespa-config-2.vespanet\t747/37423\tslobrok\tvespa-slobrok.slobrok.server.rpchooks\tevent\tThis is the log message\n" "$ts"
}


cmd=$1

case "$cmd" in
  help)
    usage
  ;;
  reset)
    rm -rf $(pwd)/logs
    setupLogsDir
  ;;
  add-line-10)
    logline >> $(pwd)/logs/logarchive/2024/02/03/10-0
  ;;
  add-line-11)
    logline >> $(pwd)/logs/logarchive/2024/02/03/11-0
    ;;
  gz-file-09)
    mv $(pwd)/logs/logarchive/2024/02/03/09-0 $(pwd)/logs/logarchive/2024/02/03/09-0.gz
    ;;
  *)
    usage
    ;;
esac


