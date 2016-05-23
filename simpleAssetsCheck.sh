#!/usr/bin/env bash

dir="."
outputFile="result.csv"

if [[ $# -ge 1 ]]; then
	dir=$1
fi

if [[ $# -ge 2 ]]; then
	outputFile=$2
fi

if [[ -f $outputFile ]]; then 
	rm $outputFile
fi

echo "timestamp,asset,count,metric,description" > $outputFile

src/checkAllFiles.sh $dir $outputFile


