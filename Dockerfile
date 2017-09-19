FROM frolvlad/alpine-oraclejdk8
MAINTAINER Dharshana Ratnayake <darthShana@gmail.com>

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/lostvictories/app.jar", "-P/etc/config/lostVictoriesServer.properties"]

# Add the service itself
ADD target/app.jar /usr/lostvictories/app.jar