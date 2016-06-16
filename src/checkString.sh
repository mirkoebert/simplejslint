#!/usr/bin/env bash

#set -x 

if [[ $# -ne 5 ]]; then
	echo "usage: checkString.sh dir metric pattern description outputFile"
	echo
	echo "where"
	echo "    dir : filename or directory path"
	echo "    metric : metric name, that will be computed"
	echo "    pattern : string pattern, that will be checked (counted)"
	echo "    description : describes, what the metric means "
	echo "    outputFile : name of the output file for the results"
	echo
	exit
fi

: ${SEARCH_CMD:=fgrep}
# echo "SEARCH_CMD used: ${SEARCH_CMD}"

dir=$1
metric=$2
pattern=$3
description=$4
outputFile=$5

shopt -s nullglob
analyzeOneFile() {
	inputFile=$1
	metric="$2"
	pattern="$3"
	desc="$4"
	outputFile=$5
    count=`$SEARCH_CMD -F -o "${pattern}" "${inputFile}" | wc -l | tr -d '[[:space:]]'`
    d=`date`
    inputFullPath="${inputFile}"
    inputFilename=${inputFullPath##*/}
    inputExtension=${inputFilename##*.}
    inputBasePath=${inputFullPath%$inputFilename}
    echo "$d,$inputFile,$inputBasePath,$inputFilename,$inputExtension,$count,$metric,$description"  | tee -a $outputFile
}

if [ -d "$dir" ]; then
    for filename in ${dir}/*.js ${dir}/**/*.js; do
        analyzeOneFile ${filename} "${metric}" "${pattern}" "$description" $outputFile
    done
else
    analyzeOneFile "${dir}" "${metric}" "${pattern}" "$description" $outputFile
fi

