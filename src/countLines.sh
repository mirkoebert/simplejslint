#!/usr/bin/env bash

count (){
    c=`cat $ ${1} | wc -l`
    d=`date`
    echo "$d, \"Count Lines of Code\", $c, $1" | tee -a log
}

dir=$1

if [ -d "$dir" ]; then
    for filename in ${dir}*.js; do
        count ${filename}
    done
else
    count  ${dir}
fi  


