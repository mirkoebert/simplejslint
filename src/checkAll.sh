#!/usr/bin/env bash

if [[ $# -ne 2 ]]; then
	echo "usage: checkAll.sh dir outputFile"
	echo
	echo "where"
	echo "    dir : filename or directory path"
	echo "    outputFile : name of the output file for the results"
	echo
	exit
fi

dir=$1
outputFile=$2

src/checkString.sh $dir eval "Potential Slow and Code Smell" $outputFile
src/checkString.sh $dir '$(' "JQuery Function, Potential Slow" $outputFile
src/checkString.sh $dir '$.' "JQuery Utility Function, Potential Slow" $outputFile
src/checkString.sh $dir with "Potential Slow" $outputFile
src/checkString.sh $dir new  "Potential Slow" $outputFile
src/checkString.sh $dir "document.write"  "Potential Slow" $outputFile

src/checkRegex.sh  $dir "for\s+in" "Potential Slow" $outputFile
src/checkRegex.sh  $dir "return\s+null" "Code Smell" $outputFile

src/countBytes.sh  $dir "artefact size" $outputFile

src/checkCSS.sh    $dir $outputFile