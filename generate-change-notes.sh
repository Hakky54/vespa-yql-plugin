#!/bin/bash


echo "# Vespa-YQL-Plugin Change Notes" > change-notes.md

BUILD_TS=$(cat src/main/resources/build-info.properties  | grep "^build-timestamp:" | awk '{print $2" "$3" "$4}')

VERSION=$(yq .idea-plugin.version src/main/resources/META-INF/plugin.xml)  \

echo "" >> change-notes.md
echo "Latest Version: **$VERSION**" >> change-notes.md
echo "" >> change-notes.md
echo "Built at: **$BUILD_TS**" >> change-notes.md
echo "" >> change-notes.md

yq .idea-plugin.change-notes src/main/resources/META-INF/plugin.xml  \
    | sed 's/&gt;/\>/g'| sed 's/&lt;/\</g'| sed 's/&#34;/\"/g' >> change-notes.md
