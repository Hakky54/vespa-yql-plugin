#!/bin/bash
rm -rf $(pwd)/logs
mkdir -p $(pwd)/logs
docker run --detach \
       --name vespa \
       --hostname vespa-container \
       --publish 8080:8080 \
       --publish 19071:19071 \
       --publish 19050:19050 \
       --volume $(pwd)/logs:/opt/vespa/logs/vespa/ \
       vespaengine/vespa:8
