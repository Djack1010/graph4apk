#!/bin/bash

SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
TIMESTAMP=$(date +%s)

if [ -f "../scriptBash.config" ]; then
    source ../scriptBash.config
else
    echo "scriptBash.config file not found, exiting..."
    exit
fi

cd ${PROJECT_PATH}
mvn compile

for apkfile in ${PROJECT_PATH}/apk_db/${APK_FOLDER}/*.apk; do
    filename=$(echo ${apkfile##*/} | cut -d'.' -f 1)
    echo "-process-dir $apkfile -SDGFileName $filename" >> ${SCRIPTPATH}/inputFile${TIMESTAMP}.txt
done