FROM openjdk:11-jre-slim

LABEL maintainer="Grégoire Rolland <gregoire.rolland@live.fr>"

ENV NGX_DISTRIBUTED_SHM_PORT=4321 \
    NGX_DISTRIBUTED_SHM_ADDRESS=0.0.0.0 \
    JDK_JAVA_OPTIONS="--add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED"

COPY target/ngx-distributed-shm.jar /

STOPSIGNAL SIGRTMIN+3

EXPOSE 4321

ENTRYPOINT java -Dhazelcast.shutdownhook.policy=GRACEFUL \
                -Dhazelcast.graceful.shutdown.max.wait=20 \
                -Dngx-distributed-shm.port=${NGX_DISTRIBUTED_SHM_PORT} \
                -Dngx-distributed-shm.bind_address=${NGX_DISTRIBUTED_SHM_ADDRESS} \
                -jar ngx-distributed-shm.jar
