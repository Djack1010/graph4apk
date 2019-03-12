#!/bin/bash
SCRIPTPATH=$PWD

for Fold in $SCRIPTPATH/*; do
    if [ -d "${Fold}" ]; then
        cd $Fold
        for dotFile in $Fold/*.dot; do
            if [ -f $dotFile ]; then
		#remove file format (substring after last dot)
                OUTPUT=${dotFile%.*}
                if [ -f $OUTPUT.pdf ]; then
                    echo "Graph for $dotFile already exist!"
                    dot -Tpdf $dotFile -o $OUTPUT.pdf
                else
                    echo "Generating graph for $dotFile"
                    dot -Tpdf $dotFile -o $OUTPUT.pdf
                fi
            fi
        done
    fi
done
