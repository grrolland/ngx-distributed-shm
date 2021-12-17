#!/bin/bash
docker login -u="$QUAY_USER" -p="$QUAY_TOKEN" quay.io
export TAG=$(if [ "$TRAVIS_BRANCH" == "master" ]; then echo "latest"; else echo $TRAVIS_BRANCH ; fi)
export IMAGE_NAME=quay.io/grrolland/ngx-distributed-shm
docker build -f Dockerfile -t $IMAGE_NAME:$TAG .
docker push $IMAGE_NAME