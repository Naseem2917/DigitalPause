@rem DigitalPause Gradle Wrapper Runner
@if exist "%~dp0gradle\wrapper\gradle-wrapper.jar" (
  @"%JAVA_HOME%\bin\java.exe" -Dorg.gradle.appname=gradlew -classpath "%~dp0gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
) else (
  @"C:\Users\HP\.gradle\wrapper\dists\gradle-9.5.0-bin\bvnork1r7n8i6kp5cnkibsc9q\gradle-9.5.0\bin\gradle.bat" %*
)
