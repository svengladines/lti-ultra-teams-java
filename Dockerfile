FROM azul/zulu-openjdk-centos:22-latest

EXPOSE 8080

ENV APP_HOME /usr/src/app

COPY target/lti-ultra-teams-java-1.0.0-SNAPSHOT.jar $APP_HOME/service.jar

WORKDIR $APP_HOME

CMD ["java", "-jar", "service.jar"]
