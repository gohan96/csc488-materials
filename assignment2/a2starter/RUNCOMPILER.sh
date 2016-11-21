#!/bin/bash

STARTDIR="$(pwd)"
cd "$(dirname "$0")"
BASEDIR="$(pwd)"
cd "${STARTDIR}"

#  Compiler reads one source file from command line argument
#  Output to standard output
java -jar "${BASEDIR}/dist/compiler488.jar" "$@"
