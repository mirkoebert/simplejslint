#!/usr/bin/env bash

dir=$1
c=`fgrep -o -E "return\s*null" ${dir}*.js | wc -l`
d=`date`
echo "$d, \"Count Pattern return null\", $c" | tee -a log


