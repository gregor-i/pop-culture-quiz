#!/bin/bash


FOLDER=`echo $1 | sed 's/^\(.\).*/\1/'`
QUERY=`echo $1 | sed 's/\s/_/'`

curl -s "https://sg.media-imdb.com/suggests/$FOLDER/$QUERY.json" | sed 's/^[a-zA-Z\$]*(\(.*\)).*$/\1/' | jq .
