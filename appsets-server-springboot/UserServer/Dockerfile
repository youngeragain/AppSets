FROM amazoncorretto:17.0.8-al2023-headless
LABEL authors="12248"

WORKDIR home

RUN mkdir tom

WORKDIR tom

RUN mkdir -r services/springboot

CP UserServer-0.0.1-SNAPSHOT.jar /home/tom/services/springboot

EXPOSE 8084

WORKDIR services/springboot

ENTRYPOINT ["java", "-jar", "UserServer-0.0.1-SNAPSHOT.jar"]