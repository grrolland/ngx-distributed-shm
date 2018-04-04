#!/bin/sh
PRG="$0"
PRGDIR=`dirname "$PRG"`
DSHM_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`
PID_FILE="${DSHM_HOME}"/bin/dshm_instance.pid

echo $PID_FILE

if [ ! -f "${PID_FILE}" ]; then
    echo "No dshm instance is running."
    exit 0
fi

PID=$(cat "${PID_FILE}");
if [ -z "${PID}" ]; then
    echo "No dshm instance is running."
    exit 0
else
   kill -15 "${PID}"
   rm "${PID_FILE}"
   echo "dshm Instance with PID ${PID} shutdown."
   exit 0
fi

