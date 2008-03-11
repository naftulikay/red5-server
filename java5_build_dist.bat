
SET JAVA_HOME=c:\dev\java5
SET PATH=c:\dev\java5\bin;%PATH%

ant -Djava.target_version=1.5 clean dist

pause
