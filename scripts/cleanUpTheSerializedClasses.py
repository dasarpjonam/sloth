#!/usr/bin/python

def processStroke(output):
    output.write("[")
    
def processStrokeFinished(output):
    output.write("--\n")

def isStroke(line, output):
    if line.find('org.ladder.core.sketch.Stroke') != -1:
        processStroke(output)
        return True
    return False

def isStrokeFinished(line, output):
    if line.find("info.sift.dg.ca.datamodel.StrokeFinished") != -1:
        processStrokeFinished(output)
        
def processSketchFinished(output):
    output.write("\nsketch\n")
    
def isSketchFinished(line, output):
    if line.find("SketchSegmentFinished") != -1:
        processSketchFinished(output)

def processPoint(output):
    output.write("")

def isPoint(line, output):
    if (line.find("org.ladder.core.sketch.Point")) != -1:
        processPoint(output)

def processX(output, x):
    output.write("'x':%f," % x)
    
def processY(output, y):
    output.write("'y':%f}," % y)
    
def isX(line, x):
    if line.find("<void property=\"x\">") != -1:
        return True;
    
    if (x):
        if line.find("</void>") != -1:
            return False
        else:
            return True
    else:
        return False

def isY(line, y):
    if line.find("<void property=\"y\">") != -1:
        return True;

    if (y):
        if line.find("</void>") != -1:
            return False
        else:
            return True
    else:
        return False
    
def isOpenObject(line):
    if line.find("<object") != -1:
        return True
    return False

def isCloseObject(line, counter, strokeCount, output):
    if line.find("</object>") != -1:
        if counter == strokeCount and strokeCount != 0:
            output.write("]\n")
            return "endstroke"
        return True
    return False

def getXY(line):
    match = "double"
    if line.find(match) != -1:
        try:
            return float(line[line.index('>')+1:line.rindex('<', True)])
        except ValueError, e:
            print "ValueError on getXY(): %s" % line
    else:
        return False
    
def isTime(line, time):
    if line.find("<void property=\"time\">") != -1:
        return True;

    if (time):
        if line.find("</void>") != -1:
            return False
        else:
            return True
    else:
        return False

def getTime(line):
    match = "long"
    if line.find(match) != -1:
        try:
            return long(line[line.index('>')+1:line.rindex('<', True)])
        except ValueError, e:
            print "ValueError on getTime(): %s" % line
    else:
        return False
    
def processTime(output, time):
    output.write("{'time':%d," % time)

def main():
    input = open('symbolset_Nov_Test_1-58.xml', 'r')
    outputFile = open('stageOneNov', 'w')
    objectCounter = 0
    strokeObjectCount = 0
    x = False;
    y = False;
    time = False;
    for line in input:
        if isOpenObject(line): 
            objectCounter += 1
            
        closer = isCloseObject(line, objectCounter, strokeObjectCount, outputFile)
        if closer == "endstroke": 
            strokeObjectCount = 0
        elif closer:
            objectCounter -= 1
            
        if isStroke(line, outputFile):
            strokeObjectCount = objectCounter
            
        isStrokeFinished(line, outputFile) 
        isSketchFinished(line, outputFile)
        
        x = isX(line, x)
        if (x and getXY(line)):
            processX(outputFile, getXY(line))
            
        y = isY(line, y)
        if (y and getXY(line)):
            processY(outputFile, getXY(line))
            
        time = isTime(line, time)
        if (time and getTime(line)):
            processTime(outputFile, getTime(line))
    
if __name__ == "__main__":
    main()