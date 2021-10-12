FROM openjdk:11-jre-slim

LABEL maintainer="Grégoire Rolland <gregoire.rolland@live.fr>"

ENV NGX_DISTRIBUTED_SHM_PORT=4321 \
    NGX_DISTRIBUTED_SHM_ADDRESS=0.0.0.0

COPY target/ngx-distributed-shm.jar /

STOPSIGNAL SIGRTMIN+3

EXPOSE 4321

ENTRYPOINT java -Dngx-distributed-shm.port=${NGX_DISTRIBUTED_SHM_PORT} -Dngx-distributed-shm.bind_address=${NGX_DISTRIBUTED_SHM_ADDRESS} -jar ngx-distributed-shm.jar
