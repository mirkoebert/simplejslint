#!/usr/bin/env bash

dir=$1
src/checkString.sh $dir eval
src/checkString.sh $dir $
src/checkString.sh $dir with
src/checkString.sh $dir new

src/checkRegex.sh  $dir "for\s+in"
src/checkRegex.sh  $dir "return\s+null"

