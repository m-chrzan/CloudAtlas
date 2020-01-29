#!/bin/bash

./gradlew runClient -Dhostname="rainbow${NODE_NUMBER}" -DzonePath="/${ZONE}/rainbow${NODE_NUMBER}" -DquerySignerHostname=rainbow01
