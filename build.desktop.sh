#!/bin/bash

./gradlew desktop:dist
cd desktop/build/libs
zip release.zip desktop-1.0.jar config.ini
