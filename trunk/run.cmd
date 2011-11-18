@ECHO ON
java -version 2>&1 | FIND "1.6" > NUL
IF %ERRORLEVEL% == 0 start javaw -Djava.util.logging.config.file=logging.properties -splash:splash.JPG -jar .\SpellChecker.jar
IF %ERRORLEVEL% NEQ 0 start javaw -Djava.util.logging.config.file=logging.properties -jar .\SpellChecker.jar
