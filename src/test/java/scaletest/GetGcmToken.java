package scaletest;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import org.xerial.snappy.Snappy;
import remotewrite.PromMetric;
import remotewrite.Remote;
import remotewrite.Types;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

/**
 * This sample is based on our official tutorials:
 * <ul>
 *   <li><a href="https://docs.gatling.io/tutorials/recorder/">Gatling quickstart tutorial</a>
 *   <li><a href="https://docs.gatling.io/tutorials/advanced/">Gatling advanced tutorial</a>
 * </ul>
 */
public class GetGcmToken extends Simulation {

    private static final PromMetric METRIC = PromMetric.builder()
            .name("scale_test")
            .labels(Map.of("type", "some_type"))
            .value(2)
            .currentTime(System.currentTimeMillis())
            .build();
    private static final String BASE_URL = "https://edge.staging.cdo.cisco.com";



//    private static final String SSX_INGESTION_ENDPOINT = "/api/platform/ssx-exchange/proxy/ai-ops/api/platform/ai-ops-data-ingest/v1/healthmetrics";
//    private static final String SSX_TOKEN = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJTU0UiLCJzdWIiOiJDb25uZWN0b3IiLCJleHAiOjE3NDEwNzM1NTAsImlhdCI6MTczMzI5NzU1MCwianRpIjoiNmY0OWUzODQtYjQ5OS00YmU1LTlhY2ItNjcwNDdhY2NjNGM2IiwiYXVkIjpbIlNTRSJdLCJpZCI6ImViMjk3ZDFmLTU1NWUtNDcyNy1hMjBkLTM1MDMwOWRhMDBmNSIsIm9yZ0lkIjoiMzhjYjZkMTQtMWUyMy00NDA4LWI3ODUtYTNhYWE5MDk2ZjgxIiwic3Vic2NyaXB0aW9ucyI6WyJyYXctZXZlbnQtYWNjZXNzIiwiY2RvLWV2ZW50LWFjY2VzcyIsIklURC1CYXNlIiwibmdmdyIsImh5YnJpZC0yLjAiXSwiand0VHlwZSI6ImFjY2VzcyIsInZlcnNpb24iOiJ2MiJ9.TsZqVGcQRjvEpo4pQ6_xlaY69mVj2rFNWA54Un9U_Ml5WSGIXtauLaQGbDZ36tZ8MR_2jUUwT4pdCkEiP_u_QBpQmtPyMkjVUFwJK6TyBR1rZ-2WUX7Qq2slrNkaph0FEzH4lR8FNDikfgcNxIQQyvbu7ELFhHmHEcKczGFQirhR6dNvN8PQY4wmA5m0fZwaNB3Rh6eSF6539z2HJx5JjBh1mOm9ibUDqvKg4N01HvHjL_REfOuxzOcykrT4QQdl4V50ZcwmBrmTYMDsOfFS4k8yPubj70raVnKUZvvc3Lnwj-hxiTqoHdmbyTLnVcFAwfhUZm6PkzjWzke2Ld2mLw"; // Replace with the actual token
//    private static final Types.TimeSeries ts = METRIC.convToTimeSeries(UUID.fromString("38cb6d14-1e23-4408-b785-a3aaa9096f81"));
//    private static final String CDO_INGESTION_ENDPOINT = "/api/platform/ai-ops-data-ingest/v1/healthmetrics";
    private static final String CDO_TOKEN = "Bearer eyJraWQiOiIwIiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJ2ZXIiOiIwIiwicm9sZXMiOlsiUk9MRV9TVVBFUl9BRE1JTiJdLCJhbXIiOiJzYW1sIiwiaXNzIjoiaXRkIiwiY2x1c3RlcklkIjoiMSIsInN1YmplY3RUeXBlIjoidXNlciIsImNsaWVudF9pZCI6ImFwaS1jbGllbnQiLCJwYXJlbnRJZCI6IjM4Y2I2ZDE0LTFlMjMtNDQwOC1iNzg1LWEzYWFhOTA5NmY4MSIsInNjb3BlIjpbInRydXN0IiwicmVhZCIsIjM4Y2I2ZDE0LTFlMjMtNDQwOC1iNzg1LWEzYWFhOTA5NmY4MSIsIndyaXRlIl0sImlkIjoiYjcyNWU0ZmEtYmU0OC00Y2U1LTkxZDYtNzc3YmIyNzgwNWMyIiwiZXhwIjozODgwODYwMTcxLCJpYXQiOjE3MzMzNzY1ODQsImp0aSI6ImE0ZWM2MzdkLTVkYjMtNDc0NC05NjJkLTFkYzY5ZTZjY2IxZCJ9.EszJXCRRiKF4xVHUmuO-lIRyZU-CUZ4cvYuh7OJCBX_hK16QA4-EvMdBUKzO08ibTzciePbJ1fLATXVvgTLEac85AEhSOkZI6GEDUwhMsPouECBYUQaICyjrEImolnLhaI3SgkQd3lAW_WBeyGOTrorGqq5lBOAMizSLXOk2gIjoIk2X_bsFT-9_DJcJnwMLPJEhtHaDGIcJj7c86LKjqpGuL5MvvqdhX7bnMcM_-q8LuHChnaoqKwNx37aTJATxnfpY6KcG9iRQzfbs-G8tLjTMK0_K8ET54o6dRP7XeOyK8IPUzZ4gpzio0qVTlHl5fySlGItPLqVAEsidetcYMA"; // Replace with the actual token
    private static final String CDO_GCM_TOKEN_ENDPOINT = "/api/platform/ai-ops-tenant-services/v1/timeseries-stack";
//    private static final Types.TimeSeries ts = METRIC.convToTimeSeries(UUID.fromString("f0c40a5b-85ea-433d-868b-24d922e48d33"));


    HttpProtocolBuilder httpCDOProtocol =
            http.baseUrl(BASE_URL)
                    .acceptHeader("application/json")
                    .authorizationHeader(CDO_TOKEN)
                    .contentTypeHeader("application/x-protobuf")
                    .header("Content-Encoding", "snappy");


    ScenarioBuilder tenantServicesGetTimeseriesStackScenario = scenario("Get GCM token")
            .exec(http("CDO Get GCM token endpoint")
                    .get(CDO_GCM_TOKEN_ENDPOINT)
            );

    {
        setUp(
                tenantServicesGetTimeseriesStackScenario.injectOpen(atOnceUsers(1000))
        ).protocols(httpCDOProtocol);
    }

    public GetGcmToken() throws IOException {
    }
}
