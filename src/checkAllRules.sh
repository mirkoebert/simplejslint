#!/usr/bin/env bash
#set -x

if [[ $# -ne 2 ]]; then
	echo "usage: checkAllRules.sh inputFile outputFile"
	echo
	echo "where"
	echo "    inputFile : filename or directory path"
	echo "    outputFile : name of the output file for the results"
	echo
	exit
fi

inputFile=$1
outputFile=$2

echo -n "${inputFile}: "

src/countBytes.sh  $inputFile $outputFile

if [[ $inputFile == *.js ]]; then
	src/checkString.sh $inputFile evalCount eval "Potential Slow and Code Smell" $outputFile
	src/checkString.sh $inputFile jQueryFunctionCalls '$(' "JQuery Function - Potential Slow" $outputFile
	src/checkString.sh $inputFile jQueryLocatorCalls '$.' "JQuery Utility Function - Potential Slow" $outputFile
	src/checkString.sh $inputFile withCount with "Potential Slow" $outputFile
	src/checkString.sh $inputFile newCount new  "Potential Slow" $outputFile
	src/checkString.sh $inputFile documentWriteCount "document.write"  "Potential Slow" $outputFile

	src/checkRegex.sh  $inputFile forInCount "for\s+in" "Potential Slow" $outputFile
	src/checkRegex.sh  $inputFile returnNullCount "return\s+null" "Code Smell" $outputFile
fi

if [[ $inputFile == *.css ]]; then
	src/checkCssLint.sh $inputFile $outputFile
	src/checkString.sh $inputFile mediaQueryCount '@media' "Media Query" $outputFile
	src/checkRegex.sh $inputFile breakpointMCount '@media\s+[^{}]*\(min-width:\s*28em\)' "Breakpoint M" $outputFile
	src/checkRegex.sh $inputFile breakpointLCount '@media\s+[^{}]*\(min-width:\s*48em\)' "Breakpoint L" $outputFile
	src/checkRegex.sh $inputFile breakpointXLCount '@media\s+[^{}]*\(min-width:\s*62em\)' "Breakpoint XL" $outputFile
	src/countMultiLineRegexBytes.sh $inputFile breakpointMBytes '@media\s+[^{}]*\(min-width:\s*28em\)[^{}]*{([^{}]*{[^{}]*})*[^{}]*}' "Breakpoint M bytes" $outputFile
	src/countMultiLineRegexBytes.sh $inputFile breakpointLBytes '@media\s+[^{}]*\(min-width:\s*48em\)[^{}]*{([^{}]*{[^{}]*})*[^{}]*}' "Breakpoint L bytes" $outputFile
	src/countMultiLineRegexBytes.sh $inputFile breakpointXLBytes '@media\s+[^{}]*\(min-width:\s*62em\)[^{}]*{([^{}]*{[^{}]*})*[^{}]*}' "Breakpoint XL bytes" $outputFile
	src/countMultiLineRegexBytes.sh $inputFile mediaQueryBytes '@media\s+[^{}]*{([^{}]*{[^{}]*})*[^{}]*}' "Media Query bytes" $outputFile
fi

echo ""
