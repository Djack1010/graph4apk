#!/bin/bash
SCRIPTPATH=$PWD

for Fold in $SCRIPTPATH/*; do
    if [ -d "${Fold}" ]; then
        cd $Fold
        for dotFile in $Fold/*.pdf; do
            if [ -f $dotFile ]; then
		echo "Removing ${dotFile}"
		rm $dotFile
            fi
        done
    fi
done
