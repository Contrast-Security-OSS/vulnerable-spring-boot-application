# Vulnerable Spring Boot Application

This application is an intentionally vulnerable Spring Boot application. The intent is to demonstrate the capabilities of Contrast Security's Maven plugin when used with continous integration (such as TravisCI or CircleCI).

## Build

```
mvn install
```

## Run

```
java -jar target/provider-search-0.0.1-SNAPSHOT.jar
```

* Go to http://localhost:8081/
* Search for zip 21230

## Test

```
mvn clean test
```

## Details

The application runs using an in-memory H2 database. Schema and sample data should load on boot.