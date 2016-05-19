#!/usr/bin/env bash

if [[ $# -ne 4 ]]; then
	echo "usage: checkString.sh dir str description outputFile"
	echo
	echo "where"
	echo "    dir : filename or directory path"
	echo "    str : string pattern that will be checked (counted)"
	echo "    description : describes, what the string pattern is looking for "
	echo "    outputFile : name of the output file for the results"
	echo
	exit
fi

description=$3
outputFile=$4

#set -x 
shopt -s nullglob
analyzeOneFile() {
    cmd=`fgrep -o "${1}" "${2}" | wc -l`
    d=`date`
    echo "$d, \"Count ${1}\", $cmd, $2, $description"  | tee -a $outputFile
}

#set -x  
dir=$1
str=$2
if [ -d "$dir" ]; then
    for filename in ${dir}*.js; do
        analyzeOneFile "${str}" ${filename} "$description" $outputFile
    done
else
    analyzeOneFile "${str}" "${dir}" "$description" $outputFile
fi

