#!/usr/bin/env bash

#set -x 

if [[ $# -ne 2 ]]; then
	echo "usage: checkAllFiles.sh dir outputFile"
	echo
	echo "where"
	echo "    dir : filename or directory path to check"
	echo "    outputFile : name of the output file for the results"
	echo
	exit
fi

dir=$1
outputFile=$2

if [ -d "$dir" ]; then
	for filename in $(find $dir -iname "*.js"); do src/checkAllRules.sh $filename $outputFile; done
	for filename in $(find $dir -iname "*.css"); do src/checkAllRules.sh $filename $outputFile; done
else
    src/checkAllRules.sh "${dir}" $outputFile
fi

