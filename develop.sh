#!/usr/bin/env sh

(
  docker run -p 5432:5432 \
    -e POSTGRES_USER=postgres postgres:11.5 \
    > /dev/null 2> /dev/null &
  npm run watch &
  sbt ~reStart
)

