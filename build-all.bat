@echo off
setlocal
echo ============================================
echo   构建 IT技术交流博客系统（前端 + 后端）
echo ============================================
rem 后端需用 JDK 8 编译（Spring Boot 2.5.9 / Java 8），如安装路径不同请修改下行
set "JAVA_HOME=D:\CS\Java\jdk1.8\jdk1.8"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "MVN=mvn"
where mvn >nul 2>&1 || set "MVN=D:\CS\Maven\apache-maven-3.6.1\bin\mvn.cmd"

echo [1/3] 安装前端依赖并打包...
cd /d "%~dp0vue"
call npm install --no-audit --no-fund || goto :fail
call npm run build || goto :fail

echo [2/3] 把前端产物复制到后端 static 目录...
set "STATIC=%~dp0springboot\src\main\resources\static"
if exist "%STATIC%" rmdir /s /q "%STATIC%"
xcopy /e /i /y "%~dp0vue\dist" "%STATIC%" >nul || goto :fail

echo [3/3] 打包后端 jar...
cd /d "%~dp0springboot"
call %MVN% -DskipTests package || goto :fail

echo.
echo [OK] 构建完成：springboot\target\springboot-0.0.1-SNAPSHOT.jar
echo 现在可以双击 start-backend.bat 启动系统。
pause
exit /b 0

:fail
echo.
echo [错误] 构建失败，请检查上方报错信息。
pause
exit /b 1
