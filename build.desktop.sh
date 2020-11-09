#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
./gradlew desktop:dist
cd desktop/build/libs
echo "app.rank=1" > config.ini
zip release.zip desktop-1.0.jar config.ini

