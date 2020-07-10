#!/usr/bin/env bash

set -e

rm -f ../server/plugins/SafeTP-*.jar
cp ../target/SafeTP-*.jar ../server/plugins
