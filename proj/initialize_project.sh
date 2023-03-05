#!/bin/bash

# from: https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html

mvn archetype:generate \
  -DarchetypeGroupId=software.amazon.awssdk \
  -DarchetypeArtifactId=archetype-app-quickstart \
  -DarchetypeVersion=2.18.16