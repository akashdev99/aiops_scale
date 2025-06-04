package scaletest;

import config.Config;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import org.xerial.snappy.Snappy;
import java.io.IOException;
import java.util.*;
import utils.CdoUtils;
import utils.RemoteWriteUtils;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class CdoDataIngestion extends Simulation {
    private static final String BASE_URL = "https://edge.scale.cdo.cisco.com";
    private static final String CDO_INGESTION_ENDPOINT = "/api/platform/ai-ops-data-ingest/v1/healthmetrics";

    private static ArrayList<String> deviceUuidList = new ArrayList<>();

    private static final int MAX_USERS = Config.getInt("max.users", 1);
    private static final int DURATION_MINUTES = Config.getInt("duration.minutes", 1);
    private static final String API_TOKENS_FILE = Config.get("api.tokens.file", "api_user_tokens.csv");

    HttpProtocolBuilder httpCDOProtocol =
            http.baseUrl(BASE_URL)
                    .acceptHeader("application/json")
                    .contentTypeHeader("application/x-protobuf")
                    .header("Content-Encoding", "snappy");

    ScenarioBuilder cdoIngestionScenario = scenario("Push health metrics directly through CDO Ingestion endpoint")
            .feed(csv(API_TOKENS_FILE).eager().queue()).pause(1).exec(
                    repeat(DURATION_MINUTES).on(pace(60).exec(
                            http("CDO Ingestion endpoint")
                                    .post(CDO_INGESTION_ENDPOINT)
                                    .header("Authorization", session -> {
                                        String token = session.getString("API_USER_TOKEN");
                                        return "Bearer " +token;
                                    })
                                    .body(ByteArrayBody(session -> {
                                        try {
                                            return Snappy.compress(RemoteWriteUtils.constructRemoteWriteData(session.getString("tenant_id") , deviceUuidList).toByteArray());
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }))
                    )));


        {
            try {
                long maxHeap = Runtime.getRuntime().maxMemory() / (1024 * 1024);
                System.out.println("max size is" + maxHeap);
                deviceUuidList = CdoUtils.getDeviceList();
            } catch (Exception e) {
                System.out.println("Failed to set tokens");
                e.printStackTrace();
            }
            System.out.println("Tokens are set");
            setUp(
                    cdoIngestionScenario.injectOpen(atOnceUsers(MAX_USERS))
            ).protocols(httpCDOProtocol);
        }


    public CdoDataIngestion() throws Exception {
    }
}
