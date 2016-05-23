#!/usr/bin/env bash

if [[ $# -ne 2 ]]; then
    echo "usage: checkCSS.sh dir outputFile"
    echo
    echo "where"
    echo "    dir : filename to check"
    echo "    outputFile : name of the output file for the results"
    echo
    exit
fi

dir=$1
outputFile=$2

analyzeOneFile() {
	inputFile=$1
	csslint --quiet --ignore=box-sizing,adjoining-classes,compatible-vendor-prefixes,gradients,text-indent --format=compact  $inputFile > .csslint.tmp
	warnings=`cat .csslint.tmp | grep "Warning -" | wc -l`
	errors=`cat .csslint.tmp | grep "Error -" | wc -l`
	d=`date`
	inputFullPath="${inputFile}"
    inputFilename=${inputFullPath##*/}
    inputExtension=${inputFilename##*.}
    inputBasePath=${inputFullPath%$inputFilename}
    echo "$d, $inputFile, $inputBasePath, $inputFilename, $inputExtension, $warnings, \"CSS Warnings Count\"" | tee -a $outputFile
	echo "$d, $inputFile, $inputBasePath, $inputFilename, $inputExtension, $errors, \"CSS Errors Count\"" | tee -a $outputFile
}

if [ -d "$dir" ]; then
    for filename in ${dir}/*.css; do
        analyzeOneFile ${filename} $outputFile
    done
else
    analyzeOneFile "${dir}" $outputFile
fi

