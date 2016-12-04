@echo off
call :runme > 0_all_sounds.txt
goto:eof

:runme
for %%f in (.\*) do echo getAM().loadSound("%%f", async);
