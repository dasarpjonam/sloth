TAMU Deep Green Sketch Recognition deliverable.

ZIP file contents:

api -- JavaDoc for provided code
conf -- Configuration file used by the system
domainDescriptions -- folder containing descriptions of shapes recognized by the high-level recognition algorithm.
lib -- all external JAR files our code depends on
logging -- contains configuration file for logging, and will contain the logs as they're produced
src -- our source code
testData -- the data used to perform our functional tests
funcitonalTests.txt -- a readme on how to use our functional tests.
README.txt -- this readme file
sketchModule.pdf -- Documentation about the sketch engine and its interfaces at a high level
tamuDeepGreen-XXX.jar -- Version XXX of our sketch recognition module for the Deep Green project. The jar's manifest is set to pick up the lib directory on its classpath. It is also executable and double clicking it launches a simple program to test recognition. But, we recommend launching it from the command line using [java -jar tamuDeepGreen-XXX.jar].