#!/bin/bash

STARTDIR="$(pwd)"
cd "$(dirname "$0")"
BASEDIR="$(pwd)"
cd "${STARTDIR}"

# Compiler reads option flags and one or more source files from command line
# arguments

java -jar "${BASEDIR}/dist/compiler488.jar" "$@"
