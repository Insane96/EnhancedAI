#!/bin/bash
set -e

./gradlew publishToMavenLocal

gh workflow run publish.yaml --ref "$(git rev-parse --abbrev-ref HEAD)"
