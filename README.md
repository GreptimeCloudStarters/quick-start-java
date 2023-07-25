# Introduction

This is a quick start demo for [GreptimeCloud](https://greptime.cloud/). It collects JVM runtime metrics through Opentelemetry and sends the metrics to GreptimeCloud. You can view the metrics on the GreptimeCloud dashboard.

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
