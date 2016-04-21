#!/usr/bin/env bash

analyzeOneFile() {
    c=`fgrep -o ${1} ${2} | wc -l`
    d=`date`
    echo "$d, \"Count ${1}\", $c, $2" | tee -a log
}

#set -x  
dir=$1
str=$2
if [ -d "$dir" ]; then
    #d=`date`
    for filename in ${dir}*.js; do
        analyzeOneFile ${str} ${filename}
        #c=`fgrep -o ${str} ${filename} | wc -l`
        #echo "$d, \"Count ${str}\", $c, $filename" | tee -a log
    done
else
    analyzeOneFile ${str} ${dir}
    #echo "No dir"
fi

