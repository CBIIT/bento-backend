#!/bin/bash

REPO=$REPO
TOKEN=$TOKEN

cd /home/bento/actions-runner

./config.sh --url https://github.com/CBIIT/${REPO} --token ${TOKEN}

cleanup() {
    echo "Removing runner..."
    ./config.sh remove --unattended --token ${REG_TOKEN} --name bento-runner-0 --labels bento-runner
}

trap 'cleanup; exit 130' INT
trap 'cleanup; exit 143' TERM

./run.sh & wait $!