#!/usr/bin/env sh
set -e

cd "$(dirname "$0")"

mvn install:install-file \
  -Dfile=../cat-client/target/cat-client-4.0-RC1.jar \
  -DpomFile=../cat-client/target/pom.xml
