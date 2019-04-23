FROM java:8-jdk-alpine

COPY ./target/OrderManagement-0.0.1-SNAPSHOT.jar /usr/app/

WORKDIR /usr/app

RUN sh -c 'touch OrderManagement-0.0.1-SNAPSHOT.jar'

ENTRYPOINT ["java","-jar","OrderManagement-0.0.1-SNAPSHOT.jar"]  