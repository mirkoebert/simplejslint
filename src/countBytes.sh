#!/usr/bin/env bash

dir=$1

countLinesOfFile(){
    c=`cat ${1} | wc -l`
    d=`date`
    echo "$d, \"Count Bytes of Code\", $c, $1" | tee -a log
}

countBytesOfFile(){
    c=`cat ${1} | wc -c`
    d=`date`
    echo "$d, \"Count Bytes of Code\", $c, $1" | tee -a log
}

if [ -d "$dir" ]; then
    for filename in ${dir}*.js; do
        countBytesOfFile ${filename} 
        countLinesOfFile ${filename}
    done                
else                
    countBytesOfFile "${dir}"
    countLinesOfFile ${filename}
fi          
