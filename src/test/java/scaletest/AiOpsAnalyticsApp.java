package scaletest;

import config.Config;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import utils.CdoUtils;

import static io.gatling.javaapi.core.CoreDsl.scenario;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class AiOpsAnalyticsApp extends Simulation {
    private static final String ANALYTICS_BASE_URL = "https://edge.scale.cdo.cisco.com/api/platform/ai-ops-analytics/v1";
    private static final String ALERT_TRIGGER = "/alert-trigger";

    private static final String API_TOKENS_FILE = Config.get("api.tokens.file", "api_user_tokens.csv");

    HttpProtocolBuilder httpDataIngest =
            http.baseUrl(ANALYTICS_BASE_URL)
                    .acceptHeader("application/json").contentTypeHeader("application/json");;

    private void cleanupInsightsAllTenants(int threads) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        CdoUtils.getCdoTokenList().forEach(tokenData -> {
            String token = tokenData.getToken();
            executor.submit(() -> {
                try {
                    CdoUtils.cleanInsightForTenant(token);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace(); // or use proper logging
                }
            });
        });

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.MINUTES); // waits for all tasks to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore interrupt status
            e.printStackTrace();
        }
    }


    //  Scenarios
    ScenarioBuilder elephantFlowTriggerScenario = scenario("Elephant Flow Scale Test")
        .feed(csv(API_TOKENS_FILE).eager().queue()).exec(
                http("CDO Ingestion endpoint")
                        .post(ALERT_TRIGGER)
                        .header("Authorization", session -> {
                            String token = session.getString("API_USER_TOKEN");
                            return "Bearer " +token;
                        })
                        .body(RawFileBody("elephant_flow_trigger_payload.json"))
        );

    {
        cleanupInsightsAllTenants(50);

        setUp(
                elephantFlowTriggerScenario.injectOpen(atOnceUsers(500))
        ).protocols(httpDataIngest);
    }

    public AiOpsAnalyticsApp() throws  Exception {

    }
}
