FROM registry.access.redhat.com/ubi8/openjdk-8 as builder

WORKDIR /source

COPY --chown=jboss:jboss pom.xml /source/

COPY --chown=jboss:jboss ./src/ /source/src/

RUN whoami && pwd && ls -la && mvn -X -e clean package -DskipTests=true

FROM registry.access.redhat.com/ubi8/openjdk-8 as app

COPY --from=builder /source/target/provider-search-0.0.1-SNAPSHOT.jar /app/provider-search.jar

EXPOSE 8080

CMD [ "java", "-jar", "/app/provider-search.jar" ]