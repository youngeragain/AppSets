FROM amazoncorretto:17.0.8-al2023-headless
LABEL authors="xcj"

WORKDIR home

RUN mkdir tom

WORKDIR tom

RUN mkdir -p services/springboot

COPY MainServer-0.0.1-SNAPSHOT.jar /home/tom/services/springboot

EXPOSE 8084

WORKDIR services/springboot

ENTRYPOINT ["java", "-jar", "MainServer-0.0.1-SNAPSHOT.jar"]