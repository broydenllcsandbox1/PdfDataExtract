#!/bin/bash

AWS_ACCESS_KEY_ID=$1
AWS_SECRET_ACCESS_KEY=$2
AWS_DEFAULT_REGION=$3
PROFILE_NAME=$4

docker build -t mvn-amazon-corretto-17 --build-arg access_key=$AWS_ACCESS_KEY_ID --build-arg secret_access_key=$AWS_SECRET_ACCESS_KEY --build-arg default_region=$AWS_DEFAULT_REGION --build-arg profile_name=$PROFILE_NAME .