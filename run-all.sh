#!/usr/bin/env bash
set -x
#IMPORTANT

#RUN THIS SCRIPT AS SUDO

SPADE_PATH=/home/wajih/SPADE

#Stop SPADE

${SPADE_PATH}/bin/spade stop

#Clean all the spade logs, audit logs

/bin/rm ${SPADE_PATH}/log/*

/bin/rm -f /var/log/audit/audit.log

/bin/rm -f /tmp/provenance.dot

/bin/rm -f /tmp/audit.log

sudo service auditd restart

${SPADE_PATH}/bin/spade start
sleep 1m
########### RUN YOUR CUSTOM COMMANDS HERE #########
COMMANDS="touch cat rm touch cat rm"
for cmd in ${COMMANDS}; do
    FINAL_CMD="${cmd} /home/wajih2/cont1.txt"
    FINAL_CMD2="${cmd} /home/newuser/cont2.txt"
    docker exec --user wajih2 67dbd13c0e74 ${FINAL_CMD}
    docker exec --user newuser 000955fb48bc ${FINAL_CMD2}
done

#Stop SPADE
#It takes some time to process
sleep 1m
${SPADE_PATH}/bin/spade stop
sleep 1m
FOLDER="$(date +"%d-%m-%Y")-exp"
mkdir -p ${SPADE_PATH}/experiments/${FOLDER}
mv /tmp/provenance.dot  ${SPADE_PATH}/experiments/${FOLDER}
mv /tmp/audit.log  ${SPADE_PATH}/experiments/${FOLDER}
mv /tmp/output.dot  ${SPADE_PATH}/experiments/${FOLDER}
dot -Tsvg -o ${SPADE_PATH}/experiments/${FOLDER}/output.svg  ${SPADE_PATH}/experiments/${FOLDER}/output.dot
transfer ${SPADE_PATH}/experiments/${FOLDER}/output.svg
