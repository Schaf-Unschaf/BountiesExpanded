..\jre\bin\java.exe -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -Djava.library.path=native\windows -Xms1536m -Xmx1536m -Xss2048k -Dcom.fs.starfarer.settings.paths.logs=. -Dcom.fs.starfarer.settings.paths.saves=../saves -Dcom.fs.starfarer.settings.paths.screenshots=../screenshots -Dcom.fs.starfarer.settings.paths.mods=../mods -classpath janino.jar;commons-compiler.jar;commons-compiler-jdk.jar;starfarer.res.jar;starfarer.api.jar;starfarer_obf.jar;jogg-0.0.7.jar;jorbis-0.0.15.jar;json.jar;lwjgl.jar;lwjgl_util_applet.jar;jinput.jar;lwjgl_test.jar;log4j-1.2.9.jar;lwjgl_util.jar;fs.sound_obf.jar;fs.common_obf.jar;xstream-1.4.10.jar com.fs.starfarer.StarfarerLauncher
@echo off
if errorlevel 1 (
   pause
)