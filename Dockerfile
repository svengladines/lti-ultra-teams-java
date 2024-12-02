FROM azul/zulu-openjdk-centos:22-latest

ARG ARTIFACT
ADD ${ARTIFACT} /opt/service.jar
