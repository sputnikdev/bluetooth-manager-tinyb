#!/usr/bin/env bash
mvn install:install-file -Dfile=lib/tinyb.jar -DgroupId=intel-iot-devkit -DartifactId=tinyb -Dversion=0.6.0-SNAPSHOT -Dpackaging=jar