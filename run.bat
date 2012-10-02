@echo off
set PWD=%~dp0
java -cp "%PWD%bin;%PWD%lib\jewelcli-0.6.jar" arden.MainClass %*
pause
