To work with a modified upstream library we work on a copy of the upstream sources.
The intention is to move all changes upstream.
If changes break compatibility to older versions or we need very specific stuff, we could maintain it here.

* use the latest upstream sources
* integrate some custom patches (e.g. PR#38, PR#40)
* add the shk-only patches (https://github.com/maggu2810/chromecast-java-api-v2/tree/shk-only)

```sh
rm -rf chromecast-java-api-v2
git clone --origin vitalidze git@github.com:vitalidze/chromecast-java-api-v2.git
cd chromecast-java-api-v2
git remote add maggu2810 git@github.com:maggu2810/chromecast-java-api-v2.git
git fetch maggu2810
git merge maggu2810/shk-only-and-PR40
rm -rf ../src; mv src ..
cd ..
rm -rf chromecast-java-api-v2
# Now we are finished and could e.g. apply the formatter
```
