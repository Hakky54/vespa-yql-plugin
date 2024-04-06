#!/bin/bash

tmp=$(mktemp)
jq .[] $(pwd)/../books.json -c | less | {
  while read line; do
    id=$(echo "$line" | jq .id -r)
    fields=$(echo "$line" | jq .fields)
    #echo "id: ${id}"
    echo "${line}" > ${tmp}

    # id:embeddings:books::466F756E646174696F6E202D204973616163204173696D6F762E65707562-22
    #https://localhost:9080/document/v1/default/books/docid/466F756E646174696F6E202D204973616163204173696D6F762E65707562-22
    doc_id=$(echo $id | cut -d':' -f5)

    
    curl < /dev/null \
         -s \
          --key $(pwd)/pki/vespa/host.key \
          --cert $(pwd)/pki/vespa/host.pem \
          --cacert $(pwd)/pki/vespa/ca-vespa.pem  \
          -X POST \
          -H "Content-Type:application/json" \
          --data @${tmp} \
          https://localhost:9443/document/v1/embeddings/books/docid/${doc_id} | jq . -c
    
  done
}

rm -f ${tmp}
