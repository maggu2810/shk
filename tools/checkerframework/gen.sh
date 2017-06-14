#!/bin/sh

#
# Configuration Parameters
#
CHECKER_VERSION="2.1.9"

#
# Constants
#
CHECKER_JAR="${HOME}/.m2/repository/org/checkerframework/checker/${CHECKER_VERSION}/checker-${CHECKER_VERSION}.jar"

gen() {
  local FQCN="${1}"; shift
  local CP="${1}"; shift

  java -cp "${CHECKER_JAR}":"${CP}" org.checkerframework.framework.stub.StubGenerator "${FQCN}" > "${FQCN}".astub
}

# ./gen.sh org.eclipse.smarthome.core.audio.AudioSink /home/maggu2810/.m2/repository/org/eclipse/smarthome/core/org.eclipse.smarthome.core.audio/0.9.0.a20/org.eclipse.smarthome.core.audio-0.9.0.a20.jar

gen "${@}"
