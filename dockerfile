FROM openjdk:11
COPY target/universal/stage /root/app
WORKDIR /root/app
CMD ["./bin/pop-culture-puzzle"]
