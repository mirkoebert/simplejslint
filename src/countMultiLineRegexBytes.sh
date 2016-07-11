#!/usr/bin/env bash

if [[ $# -ne 5 ]]; then
	echo "usage: countMultiLineRegexBytes.sh dir metric regex description outputFile"
	echo
	echo "where"
	echo "    dir : filename or directory path"
	echo "    metric : metric name, that will be computed"
	echo "    regex : regular expression pattern that will be checked (bytes measured)"
	echo "    description : describes, what the metric means "
	echo "    outputFile : name of the output file for the results"
	echo
	exit
fi

: ${SEARCH_CMD:=egrep}
# echo "SEARCH_CMD used: ${SEARCH_CMD}"

dir=$1
metric="$2"
regex="$3"
description="$4"
outputFile=$5

shopt -s nullglob
analyzeOneFile(){
	inputFile=$1
	metric="$2"
	regex="$3"
	desc="$4"
	outputFile=$5
    count=`cat ${inputFile} | tr -d "\n" | $SEARCH_CMD -o "$regex" | wc -c | tr -d '[[:space:]]'`
    d=`date`
    inputFullPath="${inputFile}"
    inputFilename=${inputFullPath##*/}
    inputExtension=${inputFilename##*.}
    inputBasePath=${inputFullPath%$inputFilename}
    echo "$d,$inputFile,$inputBasePath,$inputFilename,$inputExtension,$count,$metric,$desc" >> $outputFile
    echo -n "."
}

if [ -d "$dir" ]; then
    for filename in ${dir}/*.js ${dir}/**/*.js; do
        analyzeOneFile ${filename} "$metric" "${regex}" "$description" $outputFile
    done
else
    analyzeOneFile "${dir}" "$metric" "${regex}" "$description" $outputFile
fi

