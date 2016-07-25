#!/usr/bin/env bash

type="-all"
dir="."
outputFile="result.csv"

if [[ $# -ge 1 ]]; then
	type=$1
fi

if [[ $# -ge 2 ]]; then
	dir=$2
fi

if [[ $# -ge 3 ]]; then
	outputFile=$3
fi

# if [[ -f $outputFile ]]; then 
# 	rm $outputFile
# fi

echo "timestamp,asset,basePath,filename,extension,count,metric,description" > $outputFile

src/checkAllFiles.sh $type $dir $outputFile


