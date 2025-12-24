FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/CouponSystemProject-1.0-SNAPSHOT-jar-with-dependencies.jar .
EXPOSE 8080
CMD ["java", "-jar", "CouponSystemProject-1.0-SNAPSHOT-jar-with-dependencies.jar"]
