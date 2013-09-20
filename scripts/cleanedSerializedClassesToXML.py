#!/usr/bin/python

def startFile(file):
    file.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")

def startSketch(file):
    file.write("<BeginSketch>\n")

def endSketch(file):
    file.write("</BeginSketch>\n")
    
def startStroke(file):
    file.write("<Stroke beautified = \"false\" type = \"-1\">\n")
    
def endStroke(file):
    file.write("</Stroke>\n")

def point(p, file, adjustment, min_x, min_y, max_y):
    file.write("<Point x=\"%f\" y=\"%f\" t=\"%d\"/>\n" % (adjustToScreenApproxCoords(p['x'], adjustment, min_x),
                                                          adjustToScreenApproxCoords(p['y'], adjustment, min_y, max_y),
                                                          p['time']))

def adjustToScreenApproxCoords(coord, multiplier, translate, max_y=False):
    if max_y:
        print abs((abs(max_y)-abs(coord*multiplier))), abs(translate)
        return abs((abs(max_y)-abs(coord*multiplier)))+200
    else:
        return abs(abs(coord*multiplier)-abs(translate))+200

def distance(x1, x2, y1, y2):
    from math import sqrt
    return sqrt((x2-x1)**2+(y2-y1)**2)

def main():
    prefix = "novXML/Sketch"
    input = open('stageOneNov')
    sketches = []
    tmp = []
    sizeBetweenInPixels = 2
    
    for line in input:
        if line.find("sketch") != -1:
            sketches.append(tmp)
            tmp = []
        elif line.find("--") != -1 or line == "\n":
            continue
        else:
            tmp.append(line)
        
    j = 0    
    
    for sketch in sketches:
        out = open(prefix+str(j)+".xml", 'w')
        minimumDistance = 10000000
        min_x = 100000000000
        min_y = 100000000000
        max_y = -100000000000
        startFile(out)
        startSketch(out)
        
        for stroke in sketch:
            stroke = eval(stroke)
            for i in range(0, len(stroke)-1):
                if (distance(stroke[i]['x'], stroke[i+1]['x'], stroke[i]['y'], stroke[i+1]['y']) < minimumDistance and distance(stroke[i]['x'], stroke[i+1]['x'], stroke[i]['y'], stroke[i+1]['y']) != 0):
                    minimumDistance = distance(stroke[i]['x'], stroke[i+1]['x'], stroke[i]['y'], stroke[i+1]['y'])
                if (stroke[i]['x'] < min_x): 
                    min_x = stroke[i]['x']
                if (stroke[i]['y'] < min_y): 
                    min_y = stroke[i]['y']
                if (stroke[i]['y'] > max_y): 
                    max_y = stroke[i]['y']
            if (stroke[len(stroke)-1]['x'] < min_x): 
                min_x = stroke[i]['x']
            if (stroke[len(stroke)-1]['y'] < min_y):
                min_y = stroke[i]['y']
            if (stroke[len(stroke)-1]['y'] > max_y): 
                    max_y = stroke[i]['y']
        multiplier = sizeBetweenInPixels/minimumDistance
        min_x *= multiplier
        min_y *= multiplier
        print max_y
        max_y *= multiplier
        print max_y
        for stroke in sketch:
            stroke = eval(stroke)
            startStroke(out)
            for p in stroke:
                point(p, out, multiplier, min_x, min_y, max_y)
            endStroke(out)    
        
        endSketch(out)
        out.close()
        j += 1
            

if __name__ == "__main__":
    main()
