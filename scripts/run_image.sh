#!/bin/bash
docker run -it --entrypoint '/bin/sh' -v$PWD/..:/broyden mvn-amazon-corretto-17