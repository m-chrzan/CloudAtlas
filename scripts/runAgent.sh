#!/bin/bash

./gradlew runAgent -Dhostname="rainbow${NODE_NUMBER}" -DzonePath="/${ZONE}/rainbow${NODE_NUMBER}"
