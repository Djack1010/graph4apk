#!/bin/bash
SCRIPTPATH=$PWD

for Fold in $SCRIPTPATH/../*; do
    if [ -d "${Fold}" ]; then
        cd $Fold
        for dotFile in $Fold/*.dot; do
            if [ -f $dotFile ]; then
		        #remove file format (substring after last dot)
                OUTPUT=${dotFile%.*}
                if [ -f $OUTPUT.pdf ]; then
                    echo "Graph for $dotFile already exist!"
                else
                    echo "Generating graph for $dotFile"
                    #echo $dotFile >> $SCRIPTPATH/graphToPrint.txt
                    #dot -Tpdf $dotFile -o $OUTPUT.pdf
                    dot -Tps $dotFile -o $OUTPUT.ps
                fi
            fi
        done
    fi
done
