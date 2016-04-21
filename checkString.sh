#!/usr/bin/env bash

set -x  
dir=$1
str=$2
if [ -d "$dir" ]; then
    d=`date`
    for filename in ${dir}*.js; do
        c=`fgrep -o ${str} ${filename} | wc -l`
        echo "$d, \"Count ${str}\", $c, $filename" | tee -a log
    done
else
    echo "No dir"
fi

