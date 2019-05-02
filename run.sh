#!/bin/bash

SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
TIMESTAMP=$(date +%s)

if [ -f "${SCRIPTPATH}/scriptBash.config" ]; then
    source ${SCRIPTPATH}/scriptBash.config
else
    echo "scriptBash.config file not found, exiting..."
    exit
fi

if [ -z "$(${JAVAPATH}/bin/java -version 2>&1 >/dev/null | grep " version ")" ]; then 
    echo "java not found, check scriptBash.config! Exiting..."
    exit
fi

ENCODING="-Dfile.encoding=UTF-8" 
CPATH="-classpath ${JAVAPATH}/jre/lib/rt.jar:${PROJECT_PATH}/target/classes:${PROJECT_PATH}/lib/sootclasses-trunk-jar-with-dependencies.jar"
ARGS="-p cg all-reachable:true -no-bodies-for-excluded -full-resolver -w" 
SOOTCP="-cp .:${PROJECT_PATH}/src/main/resources/android-platforms/android-16/android.jar"
SOOTCP+=":${PROJECT_PATH}/src/main/resources/android-platforms/android-15/android.jar:${PROJECT_PATH}/src/main/resources/android-platforms/android-14/android.jar"
SOOTCP+=":${PROJECT_PATH}/src/main/resources/android-platforms/android-13/android.jar:${PROJECT_PATH}/src/main/resources/android-platforms/android-12/android.jar"
SOOTCP+=":${PROJECT_PATH}/src/main/resources/android-platforms/android-11/android.jar:${PROJECT_PATH}/src/main/resources/android-platforms/android-10/android.jar"
SOOTCP+=":${PROJECT_PATH}/src/main/resources/android-platforms/android-9/android.jar:${PROJECT_PATH}/src/main/resources/android-platforms/android-8/android.jar"
SOOTCP+=":${PROJECT_PATH}/src/main/resources/android-platforms/android-7/android.jar:${PROJECT_PATH}/src/main/resources/android-platforms/android-6/android.jar"
SOOTCP+=":${PROJECT_PATH}/src/main/resources/android-platforms/android-5/android.jar:${PROJECT_PATH}/src/main/resources/android-platforms/android-4/android.jar"
SOOTCP+=":${PROJECT_PATH}/src/main/resources/android-platforms/android-3/android.jar:${PROJECT_PATH}/src/main/resources/android-platforms/android-17/android.jar"
SOOTCP+=":${PROJECT_PATH}/src/main/resources/android-platforms/android-17/android-17-api.jar"
ANDRJAR="-android-jars ${PROJECT_PATH}/src/main/resources/android-platforms"

function usage {
    echo "USAGE: run.sh OK [OPTION ARG]"
    echo "LIST OF AVAILABLE OPTIONS:"
    echo "-targMeth TARGET_METHOD_NAME [Look for methods with similar name]"
    echo "-targMethEXACT TARGET_METHOD_NAME [Look for methods with that exact name]"
    exit
}

if [ "$#" -eq 0 ]; then
    usage
else
    myArray=( "$@" )
    n=0
    while [ $n -lt $# ]; do
        if [[ "${myArray[$n]}" == "-targMeth" ]]; then
            n=$(($n+1))
            TARGETMETHOD="-targetMethod ${myArray[$n]}"
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "-targMethEXACT" ]]; then
            n=$(($n+1))
            TARGETMETHOD="-targetMethodEXACT ${myArray[$n]}"
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "OK" ]]; then
            OK="OK"
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "-h" ]]; then
            usage
        else
            echo "INVALID INPUT: ${myArray[$n]}"
            usage
        fi       
    done
fi

if [ -z "${OK}" ]; then
    usage
fi

cd $SCRIPTPATH
if [ -z "$(which mvn)" ]; then
    echo "apache maven not found! Exiting..."
    exit
fi
mvn compile

RUN="$JAVAPATH/bin/java $ENCODING $CPATH $CLASSTORUN $ARGS $SOOTCP $ANDRJAR"

echo "STARTING SCRIPT run.sh"
echo $RUN
#eval "$RUN $APKDB/0ad370eab2ac647a932ad18fbb55d098.apk"
#exit
for apkfile in $APK_FOLDER/*.apk; do
    filename=$(echo ${apkfile##*/} | cut -d'.' -f 1)
    #echo $filename
    start=$(date +%s)
    echo "-----  ANALYZING $apkfile  -----"
    eval "$RUN -process-dir $apkfile -SDGFileName $filename ${TARGETMETHOD}"
    end=$(date +%s)
    runtime=$(($end-$start))
    echo "---> FINISHING $filename in $runtime sec"
done