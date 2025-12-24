# CouponSystemProject

JHF FullStack Bootcamp Project

## Prerequisites

- Java 21
- Maven
- Docker
- Docker Compose

## How to Build

To build the project, run the following command:

```bash
mvn clean package
```

This will create an executable JAR file in the `target/` directory.

## How to Test

To run the tests, run the following command:

```bash
mvn test
```

## How to Run

To run the application, you can use Docker Compose. First, make sure you have Docker and Docker Compose installed. Then, run the following command:

```bash
docker-compose up
```

This will start the application and a MySQL database in separate containers. The application will be available at `http://localhost:8080`.
