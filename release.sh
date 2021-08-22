#!/usr/bin/env sh

npm ci
npm run build

sbt stage
docker build -t registry.heroku.com/pop-culture-quiz-2/web .

docker push registry.heroku.com/pop-culture-quiz-2/web
heroku container:release web -a pop-culture-quiz-2
