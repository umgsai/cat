FROM maven:3.9.12-eclipse-temurin-21 AS mavenrepo

WORKDIR /app
COPY cat-alarm cat-alarm
COPY cat-consumer cat-consumer
COPY cat-hadoop cat-hadoop
COPY cat-client cat-client
COPY cat-core cat-core
COPY cat-home cat-home
COPY cat-boot cat-boot
COPY pom.xml pom.xml
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
ENV TZ=Asia/Shanghai
WORKDIR /app
COPY --from=mavenrepo /app/cat-boot/target/cat-boot-4.0-RC1.jar /app/cat-boot.jar
COPY docker/datasources.xml /data/appdatas/cat/datasources.xml
COPY docker/datasources.sh datasources.sh
RUN chmod +x datasources.sh
    
CMD ["/bin/sh", "-c", "./datasources.sh && java -Dcat.home=/data/appdatas/cat -Dserver.port=8080 -jar /app/cat-boot.jar"]
