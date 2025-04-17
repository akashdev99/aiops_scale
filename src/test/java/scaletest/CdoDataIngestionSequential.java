package scaletest;

import config.Config;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import model.TenantData;
import org.xerial.snappy.Snappy;
import utils.CdoAuthUtils;
import utils.RemoteWriteUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class CdoDataIngestionSequential extends Simulation {
    private static final String BASE_URL = "https://edge.scale.cdo.cisco.com";
    private static final String CDO_INGESTION_ENDPOINT = "/api/platform/ai-ops-data-ingest/v1/healthmetrics";

    private static List<TenantData> cdoTokens = new ArrayList<TenantData>();
    private static ArrayList<String> deviceUuidList = new ArrayList<String>();

    private static final int MAX_USERS = Config.getInt("max.users", 1);
    private static final int DURATION_MINUTES = Config.getInt("duration.minutes", 1);


    HttpProtocolBuilder httpCDOProtocol =
            http.baseUrl(BASE_URL)
                    .acceptHeader("application/json")
//                    .header("Authorization", session -> {
//                        String token = session.getString("authToken");
//                        return "Bearer " +token;
//                    })
                    .contentTypeHeader("application/x-protobuf")
                    .header("Content-Encoding", "snappy");

    ScenarioBuilder cdoIngestionScenario = scenario("Push health metrics directly through CDO Ingestion endpoint")
        .pause(1).exec(
                    repeat(DURATION_MINUTES).on(pace(60).exec(
                            repeat(MAX_USERS , "requestCounter").on(

                                    exec(
                                            // Step 1: Generate or fetch new token
                                            session -> {
                                                int counter = session.getInt("requestCounter");
                                                TenantData tokenData = cdoTokens.get(counter);
                                                String currentToken = tokenData.getToken();
                                                String currentTenantUuid = tokenData.getUuid();
                                                return session.set("authToken", currentToken).set("tenantUuid", currentTenantUuid );
                                            }
                                    ).
                                    exec(
                                                                http("CDO Ingestion endpoint")
                                .post(CDO_INGESTION_ENDPOINT)
                                .header("Authorization", session -> {
                                    String token = session.getString("authToken");
                                    return "Bearer " +token;
                                })
                                .body(ByteArrayBody(session -> {
//                                    loop through all tenants here
                                    try {
                                        return Snappy.compress(RemoteWriteUtils.constructRemoteWriteData(session.getString("tenantUuid") , deviceUuidList).toByteArray());
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                 })))
                            ))
//                        http("CDO Ingestion endpoint")
//                                .post(CDO_INGESTION_ENDPOINT)
//                                .body(ByteArrayBody(session -> {
//
//
//
////                                    loop through all tenants here
//                                    try {
//                                        return Snappy.compress(RemoteWriteUtils.constructRemoteWriteData(session.getString("tenantUuid") , deviceUuidList).toByteArray());
//                                    } catch (IOException e) {
//                                        throw new RuntimeException(e);
//                                    }
//                                 }))
                ));


        {
            try {
                cdoTokens = CdoAuthUtils.getCdoTokenList();
                deviceUuidList = CdoAuthUtils.getDeviceList();
                System.out.println(deviceUuidList);

            } catch (Exception e) {
                System.out.println("Failed to set tokens");
                e.printStackTrace();
            }
            System.out.println("Tokens are set tokens");
            setUp(
                    cdoIngestionScenario.injectOpen(atOnceUsers(1))
            ).protocols(httpCDOProtocol);
        }


    public CdoDataIngestionSequential() throws Exception {
    }
}
