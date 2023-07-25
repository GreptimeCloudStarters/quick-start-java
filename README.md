# Introduction

This is a quick start demo for [GreptimeCloud](https://greptime.cloud/). It collects JVM runtime metrics through Opentelemetry and sends the metrics to GreptimeCloud. You can view the metrics on the GreptimeCloud dashboard.

## Quick Start

Use the following command line to start it:

```shell
./gradlew run --args="-h <host> -db <dbname> -u <username> -p <password>"
```

You can also run a jar file:

```shell
./gradlew shadowJar
```

```shell
java -jar build/libs/quick-start-java-1.0-SNAPSHOT-all.jar -h <host> -db <dbname> -u <username> -p <password>
```

## Release

1. Update the version in `build.gradle`
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
