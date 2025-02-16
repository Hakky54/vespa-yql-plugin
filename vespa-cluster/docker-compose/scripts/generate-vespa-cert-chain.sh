#!/usr/bin/env bash
set -e

OUTPUT_DIRECTORY=$(dirname $0)/../pki/vespa
#VESPA_HOSTNAMES=( $(echo node{0..9}.vespanet))
VESPA_HOSTNAMES=( $(echo vespa-config-{0..2}.vespanet) $(echo vespa-query-{0..1}.vespanet) $(echo vespa-feed-{0..1}.vespanet) $(echo vespa-content-{0..5}.vespanet))

cat > ${OUTPUT_DIRECTORY}/cert-exts.cnf << EOF
[req]
distinguished_name=req

[ca_exts]
basicConstraints       = critical, CA:TRUE
keyUsage               = critical, digitalSignature, cRLSign, keyCertSign
subjectKeyIdentifier   = hash
subjectAltName         = email:foo-ca@example.com

[host_exts]
basicConstraints       = critical, CA:FALSE
keyUsage               = critical, digitalSignature, keyAgreement, keyEncipherment
extendedKeyUsage       = serverAuth, clientAuth
subjectKeyIdentifier   = hash
authorityKeyIdentifier = keyid,issuer
subjectAltName         = @host_sans
[host_sans]
EOF

for (( i=1; i <= "${#VESPA_HOSTNAMES[@]}"; i++ )); do
  echo "DNS.${i} = ${VESPA_HOSTNAMES[i - 1]}" >> ${OUTPUT_DIRECTORY}/cert-exts.cnf
done
for (( i=1; i <= 13; i++ )); do
  let nw=i+9
  echo "IP.${i} = 10.0.10.${nw}" >> ${OUTPUT_DIRECTORY}/cert-exts.cnf
done
echo "IP.14 = 127.0.0.1" >> ${OUTPUT_DIRECTORY}/cert-exts.cnf
echo "DNS.$((${#VESPA_HOSTNAMES[@]} + 1)) = localhost" >> ${OUTPUT_DIRECTORY}/cert-exts.cnf


# Self-signed CA
openssl genrsa -out ${OUTPUT_DIRECTORY}/ca-vespa.key 2048

openssl req -x509 -new \
    -key ${OUTPUT_DIRECTORY}/ca-vespa.key \
    -out ${OUTPUT_DIRECTORY}/ca-vespa.pem \
    -subj '/C=US/L=California/O=ACME Inc/OU=Vespa Sample App Internal CA/CN=acme-vespa-ca.example.com' \
    -config ${OUTPUT_DIRECTORY}/cert-exts.cnf \
    -extensions ca_exts \
    -sha256 \
    -days 10000

# Create private key, CSR and certificate for host. Certificate has DNS SANs for all provided hostnames
openssl genrsa -out ${OUTPUT_DIRECTORY}/host.key 2048

openssl req -new -key ${OUTPUT_DIRECTORY}/host.key -out ${OUTPUT_DIRECTORY}/host.csr \
    -subj '/C=SE/L=Stockholm/O=SampleApp Inc/OU=Vespa Sample Apps' \
    -sha256

openssl x509 -req \
	-in ${OUTPUT_DIRECTORY}/host.csr \
	-CA ${OUTPUT_DIRECTORY}/ca-vespa.pem \
	-CAkey ${OUTPUT_DIRECTORY}/ca-vespa.key \
	-CAcreateserial \
	-CAserial ${OUTPUT_DIRECTORY}/serial.srl \
	-out ${OUTPUT_DIRECTORY}/host.pem \
	-extfile ${OUTPUT_DIRECTORY}/cert-exts.cnf \
	-extensions host_exts \
	-days 10000 \
	-sha256

openssl pkcs12 -export \
	-out ${OUTPUT_DIRECTORY}/client.full.pfx \
	-inkey ${OUTPUT_DIRECTORY}/host.key \
	-in ${OUTPUT_DIRECTORY}/host.pem \
	-certfile ${OUTPUT_DIRECTORY}/ca-vespa.pem
