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

: ${SEARCH_CMD:=fgrep}
# echo "SEARCH_CMD used: ${SEARCH_CMD}"

dir=$1
outputFile=$2

analyzeOneFile() {
	inputFile=$1
	csslint --quiet --ignore=box-sizing,adjoining-classes,compatible-vendor-prefixes,gradients,text-indent --format=compact  $inputFile > .csslint.tmp
	warnings=`cat .csslint.tmp | $SEARCH_CMD -F "Warning -" | wc -l | tr -d '[[:space:]]'`
	errors=`cat .csslint.tmp | $SEARCH_CMD -F "Error -" | wc -l | tr -d '[[:space:]]'`
	d=`date`
	inputFullPath="${inputFile}"
    inputFilename=${inputFullPath##*/}
    inputExtension=${inputFilename##*.}
    inputBasePath=${inputFullPath%$inputFilename}
    echo "$d,$inputFile,$inputBasePath,$inputFilename,$inputExtension,$warnings,"cssWarnings",\"CSS Warnings Count\"" >> $outputFile
    echo -n "."
	echo "$d,$inputFile,$inputBasePath,$inputFilename,$inputExtension,$errors,"cssErrors",\"CSS Errors Count\"" >> $outputFile
    echo -n "."
}

if [ -d "$dir" ]; then
    for filename in ${dir}/*.css; do
        analyzeOneFile ${filename} $outputFile
    done
else
    analyzeOneFile "${dir}" $outputFile
fi

