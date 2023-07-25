package demo;

import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.SERVICE_NAME;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;

import java.time.Duration;
import org.apache.commons.cli.*;
import java.util.Base64;

import io.opentelemetry.instrumentation.runtimemetrics.*;

/**
 * Example of using a Long Gauge to measure execution time of method. The gauge callback will get
 * executed every collection interval. This is useful for expensive measurements that would be
 * wastefully to calculate each request.
 */
public final class App {

    static OpenTelemetry initOpenTelemetry(String dbHost, String db, String username, String password) {
        // Include required service.name resource attribute on all spans and metrics
        Resource resource =
                Resource.getDefault()
                        .merge(Resource.builder()
                                .put(SERVICE_NAME, "greptime-cloud-quick-start-java").build());
        String endpoint = String.format("https://%s/v1/otlp/v1/metrics", dbHost);
        String auth = username + ":" + password;
        String b64Auth = new String(Base64.getEncoder().encode(auth.getBytes()));

        OpenTelemetrySdk openTelemetrySdk =
                OpenTelemetrySdk.builder()
                        .setMeterProvider(
                                SdkMeterProvider
                                        .builder()
                                        .setResource(resource)
                                        .registerMetricReader(
                                                PeriodicMetricReader
                                                        .builder(OtlpHttpMetricExporter.builder()
                                                                .setEndpoint(endpoint)
                                                                .addHeader("x-greptime-db-name", db)
                                                                .addHeader("Authorization", String.format("Basic %s", b64Auth))
                                                                .setTimeout(Duration.ofSeconds(5))
                                                                .build())
                                                        .setInterval(Duration.ofSeconds(2))
                                                        .build())
                                        .build())
                        .buildAndRegisterGlobal();
        Runtime.getRuntime().addShutdownHook(new Thread(openTelemetrySdk::close));
        return openTelemetrySdk;
    }

    static String getCmdArgValue(String argName ,String[] args){
        Options options = new Options();
        Option dbHost = new Option("h", "host", true, "The host address of the GreptimeCloud service");
        Option db = new Option("db", "database", true, "The database of the GreptimeCloud service");
        Option username = new Option("u", "username", true, "The username of the database");
        Option password = new Option("p", "password", true, "The password of the database");
        options.addOption(dbHost);
        options.addOption(db);
        options.addOption(username);
        options.addOption(password);
        CommandLine cmd;
        CommandLineParser parser = new BasicParser();
        HelpFormatter helper = new HelpFormatter();
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption(argName)) {
                String arg = cmd.getOptionValue(argName);
                return arg;
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            helper.printHelp("Usage:", options);
            System.exit(-1);
        }
        helper.printHelp("Usage:", options);
        System.exit(-1);
        return "";
    }
    public static void main(String[] args) throws Exception {
        String dbHost = getCmdArgValue("host", args);
        String db = getCmdArgValue("database", args);
        String username = getCmdArgValue("username", args);
        String password = getCmdArgValue("password", args);

        OpenTelemetry openTelemetry = initOpenTelemetry(dbHost, db, username, password);
        BufferPools.registerObservers(openTelemetry);
        Classes.registerObservers(openTelemetry);
        Cpu.registerObservers(openTelemetry);
        GarbageCollector.registerObservers(openTelemetry);
        MemoryPools.registerObservers(openTelemetry);
        Threads.registerObservers(openTelemetry);
        System.out.println("Sending metrics...");
        while (true) {
            Thread.sleep(2000);
        }
    }
}
