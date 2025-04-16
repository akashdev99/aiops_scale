package scaletest;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import org.xerial.snappy.Snappy;
import remotewrite.PromMetric;
import remotewrite.Remote;
import remotewrite.Types;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * This sample is based on our official tutorials:
 * <ul>
 *   <li><a href="https://docs.gatling.io/tutorials/recorder/">Gatling quickstart tutorial</a>
 *   <li><a href="https://docs.gatling.io/tutorials/advanced/">Gatling advanced tutorial</a>
 * </ul>
 */
public class SsxDataIngestion extends Simulation {

    private static final PromMetric METRIC = PromMetric.builder()
            .name("scale_test")
            .labels(Map.of("type", "some_type"))
            .value(2)
            .currentTime(System.currentTimeMillis())
            .build();
    private static final String BASE_URL = "https://edge.staging.cdo.cisco.com";

    private static final String SSX_INGESTION_ENDPOINT = "/api/platform/ssx-exchange/proxy/ai-ops/api/platform/ai-ops-data-ingest/v1/healthmetrics";
    private static final String SSX_TOKEN = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJTU0UiLCJzdWIiOiJDb25uZWN0b3IiLCJleHAiOjE3NDEwNzM1NTAsImlhdCI6MTczMzI5NzU1MCwianRpIjoiNmY0OWUzODQtYjQ5OS00YmU1LTlhY2ItNjcwNDdhY2NjNGM2IiwiYXVkIjpbIlNTRSJdLCJpZCI6ImViMjk3ZDFmLTU1NWUtNDcyNy1hMjBkLTM1MDMwOWRhMDBmNSIsIm9yZ0lkIjoiMzhjYjZkMTQtMWUyMy00NDA4LWI3ODUtYTNhYWE5MDk2ZjgxIiwic3Vic2NyaXB0aW9ucyI6WyJyYXctZXZlbnQtYWNjZXNzIiwiY2RvLWV2ZW50LWFjY2VzcyIsIklURC1CYXNlIiwibmdmdyIsImh5YnJpZC0yLjAiXSwiand0VHlwZSI6ImFjY2VzcyIsInZlcnNpb24iOiJ2MiJ9.TsZqVGcQRjvEpo4pQ6_xlaY69mVj2rFNWA54Un9U_Ml5WSGIXtauLaQGbDZ36tZ8MR_2jUUwT4pdCkEiP_u_QBpQmtPyMkjVUFwJK6TyBR1rZ-2WUX7Qq2slrNkaph0FEzH4lR8FNDikfgcNxIQQyvbu7ELFhHmHEcKczGFQirhR6dNvN8PQY4wmA5m0fZwaNB3Rh6eSF6539z2HJx5JjBh1mOm9ibUDqvKg4N01HvHjL_REfOuxzOcykrT4QQdl4V50ZcwmBrmTYMDsOfFS4k8yPubj70raVnKUZvvc3Lnwj-hxiTqoHdmbyTLnVcFAwfhUZm6PkzjWzke2Ld2mLw"; // Replace with the actual token
    private static final Types.TimeSeries ts = METRIC.convToTimeSeries(UUID.fromString("38cb6d14-1e23-4408-b785-a3aaa9096f81"));

    private static final Remote.WriteRequest REMOTE_WRITE_REQUEST = Remote.WriteRequest.newBuilder()
            .addTimeseries(ts)
            .build();

    HttpProtocolBuilder httpSSXProtocol =
            http.baseUrl(BASE_URL)
                    .acceptHeader("application/json")
                    .authorizationHeader(SSX_TOKEN)
                    .contentTypeHeader("application/x-protobuf")
                    .header("Content-Encoding", "snappy");

    ScenarioBuilder ssxIngestionScenario = scenario("Push health metrics through SSX Ingestion endpoint")
            .forever()
            .on(exec(http("SSX Ingestion endpoint")
                    .post(SSX_INGESTION_ENDPOINT)
                    .body(ByteArrayBody(Snappy.compress(REMOTE_WRITE_REQUEST.toByteArray())))
            )
            .pause(Duration.ofMinutes(1)));

    {
        setUp(
                ssxIngestionScenario.injectOpen(atOnceUsers(1500))
        ).protocols(httpSSXProtocol)
                .maxDuration(Duration.ofMinutes(15));;
    }

    public SsxDataIngestion() throws IOException {
    }
}
