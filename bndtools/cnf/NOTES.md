Fix _remote.properties

mvn clean install 2>&1 | grep 'java.lang.IllegalArgumentException' | grep "${HOME}/.m2/repository" | cut -d\= -f2- | cut -d\  -f5- | grep "^${HOME}/.m2/repository" | while read LINE; do DIR="$(dirname "${LINE}")"; rm -vrf "${DIR}"; done
