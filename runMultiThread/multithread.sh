#!/bin/bash

SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
PIDRUN=$$

function usage {
    echo "USAGE: multithread.sh -sp SCRIPT_PATH -i INPUT_TEXT_FILE_PATH [OPTION ARG]"
    echo "LIST OF AVAILABLE OPTIONS:"
    echo "-m NUM_MAX_JOBS [DEFAULT = 2]"
    echo "-u UPDATE_TIME [DEFAULT = 0.5]"
    echo "-sl SLEEP_SEC [DEFAULT = 0] -> sl sec between one thread to the next one"
    exit
}

if [ "$#" -eq 0 ]; then
    usage
else
    myArray=( "$@" )
    n=0
    while [ $n -lt $# ]; do
        if [[ "${myArray[$n]}" == "-i" ]]; then
            n=$(($n+1))
            INPUTTEXTFILE=${myArray[$n]}
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "-sp" ]]; then
            n=$(($n+1))
            INPUTSCRIPT=${myArray[$n]}
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "-m" ]]; then
            n=$(($n+1))
            MAXJOBS=${myArray[$n]}
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "-u" ]]; then
            n=$(($n+1))
            UPDATE=${myArray[$n]}
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "-sl" ]]; then
            n=$(($n+1))
            SLEEP_SEC=${myArray[$n]}
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "-h" ]]; then
            usage
        else
            echo "INVALID INPUT: ${myArray[$n]}"
            usage
        fi       
    done
fi

if [ -z "$INPUTTEXTFILE" ] || [ -z "$INPUTSCRIPT" ]; then
    echo "ERROR, set arguments..."
    usage
fi
if [ ! -f $INPUTTEXTFILE ]; then
    echo "ERROR! Insert valid Input Text file, exiting..."
    exit
elif [ -z "$(cat $INPUTTEXTFILE | grep "\$")" ]; then
    echo "ERROR! The Input Text file contains invalid characters (\$), exiting..."
    exit
fi
if [ ! -f $INPUTSCRIPT ]; then
    echo "ERROR! Insert valid Script to run, exiting..."
    exit
else
    INPUTSCRIPT=$(realpath $INPUTSCRIPT)
fi
if [ -z "$MAXJOBS" ]; then
    MAXJOBS=2
fi
if [ -z "$UPDATE" ]; then
    UPDATE=0.5
fi
if [ -z "$SLEEP_SEC" ]; then
    SLEEP_SEC=0
fi

#PIDRUN=$$
# echo -e "\033[2A"
function progrBar {
    #[##################################################] (100%)
    echo -e "\033[3A"
    PAR=$(($1-$(jobs | grep "Running" | wc -l)))
    TOT=$2
    PER=$(bc <<< "scale = 2; ($PAR / $TOT) * 100")
    TEMPPER=$( echo $PER | cut -d'.' -f1)
    COUNT=0
    echo "PROGRESS: $PAR out of $TOT - RUNNING: $(jobs | grep "Running" | wc -l)"
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
    echo -ne "] ($PER%)"
    echo ""
}

FTOT=$(cat $INPUTTEXTFILE | wc -l)
FNOW=0

echo "STARTING with MAXJOBS: $MAXJOBS and UPDATE: $UPDATE"
echo "INPUTS: $INPUTTEXTFILE"
echo "SCRIPT: $INPUTSCRIPT"
echo -e "\n" #for progrBar

while IFS='' read -r line || [[ -n "$line" ]]; do
    while true; do
        if [ "$(jobs | grep "Running" | wc -l)" -lt "$MAXJOBS" ]; then
            $INPUTSCRIPT ${line} &
            sleep $SLEEP_SEC
            break
        else
            progrBar $FNOW $FTOT
            sleep $UPDATE
        fi
    done
    FNOW=$(($FNOW+1))
    progrBar $FNOW $FTOT
done < "$INPUTTEXTFILE"
while [ "$(jobs | grep "Running" )" ]; do
    progrBar $FNOW $FTOT
    sleep $UPDATE
done

exit