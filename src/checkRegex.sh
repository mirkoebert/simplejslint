#!/usr/bin/env bash

dir=$1
str="$2"
echo "A: "$str
c=`fgrep -o -E "${str}" ${dir}*.js | wc -l`
#c=`fgrep -o -E "return\s+null" ${dir}*.js | wc -l`
d=`date`
echo "$d, \"Count Pattern ${str}\", $c" | tee -a log


