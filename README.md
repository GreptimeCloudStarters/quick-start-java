# Introduction

This is a quick start demo for [GreptimeCloud](https://greptime.cloud/). It collects JVM runtime metrics through Opentelemetry and sends the metrics to GreptimeCloud. You can view the metrics on the GreptimeCloud dashboard.

## Quick Start

You can just download and run the jar file:

```shell
curl -L https://github.com/GreptimeCloudStarters/quick-start-java/releases/download/v0.1.1/quick-start-java-0.1.1-SNAPSHOT-all.jar --output quick-start.jar
java -jar quick-start.jar -h <host> -db <dbname> -u <username> -p <password>
```

Clone the repository and run:

```shell
./gradlew run --args="-h <host> -db <dbname> -u <username> -p <password>"
```

Or build a jar file by yourself and run:

```shell
./gradlew shadowJar
java -jar build/libs/quick-start-java-1.0-SNAPSHOT-all.jar -h <host> -db <dbname> -u <username> -p <password>
```

## Release

1. Update the version in `build.gradle`.
2. Commit and push code
3. Build jar

```shell
./gradlew shadowJar
```

4. Create a tag with the version and push it

```shell
git tag v<major>.<minor>.<patch>
git push origin v<major>.<minor>.<patch>
```

5. Add release and upload the jar file
6. update the jar download link in `README.md`
