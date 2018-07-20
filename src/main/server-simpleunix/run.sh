#!/bin/bash

BASEDIR=$(dirname "$0")
timestamp=$(date +"%Y-%m-%d_%H%M%S")

java -jar $BASEDIR/bin/pdflettercreation-0.0.1-SNAPSHOT.jar --server.port=4711 2>&1 1>>logs/$timestamp-out.txt &
