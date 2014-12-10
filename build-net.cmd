SET IKVM_PATH="%userprofile%"\agg\build-environment\current\tools\ikvm\bin
%IKVM_PATH%\ikvmc -debug -srcpath:src -target:library -version:1.1 -writeSuppressWarningsFile:ikvmc-warn.log -out:dist\css2xmlfo.dll dist\lib\*.jar dist\css2xmlfo.jar
