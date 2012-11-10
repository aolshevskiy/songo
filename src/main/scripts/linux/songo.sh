#!/bin/sh
exec java -Djna.library.path=$(dirname $0)/lib -jar $(dirname $0)/lib/${project.build.finalName}.jar