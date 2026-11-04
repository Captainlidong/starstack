@echo off
setlocal EnableDelayedExpansion
set PORT=9090
rem 若 java 不在 PATH 中，请把下行改为 java.exe 的完整路径（本项目需 JDK 8）
set "JAVA=java"
rem 本机专属覆盖（可选，此文件不入库）：创建 start-backend.local.bat 并写入 set "JAVA=完整路径"
if exist "%~dp0start-backend.local.bat" call "%~dp0start-backend.local.bat"
set "JAR=%~dp0springboot\target\springboot-0.0.1-SNAPSHOT.jar"
set "RUNJAR=%TEMP%\itchatblog-app.jar"

echo ============================================
echo   启动 IT技术交流博客系统 后端
echo ============================================

rem already running?
for /f "tokens=5" %%a in ('netstat -ano ^| findstr /R /C:":%PORT% " ^| findstr /I "LISTENING"') do (
  echo [提示] 后端已在运行 PID=%%a，无需重复启动。
  start "" http://localhost:%PORT%
  pause
  exit /b 0
)

if not exist "%JAR%" (
  echo [错误] 未找到后端包：
  echo   %JAR%
  echo 请先双击 build-all.bat 构建系统
  pause
  exit /b 1
)

copy /Y "%JAR%" "%RUNJAR%" >nul

rem 数据库密码等本机配置文件（已 gitignore，不入库）；相对路径基于下方 cd 后的项目根目录
set "LOCALCFG="
if exist "%~dp0springboot\application-local.yml" set "LOCALCFG=--spring.config.additional-location=optional:file:./springboot/application-local.yml"

rem 工作目录必须是项目根目录：上传文件存储在根目录 files\ 下
cd /d "%~dp0"

echo [1/2] 正在启动后端...
start "ITCHatBlogBackend" "%JAVA%" -Dfile.encoding=UTF-8 -jar "%RUNJAR%" "%LOCALCFG%"

echo [2/2] 等待服务就绪...
set OK=0
for /L %%i in (1,1,30) do (
  ping -n 2 127.0.0.1 >nul
  netstat -ano | findstr /R /C:":%PORT% " | findstr /I "LISTENING" >nul
  if not errorlevel 1 (
    set OK=1
    goto :ready
  )
)
:ready
if "%OK%"=="1" (
  echo [OK] 后端已启动：http://localhost:%PORT%
  echo 正在把后台窗口最小化到任务栏...
  powershell -NoProfile -WindowStyle Hidden -Command "(New-Object -ComObject Shell.Application).Windows() | Where-Object {$_.LocationName -eq 'ITCHatBlogBackend'} | ForEach-Object {$_.Minimize()}"
  start "" http://localhost:%PORT%
  echo.
  echo 后端已在最小化的 ITChatBlogBackend 窗口里运行，平时不用管它。
  echo 本窗口现在可以关掉了（关掉不影响后端）。
  echo 要停止后端服务，请双击 stop-backend.bat。
) else (
  echo [警告] 30 秒内未检测到 %PORT%，请查看 ITChatBlogBackend 窗口里的报错。
)
echo.
echo 要关闭系统请运行 stop-backend.bat
pause
