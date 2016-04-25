#!/usr/bin/env bash

dir=$1
str="$2"

shopt -s nullglob
analyzeOneFile(){
    c=`fgrep -o -E "$1" ${2} | wc -l`
    d=`date`
    echo "$d, \"Count Pattern $1\", $c, $2" | tee -a log
}

if [ -d "$dir" ]; then
    for filename in ${dir}*.js; do
        analyzeOneFile "${str}" ${filename} "$3"
    done
else
    analyzeOneFile "${str}" "${dir}" "$3"
fi

