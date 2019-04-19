@echo off
title Generate Javadocs
javadoc -sourcepath src -d ./docs core enums menu revenue staff tables tools

pause