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
    echo "-Xss MEMORY [allocate MEMORY MB to Soot stack memory]"
    echo "-compile [clean and recompile the project]"
    echo "-winFormat [output files windows format in <project_folder>/results_windows/]"
    echo "-apk <absolute_path_to_apk OR relative_path_from_APK_FOLDER>"
    echo "-apkFolder <absolute_path_to_a_folder_of_apk>"
    echo "-genJimple [generate Jimple code in <project_folder>/results/code/JimpleCode/]"
    exit
}

function convert_to_windows_format {
    if [ -n "${WINDOWS}" ]; then
        echo "CONVERTING OUTPUT FILES IN WINDOWS FORMAT"
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
    fi
}

function progrBar {
    #[##################################################] (100%)
    echo -e "\033[3A"
    PAR=$(($1-$(jobs | grep "Running" | wc -l)))
    TOT=$2
    PER=$(bc <<< "scale = 2; ($PAR / $TOT) * 100")
    TEMPPER=$( echo $PER | cut -d'.' -f1)
    COUNT=0
    echo "PROGRESS: $PAR out of $TOT - RUNNING: $(jobs | grep "Running" | wc -l)  "
    echo -ne "["
    while [ "$TEMPPER" -gt "0" ]; do
        TEMPPER=$(($TEMPPER-2))
        echo -ne "#"
        COUNT=$(($COUNT+1))
    done
    COUNT=$((50-$COUNT))
    for (( c=0; c<$COUNT; c++ )); do
        echo -ne "-"
    done
    echo -ne "] ($PER%)  "
    echo ""
}

if [ "$#" -eq 0 ]; then
    usage
else
    myArray=( "$@" )
    n=0
    while [ $n -lt $# ]; do
        if [[ "${myArray[$n]}" == "-apk" ]]; then
            n=$(($n+1))
            SINGLEAPK="${myArray[$n]}"
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "-apkFolder" ]]; then
            n=$(($n+1))
            APK_FOLDER="${myArray[$n]}"
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "-Xss" ]]; then
            n=$(($n+1))
            MEMORY="-Xss${myArray[$n]}m"
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "OK" ]]; then
            OK="OK"
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "-genJimple" ]]; then
            GENJIMPLE="-genJimple"
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "-compile" ]]; then
            COMPILE="TRUE"
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "-multithreads" ]]; then
            n=$(($n+1))
            MULTITHREADS="${myArray[$n]}"
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

RUN="$JAVAPATH/bin/java ${MEMORY} $ENCODING $CPATH createCG $ARGS $SOOTCP $ANDRJAR -projectPath ${PROJECT_PATH}"

echo "STARTING SCRIPT run.sh"
echo $RUN

if [ "${SINGLEAPK}" ]; then
  filename=$(echo ${SINGLEAPK##*/} | cut -d'.' -f 1)
  start=$(date +%s)
  echo "-----  ANALYZING $SINGLEAPK  -----"
  DYNAMICRUN="-process-dir $SINGLEAPK -SDGFileName $filename ${TARGETMETHOD} ${GENCCS} ${GENJIMPLE} ${SUBGRAPHMETHODS}"
  echo "RUNNING OPTION: ${DYNAMICRUN}"
  eval "$RUN $DYNAMICRUN"
  end=$(date +%s)
  runtime=$(($end-$start))
  echo "---> FINISHING $filename in $runtime sec"
  echo "--- EXECUTION INFO ---" >> $SCRIPTPATH/results/stats/$filename.txt
  echo "RUN: $RUN" >> $SCRIPTPATH/results/stats/$filename.txt
  echo "SOOT: $DYNAMICRUN" >> $SCRIPTPATH/results/stats/$filename.txt
  echo "TIME: $runtime sec" >> $SCRIPTPATH/results/stats/$filename.txt
  convert_to_windows_format
else
  FTOT=$(ls -lR $APK_FOLDER/ | grep ".apk$" | wc -l)
  FNOW=0
  echo ""
  echo ""
  for apkfile in $APK_FOLDER/**/*.apk; do
      filename=$(echo ${apkfile##*/} | cut -d'.' -f 1)
      family=$(basename $(dirname $apkfile))
      if [[ "$family" == "$APK_FOLDER" ]]; then
        family="NO_LABEL"
      fi
      if [ "${MULTITHREADS}" ]; then
        UPDATE=1
        DYNAMICRUN="-process-dir $apkfile -SDGFileName $filename -SDGLabel $family -silent ${TARGETMETHOD} ${GENCCS} ${GENJIMPLE} ${SUBGRAPHMETHODS}"

        # Skip analysis if folder exists (assuming, created in a previous run)
        if [ -f "./results/stats/${filename}.txt" ]; then
          FNOW=$(($FNOW+1))
          continue
        fi

        while [ "$(jobs | grep "Running" | wc -l)" -ge $bt "${MULTITHREADS}" ]; do
          progrBar $FNOW $FTOT
          sleep $UPDATE
        done
        eval "$RUN $DYNAMICRUN" &
        FNOW=$(($FNOW+1))
      else
        start=$(date +%s)
        echo "-----  ANALYZING $apkfile  -----"
        DYNAMICRUN="-process-dir $apkfile -SDGFileName $filename -SDGLabel $family ${TARGETMETHOD} ${GENCCS} ${GENJIMPLE} ${SUBGRAPHMETHODS}"
        echo "RUNNING OPTION: ${DYNAMICRUN}"
        eval "$RUN $DYNAMICRUN"
        end=$(date +%s)
        runtime=$(($end-$start))
        echo "---> FINISHING $filename in $runtime sec"
        echo "--- EXECUTION INFO ---" >> $SCRIPTPATH/results/stats/$filename.txt
        echo "RUN: $RUN" >> $SCRIPTPATH/results/stats/$filename.txt
        echo "SOOT: $DYNAMICRUN" >> $SCRIPTPATH/results/stats/$filename.txt
        echo "TIME: $runtime sec" >> $SCRIPTPATH/results/stats/$filename.txt
        convert_to_windows_format
      fi
  done

  if [ "${MULTITHREADS}" ]; then
    while [ "$(jobs | grep "Running" )" ]; do
      progrBar $FNOW $FTOT
      sleep $UPDATE
    done
  fi

fi

echo "FINISHING SCRIPT run.sh"