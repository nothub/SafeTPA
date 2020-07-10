#!/usr/bin/env bash

set -e

echo "downloading paper 1.12.2 (1618)"

mkdir -p ../server/plugins/bStats
cd ../server

curl https://papermc.io/api/v1/paper/1.12.2/1618/download -o paper.jar

if md5sum -c <<< "4c81838696da39b1b06987e81ca8b0af paper.jar" | grep -q "paper.jar: OK"; then
  echo "paper download successful"
else
  echo "paper download failed"
  exit 1
fi

{
  echo "eula=true"
} > ../server/eula.txt

{
  echo "enabled: false"
  echo "serverUuid: 00000000-0000-0000-0000-000000000000"
  echo "logFailedRequests: false"
} > ../server/plugins/bStats/config.yml

echo "paper installation complete"
