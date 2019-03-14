#!/bin/bash
SCRIPTPATH=$PWD

dotFile=$1
OUTPUT=${dotFile%.*}

if [ -f $OUTPUT.pdf ]; then
    exit
else
    dot -Tpdf $dotFile -o $OUTPUT.pdf
fi

exit
