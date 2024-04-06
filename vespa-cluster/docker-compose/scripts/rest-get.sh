#!/bin/bash

ENDPOINT="https://localhost:8443/document/v1/embeddings/books/docid/466F756E646174696F6E20616E64204561727468202D204973616163204173696D6F762E65707562-256"

curl -sv -X GET \
     -H "Content-Type:application/json" \
     -key $(pwd)/pki/client/client.key \
     --cert $(pwd)/pki/client/client.pem \
     --cacert $(pwd)/pki/vespa/ca-vespa.pem  \
     ${ENDPOINT} | jq .
