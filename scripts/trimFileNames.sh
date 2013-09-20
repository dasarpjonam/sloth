#! /bin/bash

# This script trims down names of files in test data directories. The problem is that some file systems can't handle
# the really long filenames (*ahem* windows)

testDataDir=../../LadderData/testData517
trimThreshold=30

for dir in `ls $testDataDir`
do
    nameLength=${#dir}

    if [[ $nameLength > $trimThreshold ]]
    then
        for file in `ls $testDataDir/$dir`
        do
            newFile
#_Text_blah
#_AA_blah
        done
    fi
done

