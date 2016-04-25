#!/usr/bin/env bash
f=$1
version=$2

a=`grep -n "//#ifdef $version" $f | awk -F: '{print $1}'`

if [[ -n "$a" ]]; then
    z=`grep -n "//#endif" $f | awk -F: '{print $1}'`
    t=`cat $1 | wc -l`
    echo "Preprocessor from $a to $z Total lines: $t"


    x=$((t-z))
    withinclude=${f}_wi.js
    withoutinclude=${f}_wo.js

    head -n $a $f >  $withoutinclude
    tail -n $x $f >> $withoutinclude
else
    echo "Warning not precompier tag found: $2"
fi

