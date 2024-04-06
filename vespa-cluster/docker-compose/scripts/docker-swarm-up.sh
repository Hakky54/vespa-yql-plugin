#!/bin/bash
# Script to start Vespa cluster with docker swarm

IMG="vespaengine/vespa:8"

NETWORK="vespanet"

CONFIG_SERVERS=vespa-config-0.${NETWORK},vespa-config-1.${NETWORK},vespa-config-2.${NETWORK}

start_config_node() {

  NODE_NAME=$1
  # 10.0.10.10
  IP=$2
  # 19071
  CFG_PORT=$3
  # 19100
  SVC_PORT=$4
  # 19050
  MGMT_PORT=$5
  # 19092
  METRICS_PORT=$6
  
  docker < /dev/null run --detach --name ${NODE_NAME} --hostname ${NODE_NAME}.${NETWORK} \
         -e VESPA_CONFIGSERVERS=${CONFIG_SERVERS} \
         -e VESPA_CONFIGSERVER_JVMARGS="-Xms32M -Xmx128M" \
         -e VESPA_CONFIGPROXY_JVMARGS="-Xms32M -Xmx32M" \
         -e VESPA_TLS_CONFIG_FILE=/var/tls/tls.json \
         --network ${NETWORK} \
         --publish ${CFG_PORT}:19071 --publish ${SVC_PORT}:19100 --publish ${MGMT_PORT}:19050 --publish ${METRICS_PORT}:19092 \
         --volume $(pwd)/tls/:/var/tls/ \
         --ip ${IP} \
         ${IMG}
}

start_container_node() {

  NODE_NAME=$1
  # 10.0.10.10
  IP=$2
  SVC_PORT=$3
  SVC_SSL_PORT=$4
  # MGMT_PORT=$5
  # --publish ${MGMT_PORT}:19050
  # METRICS_PORT=$6
  # --publish ${METRICS_PORT}:19092 
  
  docker < /dev/null run --detach --name ${NODE_NAME} --hostname ${NODE_NAME}.${NETWORK} \
         -e VESPA_CONFIGSERVERS=${CONFIG_SERVERS} \
         -e VESPA_CONFIGSERVER_JVMARGS="-Xms32M -Xmx128M" \
         -e VESPA_CONFIGPROXY_JVMARGS="-Xms32M -Xmx32M" \
         -e VESPA_TLS_CONFIG_FILE=/var/tls/tls.json \
         --network ${NETWORK} \
         --publish ${SVC_PORT}:8080 --publish ${SVC_SSL_PORT}:8443   \
         --volume $(pwd)/tls/:/var/tls/ \
         --ip ${IP} \
         ${IMG} \
         services
}

start_content_node() {

  NODE_NAME=$1
  # 10.0.10.10
  IP=$2
  
  docker < /dev/null run --detach --name ${NODE_NAME} --hostname ${NODE_NAME}.${NETWORK} \
         -e VESPA_CONFIGSERVERS=${CONFIG_SERVERS} \
         -e VESPA_CONFIGSERVER_JVMARGS="-Xms32M -Xmx128M" \
         -e VESPA_CONFIGPROXY_JVMARGS="-Xms32M -Xmx32M" \
         -e VESPA_TLS_CONFIG_FILE=/var/tls/tls.json \
         --network ${NETWORK} \
         --volume $(pwd)/tls/:/var/tls/ \
         --ip ${IP} \
         ${IMG} \
         services
}

# Ignore any errors, just make sure the swarm is initialized
docker swarm init &> /dev/null

# Create network
docker network create --driver overlay --subnet 10.0.10.0/24 --attachable ${NETWORK}

#start_config_node config0 10.0.10.10 19071 19100 19050 20092
#start_config_node config1 10.0.10.11 19072 19101 19051 20093
#start_config_node config2 10.0.10.12 19073 19102 19052 20094

#start_config_node cfg0 10.0.10.10 19071 19100 19050 20092
#start_config_node cfg1 10.0.10.11 19072 19101 19051 20093
#start_config_node cfg2 10.0.10.12 19073 19102 19052 20094

cfg_port=19071
svc_port=19100
mgmt_port=19050
metrics_port=19092

cat $(pwd)/pki/vespa/cert-exts.cnf  | grep "^DNS" | grep "vespa-config" | {
  while read line; do
    NO=$(echo ${line} | awk '{print $1}' | cut -d'.' -f2)
    FQDN=$(echo ${line} | awk '{print $3}')
    IP=$(cat pki/vespa/cert-exts.cnf  | grep "IP.${NO} " | awk '{print $3}')
    NODE_NAME=$(echo ${FQDN} | cut -d'.' -f1)
    start_config_node ${NODE_NAME} ${IP} ${cfg_port} ${svc_port} ${mgmt_port} ${metrics_port}
    ((cfg_port+=1))
    ((svc_port+=1))
    ((mgmt_port+=1))
    ((metrics_port+=1))
  done
}

container_svc_port=8080
container_svc_ssl_port=8443
#metrics_port=19192

cat $(pwd)/pki/vespa/cert-exts.cnf  | grep "^DNS" | grep "vespa-query" | {
  while read line; do
    NO=$(echo ${line} | awk '{print $1}' | cut -d'.' -f2)
    FQDN=$(echo ${line} | awk '{print $3}')
    IP=$(cat pki/vespa/cert-exts.cnf  | grep "IP.${NO} " | awk '{print $3}')
    NODE_NAME=$(echo ${FQDN} | cut -d'.' -f1)
    
    start_container_node ${NODE_NAME} ${IP} ${container_svc_port} ${container_svc_ssl_port}
    # ${metrics_port}
    ((container_svc_port+=1))
    ((container_svc_ssl_port+=1))
    #((metrics_port+=1))
  done
}


container_svc_port=9080
container_svc_ssl_port=9443
#metrics_port=19292

cat $(pwd)/pki/vespa/cert-exts.cnf  | grep "^DNS" | grep "vespa-feed" | {
  while read line; do
    NO=$(echo ${line} | awk '{print $1}' | cut -d'.' -f2)
    FQDN=$(echo ${line} | awk '{print $3}')
    IP=$(cat pki/vespa/cert-exts.cnf  | grep "IP.${NO} " | awk '{print $3}')
    NODE_NAME=$(echo ${FQDN} | cut -d'.' -f1)
    
    start_container_node ${NODE_NAME} ${IP} ${container_svc_port} ${container_svc_ssl_port}
    #${metrics_port}
    ((container_svc_port+=1))
    ((container_svc_ssl_port+=1))
    #((metrics_port+=1))
  done
}

cat $(pwd)/pki/vespa/cert-exts.cnf  | grep "^DNS" | grep "vespa-content" | {
  while read line; do
    NO=$(echo ${line} | awk '{print $1}' | cut -d'.' -f2)
    FQDN=$(echo ${line} | awk '{print $3}')
    IP=$(cat pki/vespa/cert-exts.cnf  | grep "IP.${NO} " | awk '{print $3}')
    NODE_NAME=$(echo ${FQDN} | cut -d'.' -f1)
    
    start_content_node ${NODE_NAME} ${IP} 
  done
}




