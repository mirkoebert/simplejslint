#!/usr/bin/env bash

if [[ $# -ne 4 ]]; then
	echo "usage: checkRegex.sh dir regex description outputFile"
	echo
	echo "where"
	echo "    dir : filename or directory path"
	echo "    regex : regular expression pattern that will be checked (counted)"
	echo "    description : describes, what the regex pattern is looking for "
	echo "    outputFile : name of the output file for the results"
	echo
	exit
fi


dir=$1
regex="$2"
description="$3"
outputFile=$4

shopt -s nullglob
analyzeOneFile(){
    cmd=`fgrep -o -E "$1" ${2} | wc -l`
    d=`date`
    echo "$d, \"Count Pattern $1\", $cmd, $2" | tee -a $outputFile
}

if [ -d "$dir" ]; then
    for filename in ${dir}*.js; do
        analyzeOneFile "${regex}" ${filename} "$description" $outputFile
    done
else
    analyzeOneFile "${regex}" "${dir}" "$description" $outputFile
fi

