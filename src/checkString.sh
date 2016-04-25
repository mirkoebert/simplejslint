#!/usr/bin/env bash

#set -x 
shopt -s nullglob
analyzeOneFile() {
    c=`fgrep -o "${1}" "${2}" | wc -l`
    d=`date`
    echo "$d, \"Count ${1}\", $c, $2, $3"  | tee -a log
}

#set -x  
dir=$1
str=$2
if [ -d "$dir" ]; then
    for filename in ${dir}*.js; do
        analyzeOneFile "${str}" ${filename} "$3"
    done
else
    analyzeOneFile "${str}" "${dir}" "$3"
fi

