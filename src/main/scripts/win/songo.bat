@echo off
SET BATCH_SCRIPT_DRIVE=%~d0
SET BATCH_SCRIPT_FOLDER_PATHNAME=%~dp0
%BATCH_SCRIPT_DRIVE%
CD "%BATCH_SCRIPT_FOLDER_PATHNAME%"
start javaw -Dfile.encoding=UTF-8 -Djna.library.path=lib -jar lib/${project.build.finalName}.jar