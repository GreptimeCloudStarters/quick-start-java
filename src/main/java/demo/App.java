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

    static OpenTelemetry initOpenTelemetry(String endpoint, String db, String username, String password) {
        // Include required service.name resource attribute on all spans and metrics
        Resource resource =
                Resource.getDefault()
                        .merge(Resource.builder()
                                .put(SERVICE_NAME, "greptime-cloud-quick-start-java").build());
        String auth = username + ":" + password;
        String b64Auth = new String(Base64.getEncoder().encode(auth.getBytes()));

        OtlpHttpMetricExporter exporter = OtlpHttpMetricExporter.builder()
                .setEndpoint(endpoint)
                .addHeader("X-Greptime-DB-Name", db)
                .addHeader("Authorization", String.format("Basic %s", b64Auth))
                .setTimeout(Duration.ofSeconds(5))
                .build();
        PeriodicMetricReader metricReader = PeriodicMetricReader
                .builder(exporter)
                .setInterval(Duration.ofSeconds(5))
                .build();
        SdkMeterProvider meterProvider = SdkMeterProvider
                .builder()
                .setResource(resource)
                .registerMetricReader(metricReader)
                .build();

        OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
                .setMeterProvider(meterProvider)
                .buildAndRegisterGlobal();
        Runtime.getRuntime().addShutdownHook(new Thread(openTelemetrySdk::close));
        return openTelemetrySdk;
    }

    static String getCmdArgValue(String argName, String defaultValue, String[] args){
        Options options = new Options();
        Option dbHost = new Option("h", "host", true, "The host address of the GreptimeDB");
        Option db = new Option("db", "database", true, "The database of the GreptimeDB");
        Option username = new Option("u", "username", true, "The username of the database");
        Option password = new Option("p", "password", true, "The password of the database");
        options.addOption(dbHost);
        options.addOption(db);
        options.addOption(username);
        options.addOption(password);
        CommandLine cmd;
        CommandLineParser parser = new DefaultParser();
        HelpFormatter helper = new HelpFormatter();
        try {
            cmd = parser.parse(options, args);
            String arg = cmd.getOptionValue(argName, defaultValue);
            return arg;
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            helper.printHelp("Usage:", options);
            System.exit(-1);
        }
        return "";
    }
    public static void main(String[] args) throws Exception {
        String dbHost = getCmdArgValue("host","localhost", args);
        String db = getCmdArgValue("database", "public", args);
        String username = getCmdArgValue("username", "", args);
        String password = getCmdArgValue("password", "", args);
        String endpoint = "";
        if (dbHost == "localhost" || dbHost == "127.0.0.1"){
            endpoint = String.format("http://%s:4000/v1/otlp/v1/metrics", dbHost);
        } else {
            endpoint = String.format("https://%s/v1/otlp/v1/metrics", dbHost);
        }
        OpenTelemetry openTelemetry = initOpenTelemetry(endpoint, db, username, password);
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
