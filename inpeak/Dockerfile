FROM amazoncorretto:21-alpine3.18

WORKDIR /app

COPY . .

RUN ./gradlew clean build -x test

CMD ["java", "-jar", "build/libs/inpeak-backend.jar"]
