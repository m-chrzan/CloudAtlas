#!/bin/bash

cd build/tmp
# generate private key
openssl genrsa -out query_signer.pem 2048
# convert private key to PKCS8 format
openssl pkcs8 -topk8 -inform PEM -outform DER -in query_signer.pem -out query_signer -nocrypt
# generate public key
openssl rsa -in query_signer.pem -pubout -outform DER -out query_signer.pub
