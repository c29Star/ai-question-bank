@echo off
REM ============================================================
REM 智学云前端本地启动脚本（开发环境）
REM
REM 用法：双击运行
REM 默认连后端 http://localhost:8080
REM ============================================================

setlocal
cd /d "%~dp0"

cd frontend

if not exist "node_modules" (
    echo [INFO] 安装依赖中 ...
    call npm install
)

echo [INFO] 启动前端开发服务器（端口 5173）...
echo.
npm run dev

endlocal
