#!/bin/bash

dataFile=data;
shapeFile=shapes;
dataDir=LadderData/testData517;
shapeDir=LadderDomains/domainDescriptions/shapes;

ls ../../$dataDir > $dataFile
ls ../../$shapeDir | sed 's/Ladder.*Data\///' | sed 's/\.xml//' > $shapeFile;

echo "Difference between listings in $dataDir and $shapeDir"
echo 
echo "< means a difference in $dataDir"
echo "> means a difference in $shapeDir"
echo
echo

diff $dataFile $shapeFile | grep '[<>]'

rm $dataFile $shapeFile

