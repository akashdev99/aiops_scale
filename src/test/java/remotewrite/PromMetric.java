package remotewrite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
public class PromMetric {
  private String name;
  private Map<String, String> labels;
  private long value;
  private long currentTime;
  private static final String METRIC_NAME_LABEL = "__name__";
  private static final String TENANT_UUID_LABEL = "tenant_uuid";

  public PromMetric(String name, Map<String, String> labels) {
    this.name = name;
    this.labels = labels;
  }

  public Types.TimeSeries convToTimeSeries(UUID tenantUuid) {
    Types.TimeSeries.Builder timeSeriesBuilder = Types.TimeSeries.newBuilder();
    addLables(timeSeriesBuilder, tenantUuid);
    addSample(timeSeriesBuilder);
    return timeSeriesBuilder.build();
  }

  private void addLables(Types.TimeSeries.Builder timeSeriesBuilder, UUID tenantUuid) {
    List<Types.Label> timeSerieslabels = new ArrayList<>();
    // adding metric name
    timeSerieslabels.add(
        Types.Label.newBuilder().setName(METRIC_NAME_LABEL).setValue(name).build());
    // adding tenant uuid
    timeSerieslabels.add(
        Types.Label.newBuilder()
            .setName(TENANT_UUID_LABEL)
            .setValue(tenantUuid.toString())
            .build());
    for (Map.Entry<String, String> entry : labels.entrySet()) {
      timeSerieslabels.add(
          Types.Label.newBuilder().setName(entry.getKey()).setValue(entry.getValue()).build());
    }
    timeSeriesBuilder.addAllLabels(timeSerieslabels);
  }

  private void addSample(Types.TimeSeries.Builder timeSeriesBuilder) {
    long timestamp = currentTime == 0 ? System.currentTimeMillis() : currentTime;
    Types.Sample sample = Types.Sample.newBuilder().setTimestamp(timestamp).setValue(value).build();
    timeSeriesBuilder.addSamples(sample);
  }
}
