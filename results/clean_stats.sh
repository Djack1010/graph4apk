#!/bin/bash
SCRIPTPATH=$PWD

for stat in ${SCRIPTPATH}/stats/*.txt; do
    if [ -f $stat ]; then
        echo "Removing ${stat}"
        rm $stat
    fi
done
	    
