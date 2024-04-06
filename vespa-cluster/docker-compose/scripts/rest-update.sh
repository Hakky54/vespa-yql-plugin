#!/bin/bash

ENDPOINT="http://localhost:8080/document/v1/embeddings/books/docid/466F756E646174696F6E20616E64204561727468202D204973616163204173696D6F762E65707562-256"

curl -s -X PUT -H "Content-Type:application/json" --data '
       {
      "fields": {
          "content": {
              "assign": "Warmplay"
          }
      }
  }' \
  ${ENDPOINT} | jq . -C
