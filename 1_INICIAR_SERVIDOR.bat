@echo off
title SERVIDOR BATALLA NAVAL
color 0A
echo ========================================
echo    SERVIDOR BATALLA NAVAL RMI
echo ========================================
echo.
echo Iniciando servidor en puerto 1100...
echo.

java -cp "server/target/classes;shared/target/classes" co.edu.uptc.server.ServerMain

echo.
echo El servidor se ha cerrado.
pause