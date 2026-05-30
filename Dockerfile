FROM eclipse-temurin:25-jre AS builder
WORKDIR /builder
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM eclipse-temurin:25-jre
RUN groupadd --system appgroup \
    && useradd --system --gid appgroup --no-create-home appuser
WORKDIR /application
COPY --from=builder --chown=appuser:appgroup /builder/extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /builder/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /builder/extracted/application/ ./
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "application.jar"]
