@echo off
title JUGADOR 1 - BATALLA NAVAL
color 0B
echo ========================================
echo      JUGADOR 1 - BATALLA NAVAL
echo ========================================
echo.
echo Conectando al servidor...
echo.

java -cp "client/target/classes;shared/target/classes" co.edu.uptc.client.ClientMain localhost 1100

echo.
echo El juego se ha cerrado.
pause