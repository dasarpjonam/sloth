# !/bin/bash
###############################################################################
#
# postBuildTest
#
###############################################################################
#
# This shell script should be run on a build to test basic functionality of a software drop.
#
###############################################################################
# Revision History
# ---------------------------
# Feb 28, 2009    Joshua Johnston    Original
###############################################################################

jarFile=`ls tamuDeepGreen*.jar`
testResults=dropTestResults.txt

echo "Post test build, jar $jarFile" > $testResults
date >> $testResults

# javadoc present
echo "Javadoc present?"
numApiFiles=`ls api | wc | sed 's/  */:/g' | cut -d : -f 2`
if [[ $numApiFiles > 0 ]]
then
    echo '     YES'
    echo 'Javadoc present' >> $testResults
else
    echo '     NO'
    echo '*********************************' >> $testResults
    echo '**  Javadoc Missing            **' >> $testResults
    echo '*********************************' >> $testResults
fi
echo >> $testResults
echo >> $testResults

# domain compiler -- includes SIDC and control point check
java -cp $jarFile test.unit.deepgreen.ControlPointCheckTest > ctlPts.txt
echo These shapes are missing control points:
cat ctlPts.txt
echo These shapes are missing control points: >> $testResults
cat ctlPts.txt >> $testResults
rm ctlPts.txt

# DDAT
ddatResultsFile=ddat.txt
java -Xmx1024m -cp $jarFile test.functional.ladder.recognition.constraint.domains.DomainDescriptionAccuracyTest testData domainDescriptions/domains/COA.xml $ddatResultsFile
echo DDAT results in $ddatResultsFile
echo DDAT results in $ddatResultsFile >> $testResults
