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
    inputFullPath="${inputFile}"
    inputFilename=${inputFullPath##*/}
    inputExtension=${inputFilename##*.}
    inputBasePath=${inputFullPath%$inputFilename}
    count=`cat ${inputFile} | wc -l | tr -d '[[:space:]]'`
    d=`date`
    echo "$d,$inputFile,$inputBasePath,$inputFilename,$inputExtension,$count,"loc",\"Count Lines of Code\"" | tee -a $outputFile
}

countBytesOfFile(){
    inputFile=$1
    inputFullPath="${inputFile}"
    inputFilename=${inputFullPath##*/}
    inputExtension=${inputFilename##*.}
    inputBasePath=${inputFullPath%$inputFilename}
    count=`cat ${inputFile} | wc -c | tr -d '[[:space:]]'`
    d=`date`
    echo "$d,$inputFile,$inputBasePath,$inputFilename,$inputExtension,$count,"bytes",\"Count Bytes of Code\"" | tee -a $outputFile
}

countBytesOfMinifiedFile(){
    inputFile=$1
    inputFullPath="${inputFile}"
    inputFilename=${inputFullPath##*/}
    inputExtension=${inputFilename##*.}
    inputBasePath=${inputFullPath%$inputFilename}
    d=`date`
    if [ $inputExtension == "js" ]; then
        count=`./node_modules/.bin/uglifyjs --mangle --compress -- ${inputFile} | wc -c | tr -d '[[:space:]]'`
        echo "$d,$inputFile,$inputBasePath,$inputFilename,$inputExtension,$count,"minBytes",\"Minified Code Size\"" | tee -a $outputFile
    elif [ $inputExtension == "css" ]; then
        count=`./node_modules/.bin/cleancss ${inputFile} | wc -c | tr -d '[[:space:]]'`
        echo "$d,$inputFile,$inputBasePath,$inputFilename,$inputExtension,$count,"minBytes",\"Minified Code Size\"" | tee -a $outputFile
    fi
}

countBytesOfMinifiedGzipFile(){
    inputFile=$1
    inputFullPath="${inputFile}"
    inputFilename=${inputFullPath##*/}
    inputExtension=${inputFilename##*.}
    inputBasePath=${inputFullPath%$inputFilename}
    d=`date`
    if [ $inputExtension == "js" ]; then
        count=`./node_modules/.bin/uglifyjs --mangle --compress -- ${inputFile} | gzip -9 -c | wc -c | tr -d '[[:space:]]'`
        echo "$d,$inputFile,$inputBasePath,$inputFilename,$inputExtension,$count,"minBytesGzip",\"Minified, gzipped Code Size\"" | tee -a $outputFile
    elif [ $inputExtension == "css" ]; then
        count=`./node_modules/.bin/cleancss ${inputFile} | gzip -9 -c | wc -c | tr -d '[[:space:]]'`
        echo "$d,$inputFile,$inputBasePath,$inputFilename,$inputExtension,$count,"minBytesGzip",\"Minified, gzipped Code Size\"" | tee -a $outputFile
    fi
}

shopt -s nullglob
if [ -d "$dir" ]; then
    for filename in ${dir}/*.js; do
        countLinesOfFile ${filename} $outputFile
        countBytesOfFile ${filename} $outputFile
        countBytesOfMinifiedFile ${filename} $outputFile
        countBytesOfMinifiedGzipFile ${filename} $outputFile
    done                
else                
    countLinesOfFile ${dir} $outputFile
    countBytesOfFile "${dir}" $outputFile
    countBytesOfMinifiedFile "${dir}" $outputFile
    countBytesOfMinifiedGzipFile "${dir}" $outputFile
fi          
