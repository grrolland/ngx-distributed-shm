FROM maven:3.6.3-jdk-11 AS build

RUN git clone https://github.com/revomatico/ngx-distributed-shm
RUN cd ngx-distributed-shm \
    && mvn clean package

FROM openjdk:11-jre-slim

LABEL maintainer="Cristian Chiru <cristian.chiru@revomatico.com>"

ENV NGX_DISTRIBUTED_SHM_PORT=4321 \
    NGX_DISTRIBUTED_SHM_ADDRESS=0.0.0.0

COPY --from=build /ngx-distributed-shm/target/ngx-distributed-shm.jar /

STOPSIGNAL SIGRTMIN+3

EXPOSE 4321

ENTRYPOINT java -Dngx-distributed-shm.port=${NGX_DISTRIBUTED_SHM_PORT} -Dngx-distributed-shm.bind_address=${NGX_DISTRIBUTED_SHM_ADDRESS} -jar ngx-distributed-shm.jar
