:: 关闭回显，即执行本脚本时不显示执行路径和命令，直接显示结果
@echo off
rem @author sc

color f8
rem 定义nginx目录
set NGINX_DIR=C:\nginx-1.20.2\

if exist "%NGINX_DIR%nginx.exe" (
cd /d %NGINX_DIR%
start nginx.exe
)

rem 运行exe文件
start /min "" "C:\PLC-Release\PLCRouter.exe"

rem 休眠5秒
timeout /T 5

rem 执行 bat 脚本
start "D:\install-software\work\tomcat8\bin\startup.bat"

rem 将此文件放入如下目录中 C:\ProgramData\Microsoft\Windows\Start Menu\Programs\StartUp