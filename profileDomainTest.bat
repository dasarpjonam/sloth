javac -sourcepath src -d bin -cp lib/log4j-1.2.15.jar;lib/Jama-1.0.2.jar src/test/ladder/recognition/constraint/domains/DomainDefTest.java
java -javaagent:lib/profile.jar -Dprofile.properties=misc/profile.properties -cp bin;lib/log4j-1.2.15.jar;lib/Jama-1.0.2.jar test.ladder.recognition.constraint.domains.DomainDefTest testData domainDescriptions/domains/COA.xml domainTest.txt
