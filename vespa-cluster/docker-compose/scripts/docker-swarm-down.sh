#!/bin/bash


#docker rm -f config0 config1 config2
#docker rm -f node0 node1 node2
#docker rm -f cfg0 cfg1 cfg2

cat $(pwd)/pki/vespa/cert-exts.cnf  | grep "^DNS" | grep -v "localhost" | {
  while read line; do
    NO=$(echo ${line} | awk '{print $1}' | cut -d'.' -f2)
    FQDN=$(echo ${line} | awk '{print $3}')
    IP=$(cat pki/vespa/cert-exts.cnf  | grep "IP.${NO} " | awk '{print $3}')
    NODE_NAME=$(echo ${FQDN} | cut -d'.' -f1)
    docker rm -f ${NODE_NAME}
  done
}


docker network rm -f vespanet

