#!/bin/bash

./gradlew desktop:dist
cd desktop/build/libs
echo "app.rank=1" > config.ini
zip release.zip desktop-1.0.jar config.ini

