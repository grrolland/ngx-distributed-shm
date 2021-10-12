#!/bin/bash
docker login -u="$QUAY_USER" -p="$QUAY_TOKEN" quay.io
export TAG=$(if [ "$TRAVIS_BRANCH" == "master" ]; then echo "latest"; else echo $TRAVIS_BRANCH ; fi)
export IMAGE_NAME=grrolland/ngx-distibuted-shm
docker build -f Dockerfile -t $IMAGE_NAME:$TAG .
docker tag $IMAGE_NAME:$COMMIT $IMAGE_NAME:$TAG
docker tag $IMAGE_NAME:$COMMIT $IMAGE_NAME:travis-$TRAVIS_BUILD_NUMBER
docker push quay.io/$IMAGE_NAME