#!/usr/bin/env bash

#set -x 

if [[ $# -ne 3 ]]; then
	echo "usage: checkAllFiles.sh type dir outputFile"
	echo
	echo "where"
	echo "    type : one of the following options: -all | -js | -css"
	echo "    dir : filename or directory path to check"
	echo "    outputFile : name of the output file for the results"
	echo
	exit
fi

type=$1
dir=$2
outputFile=$3

if [ -d "$dir" ]; then
	if [[ ($type == "-all") || ($type == "-js") ]]; then
		for filename in $(find $dir -iname "*.js" | egrep "([^/]+/){3}[0-9a-f]{16}/.*"); do 
			src/checkAllRules.sh $filename $outputFile 
		done
	fi
	if [[ ($type == "-all") || ($type == "-css") ]]; then
		for filename in $(find $dir -iname "*.css" | egrep "([^/]+/){3}[0-9a-f]{16}/.*"); do 
			src/checkAllRules.sh $filename $outputFile
		done
	fi
else
    src/checkAllRules.sh "${dir}" $outputFile
fi

