package scaletest;

import config.Config;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import model.TenantData;
import org.xerial.snappy.Snappy;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import utils.CdoAuthUtils;
import utils.RemoteWriteUtils;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class CdoDataIngestion extends Simulation {
    private static final String BASE_URL = "https://edge.scale.cdo.cisco.com";
    private static final String CDO_INGESTION_ENDPOINT = "/api/platform/ai-ops-data-ingest/v1/healthmetrics";

    private static List<TenantData> cdoTokens = new ArrayList<TenantData>();
    private static ArrayList<String> deviceUuidList = new ArrayList<String>();
    private static final AtomicInteger tokenIndex = new AtomicInteger(0);

    private static final int MAX_USERS = Config.getInt("max.users", 1);
    private static final int DURATION_MINUTES = Config.getInt("duration.minutes", 1);

    private io.gatling.javaapi.core.Session setSessionTenantDetails(io.gatling.javaapi.core.Session session){
        int count = tokenIndex.getAndIncrement();
        int index = count % MAX_USERS;
        TenantData tokenData = cdoTokens.get(index);
        String currentToken = tokenData.getToken();
        String currentTenantUuid = tokenData.getUuid();
        return session.set("authToken", currentToken).set("tenantUuid", currentTenantUuid );
    }

    HttpProtocolBuilder httpCDOProtocol =
            http.baseUrl(BASE_URL)
                    .acceptHeader("application/json")
                    .header("Authorization", session -> {
                        String token = session.getString("authToken");
                        return "Bearer " +token;
                    })
                    .contentTypeHeader("application/x-protobuf")
                    .header("Content-Encoding", "snappy");

    ScenarioBuilder cdoIngestionScenario = scenario("Push health metrics directly through CDO Ingestion endpoint")
        .exec(this::setSessionTenantDetails).pause(1).exec(
                    repeat(DURATION_MINUTES).on(pace(60).exec(
                        http("CDO Ingestion endpoint")
                                .post(CDO_INGESTION_ENDPOINT)
                                .body(ByteArrayBody(session -> {
                                    try {
                                        return Snappy.compress(RemoteWriteUtils.constructRemoteWriteData(session.getString("tenantUuid") , deviceUuidList).toByteArray());
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                 }))
                )));


        {
            try {
                long maxHeap = Runtime.getRuntime().maxMemory() / (1024 * 1024);
                System.out.println("max size is" + maxHeap);
                cdoTokens = CdoAuthUtils.getCdoTokenList();
                deviceUuidList = CdoAuthUtils.getDeviceList();
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
