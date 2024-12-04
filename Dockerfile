FROM azul/zulu-openjdk-centos:22-latest

WORKDIR /app

COPY ${JAR_FILE} service.jar
EXPOSE 8080
CMD ["java", "-jar", "service.jar"]
