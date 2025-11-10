#!/bin/sh

##############################################################################
# Gradle Wrapper Script
##############################################################################

APP_NAME="Gradle"
APP_BASE_NAME="${0##*/}"

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

exec java -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
