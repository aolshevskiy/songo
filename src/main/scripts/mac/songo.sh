#!/bin/sh
exec java -jar $(dirname $0)/lib/${project.build.finalName}.jar
