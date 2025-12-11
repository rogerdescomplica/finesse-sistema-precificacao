# escape=`
# Base: OpenJDK 25 RC on Windows Server Core LTSC 2025 (as requested)
FROM openjdk:25-rc-windowsservercore-ltsc2025

SHELL ["powershell", "-Command"]

# Directories
ENV APP_HOME="C:\\app" `
    APP_LOGS="C:\\app\\logs" `
    APP_CONFIG="C:\\app\\config" `
    SERVER_PORT=8080 `
    SPRING_PROFILES_ACTIVE=prod

RUN New-Item -ItemType Directory -Path $env:APP_HOME,$env:APP_LOGS,$env:APP_CONFIG | Out-Null

# Security: run as ContainerUser (non-admin)
USER ContainerUser

# Performance JVM options for JAR runtime
ENV JAVA_TOOL_OPTIONS="-XX:+UnlockExperimentalVMOptions -XX:+UseStringDeduplication -XX:+AlwaysActAsServerClassMachine -XX:+TieredCompilation -XX:MaxRAMPercentage=75"

# Copy application artifacts (provide either native exe or jar at build time)
# Default: JAR runtime
ARG JAR_FILE=target\\finesse-backend-1.0.0.jar
COPY ${JAR_FILE} C:\\app\\app.jar

# Optional: Native executable (copy when building native image externally)
# ARG NATIVE_EXE=target\\finesse-backend.exe
# COPY ${NATIVE_EXE} C:\\app\\finesse-backend.exe

EXPOSE 8080
VOLUME ["C:\\app\\logs", "C:\\app\\config"]

# Healthcheck via Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 CMD `
  try { $r = Invoke-WebRequest -UseBasicParsing http://localhost:$env:SERVER_PORT/actuator/health ; if ($r.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }

# Entrypoint: prefer native if present; otherwise run JAR
ENTRYPOINT ["powershell", "-Command", "if (Test-Path 'C:\\app\\finesse-backend.exe') { & 'C:\\app\\finesse-backend.exe' } else { & java -Xms256m -Xmx512m -XX:+UseSerialGC -jar 'C:\\app\\app.jar' }" ]

# Notes:
# - To use native executable, build it outside the container with GraalVM and MSVC Build Tools,
#   then pass ARG NATIVE_EXE at docker build time and remove/correct the JAR copy.