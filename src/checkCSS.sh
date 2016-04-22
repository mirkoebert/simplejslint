#!/usr/bin/env bash
d=`date`
w=`csslint --quiet --ignore=box-sizing,adjoining-classes,compatible-vendor-prefixes,gradients,text-indent --format=compact  $1 | grep "Warning -" | wc -l`
e=`csslint --quiet --ignore=box-sizing,adjoining-classes,compatible-vendor-prefixes,gradients,text-indent --format=compact  $1 | grep "Error -" | wc -l`

echo "$d, \"CSS Warnings Count\", $w, $1" | tee -a log
echo "$d, \"CSS Errors Count\", $e, $1" | tee -a log


