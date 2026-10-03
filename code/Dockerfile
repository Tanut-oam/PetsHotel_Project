# ---------- ขั้นที่ 1: build ----------
FROM maven:3.9-eclipse-temurin-26 AS build
WORKDIR /workspace

# โหลด dependency ก่อน ถ้า pom.xml ไม่เปลี่ยน Docker จะใช้ cache ของชั้นนี้ build รอบหลังเร็วขึ้น
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# test รันแยกด้วย mvnw test (repository test ต้องใช้ DB จริง จึงไม่รันตอน build image)
RUN mvn -B -q package -DskipTests

# ---------- ขั้นที่ 2: runtime ----------
FROM eclipse-temurin:26-jre
WORKDIR /app

# ไม่รันแอปด้วย root
RUN groupadd --system app && useradd --system --gid app app \
    && mkdir -p /app/uploads && chown -R app:app /app

COPY --from=build /workspace/target/*.jar app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]