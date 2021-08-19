# docker build -t gregor23/pop-culture-quiz:latest .

FROM openjdk:11
COPY target/universal/stage /root/app
WORKDIR /root/app

EXPOSE 9000
CMD ["./bin/pop-culture-puzzle"]
