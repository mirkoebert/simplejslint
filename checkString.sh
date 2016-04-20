#!/usr/bin/env bash

dir=$1
str=$2
c=`fgrep -o ${str} ${dir}*.js | wc -l`
d=`date`
echo "$d, \"Count ${str}\", $c" | tee -a log


