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
    echo "-compile [clean and recompile the project]"
    echo "-winFormat [output files windows format in <project_folder>/results_windows/]"
    echo "-targMeth TARGET_METHOD_NAME [Look for methods with similar name]"
    echo "-targMethEXACT TARGET_METHOD_NAME [Look for methods with that exact name]"
    echo "-apk <absolute_path_to_apk OR relative_path_from_APK_FOLDER>"
    echo "-genCCS [generate CCS file in <project_folder>/results/graphs/CCS/]"
    echo "-genJimple [generate Jimple code in <project_folder>/results/code/JimpleCode/]"
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
        elif [[ "${myArray[$n]}" == "-apk" ]]; then
            n=$(($n+1))
            SINGLEAPK="${myArray[$n]}"
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "OK" ]]; then
            OK="OK"
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "-genCCS" ]]; then
            GENCCS="-genCCS"
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "-genJimple" ]]; then
            GENJIMPLE="-genJimple"
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "-compile" ]]; then
            COMPILE="TRUE"
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "-winFormat" ]]; then
            WINDOWS="TRUE"
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

if [ "${SINGLEAPK}" ]; then
    if ! [ -f ${SINGLEAPK} ]; then
        if ! [ -f $APK_FOLDER/${SINGLEAPK} ]; then
            echo "APK not found! Exiting..."
            exit
        else
            SINGLEAPK="${APK_FOLDER}/${SINGLEAPK}"
        fi
    fi
fi

if [ -n "${COMPILE}" ]; then
    mvn clean
    mvn compile
fi

RUN="$JAVAPATH/bin/java $ENCODING $CPATH $CLASSTORUN $ARGS $SOOTCP $ANDRJAR -projectPath ${PROJECT_PATH}"

echo "STARTING SCRIPT run.sh"
echo $RUN

if [ "${SINGLEAPK}" ]; then
    filename=$(echo ${SINGLEAPK##*/} | cut -d'.' -f 1)
    start=$(date +%s)
    echo "-----  ANALYZING $SINGLEAPK  -----"
    DYNAMICRUN="-process-dir $SINGLEAPK -SDGFileName $filename ${TARGETMETHOD} ${GENCCS} ${GENJIMPLE}"
    echo "RUNNING OPTION: ${DYNAMICRUN}"
    eval "$RUN $DYNAMICRUN"
    end=$(date +%s)
    runtime=$(($end-$start))
    echo "---> FINISHING $filename in $runtime sec"

else
    for apkfile in $APK_FOLDER/*.apk; do
        filename=$(echo ${apkfile##*/} | cut -d'.' -f 1)
        start=$(date +%s)
        echo "-----  ANALYZING $apkfile  -----"
        DYNAMICRUN="-process-dir $apkfile -SDGFileName $filename ${TARGETMETHOD} ${GENCCS} ${GENJIMPLE}"
        echo "RUNNING OPTION: ${DYNAMICRUN}"
        eval "$RUN $DYNAMICRUN"
        end=$(date +%s)
        runtime=$(($end-$start))
        echo "---> FINISHING $filename in $runtime sec"
    done
    
fi

if [ -n "${WINDOWS}" ]; then
    echo "CONVERTING OUTPUT FILES IN WINDOWS FORMAT"
    filename=$(echo ${SINGLEAPK##*/} | cut -d'.' -f 1)
    mkdir -p ${SCRIPTPATH}/results_windows
    if [ -n "${GENJIMPLE}" ]; then
        mkdir -p ${SCRIPTPATH}/results_windows/code/JimpleCode
        if [ -d "${SCRIPTPATH}/results_windows/code/JimpleCode/$filename" ]; then
            rm -rd "${SCRIPTPATH}/results_windows/code/JimpleCode/$filename"
        fi
        cp -r ${SCRIPTPATH}/results/code/JimpleCode/$filename ${SCRIPTPATH}/results_windows/code/JimpleCode
        for jimple_file in ${SCRIPTPATH}/results_windows/code/JimpleCode/$filename/*.jimple; do
            unix2dos -q $jimple_file
        done
    fi
    if [ -n "${GENCCS}" ]; then
        mkdir -p ${SCRIPTPATH}/results_windows/graphs/CCS
        CCSPATH="${SCRIPTPATH}/results_windows/graphs/CCS/$filename"
        if [ -d "$CCSPATH" ]; then
            rm -rd "$CCSPATH"
        fi
        cp -r ${SCRIPTPATH}/results/graphs/CCS/$filename $CCSPATH
        unix2dos -q ${CCSPATH}/$filename.ccs
        unix2dos -q ${CCSPATH}/$filename.txt
        for cpdg_file in ${CCSPATH}/CPDG/*.ccs; do
            unix2dos -q $cpdg_file
        done
    fi
fi

echo "FINISHING SCRIPT run.sh"