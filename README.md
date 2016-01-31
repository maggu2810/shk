# SmartHome for Karaf

The intention of this project is to give an easy to use integration of the Eclipse SmartHome framework in the Karaf container.

## Release

Build release without Jenkins:

```text
REPO_USER=""
REPO_PASS=""
REPO_URL=""
VERSION_RELEASE=""
VERSION_NEXT=""

mvn -B -DpreparationGoals="clean install" -DdevelopmentVersion="${VERSION_NEXT}" -DreleaseVersion="${VERSION_RELEASE}" release:prepare release:perform

# -Darguments="-DaltDeploymentRepository=artifactory.buildsystem::default::http://${REPO_USER}:${REPO_PASS}@${REPO_URL} \
```
