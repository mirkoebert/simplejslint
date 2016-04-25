#!/usr/bin/env bash
f=$1
cp $f tmp
f=tmp
version=$2


count=`grep -n "//#ifdef $version" $f | wc -l`
echo "Incude count: $count"

withoutinclude=t.js

#for i in {1..$count}
while [ $count -gt 0 ]
do
    echo "Loop $count"
    a=`grep -n "//#ifdef $version" $f | head -n 1 |awk -F: '{print $1}'`
    a=$((a-1))
    #echo "AAA: $a"
    t=`cat $f | wc -l`
    z=`grep -n "//#endif" $f | head -n 1 | awk -F: '{print $1}'`
    echo "Preprocessor from line $a to line $z, total lines: $t"
    x=$((t-z))

    head -n $a $f >  $withoutinclude
    tail -n $x $f >> $withoutinclude
    cp $withoutinclude $f

    count=$(( $count - 1 ))
done


#a=`grep -n "//#ifdef $version" $f | awk -F: '{print $1}'`

#if [[ -n "$a" ]]; then
#    z=`grep -n "//#endif" $f | awk -F: '{print $1}'`
#    t=`cat $1 | wc -l`
#    echo "Preprocessor from $a to $z Total lines: $t"


#    x=$((t-z))
#    withinclude=${f}_wi.js
#    withoutinclude=${f}_wo.js

#   head -n $a $f >  $withoutinclude
#   tail -n $x $f >> $withoutinclude
#else
#    echo "Warning not precompier tag found: $2"
#fi

