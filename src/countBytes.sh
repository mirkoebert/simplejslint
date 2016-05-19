#!/usr/bin/env bash

if [[ $# -ne 3 ]]; then
    echo "usage: countBytes.sh dir description outputFile"
    echo
    echo "where"
    echo "    dir : filename or directory path"
    echo "    description : describes, what the check is looking for "
    echo "    outputFile : name of the output file for the results"
    echo
    exit
fi

description=$2
outputFile=$3
dir=$1

countLinesOfFile(){
    inputFile=$1
    count=`cat ${inputFile} | wc -l`
    d=`date`
    echo "$d, $inputFile, $count, \"Count Lines of Code\"" | tee -a $outputFile
}

countBytesOfFile(){
    inputFile=$1
    count=`cat ${inputFile} | wc -c`
    d=`date`
    echo "$d, $inputFile, $count, \"Count Bytes of Code\"" | tee -a $outputFile
}

shopt -s nullglob
if [ -d "$dir" ]; then
    for filename in ${dir}/*.js; do
        countBytesOfFile ${filename} $outputFile
        countLinesOfFile ${filename} $outputFile
    done                
else                
    countBytesOfFile "${dir}" $outputFile
    countLinesOfFile ${dir} $outputFile
fi          
