@echo off
rem 自动查找 idea64.exe（常见安装位置 + 本机 D 盘位置）；若未自动找到，请把下方的 set 改为你的 idea64.exe 完整路径
set "IDEA_EXE="
for /f "delims=" %%f in ('dir /b /ad "%ProgramFiles%\JetBrains\IntelliJ IDEA*" 2^>nul') do if not defined IDEA_EXE if exist "%ProgramFiles%\JetBrains\%%f\bin\idea64.exe" set "IDEA_EXE=%ProgramFiles%\JetBrains\%%f\bin\idea64.exe"
if not defined IDEA_EXE if exist "D:\CS\Idea\IntelliJ IDEA 2022.3.2\bin\idea64.exe" set "IDEA_EXE=D:\CS\Idea\IntelliJ IDEA 2022.3.2\bin\idea64.exe"
set PROJECT_DIR=%~dp0
if not defined IDEA_EXE (
  echo 未自动找到 IntelliJ IDEA，请编辑本文件，将 IDEA_EXE 设置为你的 idea64.exe 完整路径
  pause
  exit /b 1
)
start "" "%IDEA_EXE%" "%PROJECT_DIR%"
echo 已在 IntelliJ IDEA 中打开工程：%PROJECT_DIR%
