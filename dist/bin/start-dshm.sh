#!/bin/sh

PRG="$0"
PRGDIR=`dirname "$PRG"`
DSHM_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`
PID_FILE=$DSHM_HOME/bin/dshm_instance.pid

##
# Set you java home here
#
JAVA_HOME=/opt/jdk
##

RUN_JAVA=$JAVA_HOME/bin/java

#### minimum heap size
MIN_HEAP_SIZE=1G

#### maximum heap size
MAX_HEAP_SIZE=1G


if [ "x$MIN_HEAP_SIZE" != "x" ]; then
	JAVA_OPTS="$JAVA_OPTS -Xms${MIN_HEAP_SIZE}"
fi

if [ "x$MAX_HEAP_SIZE" != "x" ]; then
	JAVA_OPTS="$JAVA_OPTS -Xmx${MAX_HEAP_SIZE}"
fi


JAVA_OPTS="$JAVA_OPTS -XX:+UseNUMA -XX:+UseBiasedLocking -XX:+UseFastAccessorMethods -XX:+UnlockExperimentalVMOptions -XX:+UseCompressedOops -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+AggressiveOpts -XX:-UsePerfData -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000 -XX:+DisableExplicitGC"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9989 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"

JAVA_OPTS="$JAVA_OPTS -Dhazelcast.jmx=true"
JAVA_OPTS="$JAVA_OPTS -Dhazelcast.logging.type=none"
JAVA_OPTS="$JAVA_OPTS -Dngx-distributed-shm.port=12000"
JAVA_OPTS="$JAVA_OPTS -Dngx-distributed-shm.bind_address=127.0.0.1"
JAVA_OPTS="$JAVA_OPTS -Dngx-distributed-shm.log_dir=$DSHM_HOME/logs"


echo "########################################"
echo "# RUN_JAVA=$RUN_JAVA"
echo "# JAVA_OPTS=$JAVA_OPTS"
echo "# starting now...."
echo "########################################"

CLASSPATH=$DSHM_HOME/conf:$DSHM_HOME/lib/ngx-distributed-shm.jar

PID=$(cat "${PID_FILE}" )
if [ -z "${PID}" ]; then
    echo "Process ID for dshm instance is written to location: {$PID_FILE}"
    $RUN_JAVA -server $JAVA_OPTS -cp $CLASSPATH com.flutech.hcshm.Main &
    echo $! > ${PID_FILE}
else
    echo "Another dshm instance (PID=${PID}) is already started in this folder."
    exit 0
fi

