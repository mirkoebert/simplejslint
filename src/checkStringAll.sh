#!/usr/bin/env bash

dir=$1
src/checkString.sh $dir eval "Potential Slow and Code Smell" result.csv
src/checkString.sh $dir '$(' "JQuery Function, Potential Slow" result.csv
src/checkString.sh $dir '$.' "JQuery Utility Function, Potential Slow" result.csv
src/checkString.sh $dir with "Potential Slow" result.csv
src/checkString.sh $dir new  "Potential Slow" result.csv
src/checkString.sh $dir "document.write"  "Potential Slow" result.csv

src/checkRegex.sh  $dir "for\s+in" "Potential Slow" result.csv
src/checkRegex.sh  $dir "return\s+null" "Code Smell" result.csv

src/countBytes.sh  $dir "artefact size" result.csv

