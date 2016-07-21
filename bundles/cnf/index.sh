#!/bin/sh

TMP_DIR="`mktemp -d`"

TMP_STDERR="${TMP_DIR}"/stderr
TMP_IAE="${TMP_DIR}"/iae
TMP_IAE_REPO="${TMP_DIR}"/iae_repo
TMP_ARTIFACTS="${TMP_DIR}"/artifacts
loop=1
while [ ${loop} = 1 ]
do
  #mvn -am -pl tools/shk-deps/ clean install 2> "${TMP_STDERR}"
  mvn clean install 2> "${TMP_STDERR}"
  grep 'java.lang.IllegalArgumentException' "${TMP_STDERR}" > "${TMP_IAE}"
  grep "${HOME}/.m2/repository" "${TMP_IAE}" > "${TMP_IAE_REPO}"
  cat "${TMP_IAE_REPO}" | cut -d\= -f2- | cut -d\  -f5- | grep "^${HOME}/.m2/repository" > "${TMP_ARTIFACTS}"
  grep "^${HOME}/.m2/repository" "${TMP_ARTIFACTS}" || exit
  cat "${TMP_ARTIFACTS}" | while read LINE; do DIR="$(dirname "${LINE}")"; rm -vrf "${DIR}"; done
done

