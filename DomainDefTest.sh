# !/bin/bash

compileFile=ddatCompileResults.txt
classpath=lib/log4j-1.2.15.jar:lib/Jama-1.0.2.jar:lib/weka.jar:lib/libsvm.jar:lib/xml-apis.jar:lib/xercesImpl.jar
domainDefClass=test.functional.ladder.recognition.constraint.domains.DomainDescriptionAccuracyTest
# turn the qualified class name into filename for the Java file. Change . to /, prepend src/, append .java
domainDefSourceFile=`echo $domainDefClass | sed -e 's/\./\//g' -e 's/^/src\//' -e 's/$/\.java/'`
javacOptions=-Xlint
javaOptions=-Xmx2048m

testDataDir=../LadderData/testDataIAAI
domainDefinitions=../LadderDomains/domainDescriptions/domains/COA.xml
outputFile=
sampling=

javac $javacOptions -sourcepath src -d bin -cp $classpath $domainDefSourceFile > $compileFile
cat $compileFile
java $javaOptions -cp bin:$classpath $domainDefClass $testDataDir $domainDefinitions $outputFile $sampling
