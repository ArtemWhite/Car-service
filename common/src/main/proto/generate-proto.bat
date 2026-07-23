@echo off
chcp 65001 > nul
cd /d "%~dp0"

echo Generating proto files...

C:\protoc\bin\protoc.exe --java_out=../../java car_service.proto

if %errorlevel% equ 0 (
    echo SUCCESS! Proto files generated.
    echo Files are in: ..\..\java\com\dealership\grpc\
) else (
    echo FAILED! Check errors above.
)

pause