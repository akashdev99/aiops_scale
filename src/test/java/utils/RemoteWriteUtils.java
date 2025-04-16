package utils;

import config.Config;
import remotewrite.PromMetric;
import remotewrite.Remote;
import remotewrite.Types;
import scala.collection.immutable.List;
import scaletest.CdoDataIngestion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RemoteWriteUtils {
    private static final int NO_OF_DEVICES_PER_USER = Config.getInt("devices.per.user", 1);
    private static final int NO_OF_FMC_PER_USER = Config.getInt("fmc.per.user", 1);

    private static Map<String , String> convertLabelToMap(String label){
        Map<String , String> labels = new HashMap<String , String >();
        if(label==null){
            return labels;
        }
        Arrays.stream(label.replaceAll("[{}'\"]", "").split(",")).map(s -> s.split("=")).forEach(s -> {
            if(s.length<=1){
                // empty label values handling
                labels.put(s[0].strip(),"");
            }else{
                labels.put(s[0].strip(), s[1].strip());
            }
        });
        return labels;
    }

    private static void addTimeSeriesToRemoteWriteBuilder(Remote.WriteRequest.Builder remoteWriteBuilder , String file , int dataSourceCount, String tenantUuid , ArrayList<String> deviceList){
        try (InputStream inputStream = CdoDataIngestion.class.getClassLoader().getResourceAsStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            Pattern pattern = Pattern.compile("(\\S+)\\s*\\{([^}]*)\\}");
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                while(matcher.find()){
                    String metricName = matcher.group(1);
                    Map<String , String> labels = convertLabelToMap(matcher.group(2));

                    for(int i = 0; i< dataSourceCount; i++){
                        if(labels.containsKey("uuid")){
                            // TODO : Cannot be random . It has to be a fixed set of device UUIDs per tenant
                            String deviceUuid = !deviceList.isEmpty() ? deviceList.get(i) : UUID.randomUUID().toString();
                            labels.put("uuid", deviceUuid);
                        }

                        PromMetric metric = PromMetric.builder()
                                .name(metricName)
                                .labels(labels)
                                .value(5)
                                .currentTime(System.currentTimeMillis())
                                .build();
                        Types.TimeSeries ts = metric.convToTimeSeries(UUID.fromString(tenantUuid));
                        remoteWriteBuilder.addTimeseries(ts);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Remote.WriteRequest constructRemoteWriteData(String tenantUuid , ArrayList<String> deviceList){
        Remote.WriteRequest.Builder remoteWriteBuilder = Remote.WriteRequest.newBuilder();

        // FMC metrics
        addTimeSeriesToRemoteWriteBuilder(remoteWriteBuilder , "fmc_metrics.txt" , NO_OF_FMC_PER_USER,  tenantUuid ,new ArrayList<String>() );
        // FTD metrics
        addTimeSeriesToRemoteWriteBuilder(remoteWriteBuilder , "ftd_metrics.txt", NO_OF_DEVICES_PER_USER , tenantUuid ,deviceList  );
        return remoteWriteBuilder.build();
    }
}
