@echo off
title Generate Javadocs
javadoc -sourcepath src -d ../Javadocs -private -noqualifier all core enums menu revenue staff tables tools

pause