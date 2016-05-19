#!/usr/bin/env bash

if [[ $# -ne 2 ]]; then
    echo "usage: checkCSS.sh dir outputFile"
    echo
    echo "where"
    echo "    filename : filename to check"
    echo "    outputFile : name of the output file for the results"
    echo
    exit
fi

dir=$1
outputFile=$2

analyzeOneFile() {
	warnings=`csslint --quiet --ignore=box-sizing,adjoining-classes,compatible-vendor-prefixes,gradients,text-indent --format=compact  $filename | grep "Warning -" | wc -l`
	errors=`csslint --quiet --ignore=box-sizing,adjoining-classes,compatible-vendor-prefixes,gradients,text-indent --format=compact  $filename | grep "Error -" | wc -l`
	d=`date`
	echo "$d, \"CSS Warnings Count\", $warnings, $1" | tee -a $outputFile
	echo "$d, \"CSS Errors Count\", $errors, $1" | tee -a $outputFile
}

if [ -d "$dir" ]; then
    for filename in ${dir}*.css; do
        analyzeOneFile ${filename} $outputFile
    done
else
    analyzeOneFile "${dir}" $outputFile
fi

