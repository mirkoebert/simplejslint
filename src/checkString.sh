#!/usr/bin/env bash

#set -x 

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

shopt -s nullglob
analyzeOneFile() {
	pattern="$1"
	inputFile=$2
	desc="$3"
    count=`fgrep -o "${pattern}" "${inputFile}" | wc -l`
    d=`date`
    inputFullPath="${inputFile}"
    inputFilename=${inputFullPath##*/}
    inputExtension=${inputFilename##*.}
    inputBasePath=${inputFullPath%$inputFilename}
    echo "$d, $inputFile, $inputBasePath, $inputFilename, $inputExtension, $count, \"Count ${pattern}\", $description"  | tee -a $outputFile
}

dir=$1
str=$2
if [ -d "$dir" ]; then
    for filename in ${dir}/*.js ${dir}/**/*.js; do
        analyzeOneFile "${str}" ${filename} "$description" $outputFile
    done
else
    analyzeOneFile "${str}" "${dir}" "$description" $outputFile
fi

