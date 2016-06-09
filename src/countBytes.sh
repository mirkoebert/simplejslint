#!/usr/bin/env bash

if [[ $# -ne 2 ]]; then
    echo "usage: countBytes.sh dir outputFile"
    echo
    echo "where"
    echo "    dir : filename or directory path"
    echo "    outputFile : name of the output file for the results"
    echo
    exit
fi

dir=$1
outputFile=$2

countLinesOfFile(){
    inputFile=$1
    count=`cat ${inputFile} | wc -l | tr -d '[[:space:]]'`
    d=`date`
    inputFullPath="${inputFile}"
    inputFilename=${inputFullPath##*/}
    inputExtension=${inputFilename##*.}
    inputBasePath=${inputFullPath%$inputFilename}
    echo "$d,$inputFile,$inputBasePath,$inputFilename,$inputExtension,$count,"loc",\"Count Lines of Code\"" | tee -a $outputFile
}

countBytesOfFile(){
    inputFile=$1
    count=`cat ${inputFile} | wc -c | tr -d '[[:space:]]'`
    d=`date`
    inputFullPath="${inputFile}"
    inputFilename=${inputFullPath##*/}
    inputExtension=${inputFilename##*.}
    inputBasePath=${inputFullPath%$inputFilename}
    echo "$d,$inputFile,$inputBasePath,$inputFilename,$inputExtension,$count,"bytes",\"Count Bytes of Code\"" | tee -a $outputFile
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
