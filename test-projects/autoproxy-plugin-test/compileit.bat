cls
del /F /Q autoproxy-plugin.jar
xcopy ..\..\scala-plugin-workspace\autoproxy-plugin\target\jar\autoproxy-plugin.jar .
scalac -Xplugin:autoproxy-plugin.jar -verbose -Ycheck:generatesynthetics -Ybrowse:earlytyper -Ylog:errorretyper,lambdalift -d .\plugin-output .\src\main\scala\autoproxy\test\*.scala
REM -Ycheck:errorretyper
REM -Ytyper-debug