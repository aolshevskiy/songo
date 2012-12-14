#!/bin/sh
exec java -XstartOnFirstThread -jar $(dirname $0)/lib/${project.build.finalName}.jar
