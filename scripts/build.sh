#!/bin/bash

# Code by Jordan Frimpter
# Command to grant permission to file to run [RUN THIS]: chmod +x build.sh

# code to remove carriage returns from files: sed -i -e 's/\r$//' <filename>

# compilation command [CHANGE THIS to match your project files]

# Root directory of project [CHANGE THIS]
# PROJDIR=/home/012/l/lx/lxa230013/aos/Assignment2
PROJDIR=/mnt/c/Users/layqa/Desktop/2024_Fall/CS6378-AOS/AdvOS-Assignments/Assignment2

# Directory your compiled classes are in [CHANGE THIS if you move the classes]
BINDIR=$PROJDIR/bin

mkdir -p $BINDIR
javac -d $BINDIR -cp $PROJDIR $PROJDIR/code/Runner.java

echo done building
