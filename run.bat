@echo off
title CampusHire — Starting...
echo Starting CampusHire...

set JAVA=C:\PROGRA~1\Android\ANDROI~1\jbr\bin\javaw.exe
set CACHE=C:\Users\abish\.gradle\caches\modules-2\files-2.1

set JFXPATH=%CACHE%\org.openjfx\javafx-controls\21.0.2\76c2c5b29a08edfbf56266079fcefb8c490b4a49\javafx-controls-21.0.2-win.jar
set JFXPATH=%JFXPATH%;%CACHE%\org.openjfx\javafx-fxml\21.0.2\e1e47aa357bedb99dfcf7a1ae402030ca438a879\javafx-fxml-21.0.2-win.jar
set JFXPATH=%JFXPATH%;%CACHE%\org.openjfx\javafx-graphics\21.0.2\e5d2f774d53e2ad0aa13057c82b3ff09f515e200\javafx-graphics-21.0.2-win.jar
set JFXPATH=%JFXPATH%;%CACHE%\org.openjfx\javafx-base\21.0.2\7b3bb793fde4402c0eeb5204c08597c6be30362e\javafx-base-21.0.2-win.jar

set CP=C:\Users\abish\Campushire\build\classes\java\main
set CP=%CP%;C:\Users\abish\Campushire\build\resources\main
set CP=%CP%;%CACHE%\org.xerial\sqlite-jdbc\3.45.3.0\6ae68e5fbf0d184c2f8191818fbe66e4dd70e14\sqlite-jdbc-3.45.3.0.jar
set CP=%CP%;%CACHE%\com.google.code.gson\gson\2.10.1\b3add478d4382b78ea20b1671390a858002feb6c\gson-2.10.1.jar
set CP=%CP%;%CACHE%\org.slf4j\slf4j-api\1.7.36\6c62681a2f655b49963a5983b8b0950a6120ae14\slf4j-api-1.7.36.jar

start "" "%JAVA%" --module-path "%JFXPATH%" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base -cp "%CP%" com.campushire.app.CampusHireApp
