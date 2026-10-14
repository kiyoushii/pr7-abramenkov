#!/bin/sh
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -d out src/ru/edu/pr04/*.java && java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out ru.edu.pr04.SelfCheck
