#!/bin/bash

#from: https://unix.stackexchange.com/questions/145749/passing-arguments-from-a-file-to-a-bash-script
IFS=$'\n' read -ra arr -d '' <$BROYDEN_HOME/aws.pdfextract.credentials
source ./build_image.sh "${arr[@]}"
