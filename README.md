# Vulnerable Spring Boot Application

This application is an intentionally vulnerable Spring Boot application to be used in conjunction with Contrast Security's Openshift Pipelines Tasks to demonstrate how to add Contrast's Java Agent to an application when unable to include it from source.

## Build

```
mvn install
```

## Run

```
java -jar target/provider-search-0.0.1-SNAPSHOT.jar
```

* Go to http://localhost:8080/
* Search for zip 21230

## Test

```
mvn clean test
```

## Details

The application runs using an in-memory H2 database. Schema and sample data should load on boot.
