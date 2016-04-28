#!/usr/bin/env bash

dir=$1
src/checkString.sh $dir eval "Potential Slow and Code Smell"
src/checkString.sh $dir '$(' "JQuery Function, Potential Slow"
src/checkString.sh $dir '$.' "JQuery Utility Function, Potential Slow"
src/checkString.sh $dir with "Potential Slow"
src/checkString.sh $dir new  "Potential Slow"
src/checkString.sh $dir "document.write"  "Potential Slow"

src/checkRegex.sh  $dir "for\s+in" "Potential Slow"
src/checkRegex.sh  $dir "return\s+null" "Code Smell"

src/countBytes.sh  $dir

