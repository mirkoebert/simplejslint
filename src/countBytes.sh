#!/usr/bin/env bash

dir=$1

countBytesOfFile(){
    c=`cat ${1} | wc -c`
    d=`date`
    echo "$d, \"Count Bytes of Code\", $c, $1" | tee -a log
}

if [ -d "$dir" ]; then
    for filename in ${dir}*.js; do
        countBytesOfFile ${filename} 
    done                
else                
    countBytesOfFile "${dir}"
fi          
