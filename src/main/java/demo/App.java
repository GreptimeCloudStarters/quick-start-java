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

    static Options generateCommandOptions() {
        Options options = new Options();
        Option dbHost = new Option("h", "host", true, "The host address of the GreptimeDB");
        Option db = new Option("db", "database", true, "The database of the GreptimeDB");
        Option username = new Option("u", "username", true, "The username of the database");
        Option password = new Option("p", "password", true, "The password of the database");
        Option noSecure = new Option("ns", "no-secure", false, "Do not use secure connection to GreptimeDB" );
        Option port = new Option("P", "port", true, "The port of the HTTP endpoint of GreptimeDB");
        options.addOption(dbHost);
        options.addOption(db);
        options.addOption(username);
        options.addOption(password);
        options.addOption(noSecure);
        options.addOption(port);
        return options;
    }

    static CommandLine getCmd(String[] args) throws ParseException {
        Options options = generateCommandOptions();
        CommandLine cmd;
        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(options, args);
        return cmd;
    }

    public static void main(String[] args) throws Exception {
        Options options = generateCommandOptions();
        String dbHost = "";
        String db = "";
        String username = "";
        String password = "";
        String port = "";
        boolean noSecure = false;
        try {
            CommandLine cmd = getCmd(args);
            dbHost = cmd.getOptionValue("host", "localhost");
            db = cmd.getOptionValue("database", "public");
            username = cmd.getOptionValue("username", "");
            password = cmd.getOptionValue("password", "");
            port = cmd.getOptionValue("port", "");
            if (cmd.hasOption("no-secure")){
                noSecure = true;
            }
        } catch (ParseException e) {
            HelpFormatter helper = new HelpFormatter();
            System.out.println(e.getMessage());
            helper.printHelp("Usage:", options);
            System.exit(-1);
        }

        String url = "";
        if (noSecure){
            url = "http://";
        } else {
            url = "https://";
        }

        url += dbHost;
        if (port != ""){
            url = String.format("%s:%s", url, port);
        }
        url += "/v1/otlp/v1/metrics";

        OpenTelemetry openTelemetry = initOpenTelemetry(url, db, username, password);
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
