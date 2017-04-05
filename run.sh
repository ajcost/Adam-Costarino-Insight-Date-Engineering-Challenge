#!/usr/bin/env bash

# one example of run.sh script for implementing the features using python
# the contents of this script could be replaced with similar files from any major language

# I'll execute my programs, with the input directory log_input and output the files in the directory log_output
rm -f ./log_output/*.*
touch ./log_output/blocked.txt
touch ./log_output/hosts.txt
touch ./log_output/hours.txt
touch ./log_output/resources.txt

cd src
javac edu/upenn/sas/acost/insightchallenge/*.java
java edu/upenn/sas/acost/insightchallenge/Main




