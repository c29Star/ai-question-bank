@echo off
REM ============================================================
REM 智学云后端本地启动脚本（开发环境）
REM
REM 用法：
REM   1. 复制 .env.local.example 为 .env.local，并填入你的本地密码
REM   2. 双击本脚本（或在 cmd 中执行 run-dev.bat）
REM
REM 行为：
REM   - 自动读取项目根目录下的 .env.local（已 gitignore，不会被推上去）
REM   - 把 DB_USERNAME / DB_PASSWORD / REDIS_PASSWORD 注入为环境变量
REM   - 以 dev profile 启动 Spring Boot
REM
REM 注意：
REM   - .env.local 是本地开发用，已被 .gitignore 拦截，不会被提交
REM   - 如果你重装了系统/换电脑，需要重新建 .env.local
REM ============================================================

setlocal

REM 切到脚本所在目录（即项目根目录）
cd /d "%~dp0"

REM 如果 .env.local 不存在，提示用户
if not exist ".env.local" (
    echo [WARN] .env.local 不存在，请先创建：
    echo        copy .env.local.example .env.local
    echo        然后编辑 .env.local 填入你的本地密码
    echo.
    echo 当前将使用 application-dev.yml 中的默认占位符启动
    echo        （密码为 please-set-your-password，需要 MySQL 允许空密码或已设相同密码）
    echo.
    pause
) else (
    echo [INFO] 读取 .env.local ...
    for /f "usebackq tokens=1,2 delims==" %%a in (".env.local") do (
        if not "%%a"=="" if not "%%a:~0,1%"=="#" (
            set "%%a=%%b"
        )
    )
)

REM 透传关键环境变量给 Spring Boot
if defined DB_USERNAME set "SPRING_DATASOURCE_USERNAME=%DB_USERNAME%"
if defined DB_PASSWORD set "SPRING_DATASOURCE_PASSWORD=%DB_PASSWORD%"
if defined REDIS_PASSWORD set "SPRING_DATA_REDIS_PASSWORD=%REDIS_PASSWORD%"

echo [INFO] 启动后端（dev profile）...
echo        端口：8080
echo.

cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

endlocal
