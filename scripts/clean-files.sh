#!/bin/bash

# Code adapted from posted example by Jordan Frimpter
# Updated by Layeeq
# Command to grant permission to file to run [RUN THIS]: chmod +x cleanFiles.sh

# WARNING: this will delete all class files and output files in the folder

PROJDIR=/home/012/l/lx/lxa230013/aos/Assignment2
BINDIR=$PROJDIR/bin

# [CHANGE THIS to remove files from your project as is relevant to your language]
rm -r $BINDIR      # removes java class files
rm $PROJDIR/*.out        # removes output files

echo done cleaning files