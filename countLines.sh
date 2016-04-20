#!/usr/bin/env bash

dir=$1
c=`cat $ ${dir}*.js | wc -l`
d=`date`
echo "$d, \"Count Lines of Code\", $c" | tee -a log


