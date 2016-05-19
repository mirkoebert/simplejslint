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
	regex="$1"
	inputFile=$2
	desc="$3"
    count=`fgrep -o -E "$regex" ${inputFile} | wc -l`
    d=`date`
    echo "$d, $inputFile, $count, \"Count Pattern $regex\", $desc" | tee -a $outputFile
}

if [ -d "$dir" ]; then
    for filename in ${dir}/*.js ${dir}/**/*.js; do
        analyzeOneFile "${regex}" ${filename} "$description" $outputFile
    done
else
    analyzeOneFile "${regex}" "${dir}" "$description" $outputFile
fi

