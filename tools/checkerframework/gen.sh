#!/bin/sh

CHECKER_JAR=${HOME}/.m2/repository/org/checkerframework/checker/2.1.5/checker-2.1.5.jar

FQCN=org.eclipse.smarthome.core.audio.AudioSink

gen() {
  local FQCN="${1}"; shift
  local CP="${1}"; shift

  java -cp "${CHECKER_JAR}":"${CP}" org.checkerframework.framework.stub.StubGenerator "${FQCN}" > "${FQCN}".astub
}

# ./gen.sh org.eclipse.smarthome.core.audio.AudioSink /home/maggu2810/.m2/repository/org/eclipse/smarthome/core/org.eclipse.smarthome.core.audio/0.9.0.a20/org.eclipse.smarthome.core.audio-0.9.0.a20.jar

gen "${@}"
