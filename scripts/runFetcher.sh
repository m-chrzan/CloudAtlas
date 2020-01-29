#!/bin/bash

./gradlew runFetcher -Dhostname="rainbow${NODE_NUMBER}" -DzonePath="/${ZONE}/rainbow${NODE_NUMBER}" -DownAddr="10.1.1.1${NODE_NUMBER#0}" -DfallbackContacts="$FALLBACKS"
