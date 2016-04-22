#!/usr/bin/env bash

#set -x  
analyzeOneFile() {
    c=`fgrep -o "${1}" "${2}" | wc -l`
    d=`date`
    echo "$d, \"Count ${1}\", $c, $2" | tee -a log
}

#set -x  
dir=$1
str=$2
if [ -d "$dir" ]; then
    for filename in ${dir}*.js; do
        analyzeOneFile "${str}" ${filename}
    done
else
    analyzeOneFile "${str}" "${dir}"
fi

