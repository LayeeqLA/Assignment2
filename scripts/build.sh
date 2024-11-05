#!/bin/bash

# Code adapted from posted example by Jordan Frimpter
# Updated by Layeeq
# Command to grant permission to file to run [RUN THIS]: chmod +x build.sh

# code to remove carriage returns from files: sed -i -e 's/\r$//' <filename>

# compilation command [CHANGE THIS to match your project files]

# Root directory of project [CHANGE THIS]
PROJDIR=/home/012/l/lx/lxa230013/aos/Assignment2

# Directory your compiled classes are in [CHANGE THIS if you move the classes]
BINDIR=$PROJDIR/bin

# External jars if any for classpath
LIBDIR=$PROJDIR/lib/*

mkdir -p $BINDIR
javac -d $BINDIR -cp $LIBDIR:$PROJDIR $PROJDIR/code/Runner.java

echo done building
