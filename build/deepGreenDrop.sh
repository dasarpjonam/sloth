# !/bin/bash
################################################################################################
#
# deepGreenDrop
#
################################################################################################
#
# This shell script is used to build and test a drop. This one script encompasses ALL tasks that should be undertaken to
# get a successful drop BUILT and TESTED. If the tests pass to your liking, you may drop the ZIP file.
#
################################################################################################
# Revision History
# -------------------------------
# Mar 12, 2009    Joshua Johnston        Original
################################################################################################

echo Deep Green Drop
echo SRL @ TAMU
date

# perform the build
echo
echo RUN THE BUILD
echo
sh ./deepGreenBuild.sh

# copy the build to a different directory
zipName=`ls deliverable | grep '\.zip'`
dropName=`echo $zipName | sed 's/\.zip//'`
testDirectory=$HOME/Darpa/drop/$dropName
echo
echo COPY $zipName TO $testDirectory
echo
rm -rf $testDirectory
mkdir -p $testDirectory
cp deliverable/$zipName $testDirectory

# move to that directory and unzip
echo
echo CD THERE AND UNZIP
echo
cd $testDirectory
unzip $zipName

# copy junit results to that same directory, if they exist
cp -R testReports $testDirectory

# run the test script
echo
echo RUN THE POST BUILD TEST
echo
chmod +x postBuildTest.sh
sh ./postBuildTest.sh
cat dropTestResults.txt

