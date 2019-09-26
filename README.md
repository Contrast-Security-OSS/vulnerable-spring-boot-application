# Vulnerable Spring Boot Application

This application is an intentionally vulnerable Spring Boot application. The intent is to demonstrate the capabilities of Contrast Security's Maven plugin when used with continuous integration (such as TravisCI or CircleCI).

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

## Running With Contrast

You will need an account with Contrast https://www.contrastsecurity.com

* Review the `run-with-contrast` profile settings in `pom.xml`

* Set the following environment variables

```
CONTRAST_MAVEN_USERNAME
CONTRAST_MAVEN_ORGUUID
CONTRAST_MAVEN_TEAMSERVERURL
CONTRAST_MAVEN_APIKEY
CONTRAST_MAVEN_SERVICEKEY
```

* `mvn install -P run-with-contrast`


## Details

The application runs using an in-memory H2 database. Schema and sample data should load on boot.
