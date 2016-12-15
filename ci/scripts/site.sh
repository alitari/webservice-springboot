#!/bin/bash

set -e -x

mvn -f webservice-springboot -s /usr/share/maven/ref/settings-docker.xml clean site