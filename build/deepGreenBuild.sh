# !/bin/bash
#############################
#
# This BASH script is intened to be an "automatic" build for Deep Green releases. There is still one thing you have to
# do by hand:
# - Java does not have a nice way to check code dependencies. Therefore, when you are changing the classes that need to
# be released, you will have to edit build.xml and change the source code file set. If you don't, and you're missing
# classes, ANT will pass along the Java compiler's complaints and die.
#
# Otherwise, after you've manually modified the source code file set (if needed), just run this script from the command
# line with
#     ./deepGreenBuild.sh
#
# You might have to first set it executable with
#     chmod +x deepGreenBuild.sh
#
# Note that this is a BASH script, so you'll need the Bourne Again shell (BASH) to run it. This means a *NIX
# environment, like Linux, OS X, or Cygwin for Windows.
#
# All the testData and shapeDefinition entries will be taken from COA.xml. That means you should regard COA.xml as
# scripture. Anything you want in your release you must put into COA.xml. Anything you DO NOT want in your release you
# just don't put into COA.xml. Magic.
#
# Instructions:
#     1) Run this script. It should run ant automatically.
#     2) That's all
#
# Troubleshooting:
#     Make sure you have ANT installed. I recommend the latest version, which was 1.7.1 at the time of this writing.
#     Make sure You have ant configured to compile against Java 1.6. Here's what I had to do on my install of OS X
#          You need to specify that your JAVA_HOME directory variable points to 1.6. But, it looks for
#              JAVA_HOME/bin/java, which doesn't work on OS X because the Java executables are stored in the Commands
#              directory. It /should/ magically work on Windows, just set the environment variable in Control Panel.
#          cd ~
#          mkdir javaHome
#          cd javaHome
#          ln -fhsv /System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Commands bin
#              This tricks OS X into thinking that our ~/javaHome directory has a bin subdirectory that contains all our
#              Java commands
#          export JAVA_HOME=$HOME/javaHome
#              Now, ant will look for 'java' and 'javac' in $JAVA_HOME/bin, which is our symlink to the right Java
#              version's Commands folder. You'll probably want to put this export statement in your .bash_profile.
#
#############################
#
# Release notes
# -------------
# Jan 27 2009, Josh Johnston, Original
# Feb 26 2009, Josh Johnston, Smart copy of test data directories based on what's in COA
# Mar 12 2009, Josh Johnston, Invoke ant and build.xml from the command line, dynamically set date name of jar/zip files
# Mar 31 2009, Josh Johnston, New test data folder; testData517
#
##############################



##############################
#
# Define our variables
# 
###############################

# Date--name of the JAR and ZIP files
dateName=`date +"%Y-%m-%d"`
echo "Setting name of JAR and ZIP files to $dateName"

# ANT stuff, the build file and task within the build that we'll execute
antCommand=ant
antBuildFile=build.xml
antParameters="-DdeepGreenVersion=$dateName"
antBuildTarget="test.junit deepGreenCompletePackage"

# Stuff for the domain/shape definition directories
domainBase=../../LadderDomains;
domainDefDirectory=$domainBase/domainDescriptions/domains
shapeDefDirectory=$domainBase/domainDescriptions/shapes
domainDefinitionFile=$domainDefDirectory/COA.xml

# Stuff for the testData directories
dataBase=../../LadderData
testDataDirectory=$dataBase/testData517

# The target directories where we're going to copy stuff into
targetDomainDir=$domainBase/deepGreenRelease/domainDescriptions/domains
targetShapeDefDir=$domainBase/deepGreenRelease/domainDescriptions/shapes
targetDataDir=$dataBase/deepGreenRelease/testData


##############################
#
# Copy over testData and shapeDefinitions
# 
###############################


# Clean out the target directories
echo "Cleaning out (recursive delete) $targetDataDir"
rm -rf $targetDataDir
mkdir -p $targetDataDir
echo "Cleaning out (recursive delete) $targetDomainDir"
rm -rf $targetDomainDir
mkdir -p $targetDomainDir
echo "Cleaning out (recursive delete) $targetShapeDefDir"
rm -rf $targetShapeDefDir
mkdir -p $targetShapeDefDir

# Copy over the actual COA.xml file
cp $domainDefinitionFile $targetDomainDir
# Get the contents of the COA.xml and loop over each shape that is defined there
for shape in `cat $domainDefinitionFile | grep 'shape name' | sed 's/.*shapeDefinition="\(.*\).xml">.*/\1/'`
do
    echo "Processing $shape"

    shapeNamePrefix=`echo $shape | sed 's/\([0-9]*_[FHX]\).*/\1/'`

    # Copy the shape definition for this shape, if a shape definition XML file exists
    shapeDefFile="$shapeDefDirectory/$shape.xml"
    if [[ -f $shapeDefFile ]]
    then
        echo "    - copy $shapeDefFile"
        cp $shapeDefFile $targetShapeDefDir
    else
        echo "No shape definition XML file found: $shapeDefFile"
    fi

    # Copy the test data directories for the shape. This involves matching the first part of the 
    # shape name to the first part of test data directories
    for dataDir in `ls $testDataDirectory | grep "^$shapeNamePrefix"`
    do
        testDataDir=$testDataDirectory/$dataDir
        # only copy if this data directory exists in the testData location, but also if it doesn't already exist in the
        # target location
        if [[ -d $testDataDir && ! -d $targetDataDir/$dataDir ]]
        then
            echo "    - Copying data from $dataDir"
            cp -R $testDataDir $targetDataDir
            rm -rf $targetDataDir/$dataDir/.svn
        else
            echo "    No testData to copy for: $testDataDir. Already copied or does not exist"
        fi
    done
done

echo "We just copied `ls $targetShapeDefDir | wc -l` shape definitions and `ls $targetDataDir | wc -l` test data directories"
echo "That's `du -c -h $targetDataDir/ | grep 'total' | cut -f1` of data across `find $targetDataDir -name "*.xml" | wc -l` example sketches"


# Run ANT
echo
echo
echo 'Running ANT from the command line....'

$antCommand -buildfile $antBuildFile $antParameters $antBuildTarget
du -h deliverable/*.zip

