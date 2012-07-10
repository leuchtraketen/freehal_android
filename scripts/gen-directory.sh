#!/bin/bash

for x in . ../android-libs/ActionBarSherlock
do
cd $x

rm -rf _gen
mv -f gen _gen
svn up gen
cd _gen
find -name .svn -exec rm -rf '{}' \;
find -type d -exec mkdir -p ../gen/'{}' \;
find -type f -exec cp '{}' ../gen/'{}' \;
cd ..
rm -rf _gen

done
