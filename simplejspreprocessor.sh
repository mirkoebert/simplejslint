#!/usr/bin/env bash
f=$1
cp $f tmp
f=tmp
version=$2


label=`grep -n "//#ifdef\s\s*\w" $f | awk '{print $2}' | sort | uniq`

for l in $label 
do
    #echo $l
    if [ "$l" == "$version" ] 
    then
        echo "Keep $l"
    else
        echo "Delete $l"
        ./simplejspreprocessor_cut.sh $f $l
        mv t.js $f
    fi
done


