#!/usr/bin/env bash

dir=$1
c=`cat $ ${dir}*.js | wc -c`
d=`date`
echo "$d, \"Count Bytes of Code\", $c" | tee -a log


