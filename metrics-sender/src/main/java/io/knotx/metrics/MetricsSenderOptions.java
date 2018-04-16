package io.knotx.metrics;

import io.knotx.metrics.graphite.GraphiteOptions;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true, publicConverter = false)
public class MetricsSenderOptions {

  private static final String DEFAULT_METRICS = "io.knotx";
  private static final int DEFAULT_POLLS_PERIOD_MS = 1000;

  private GraphiteOptions graphite;
  private String prefix;
  private int pollsPeriod;

  public MetricsSenderOptions() {
    init();
  }

  public MetricsSenderOptions(JsonObject jsonObject) {
    init();
    MetricsSenderOptionsConverter.fromJson(jsonObject, this);
  }

  private void init() {
    graphite = new GraphiteOptions();
    prefix = DEFAULT_METRICS;
    pollsPeriod = DEFAULT_POLLS_PERIOD_MS;
  }

  public MetricsSenderOptions setGraphite(GraphiteOptions graphite) {
    this.graphite = graphite;
    return this;
  }

  public MetricsSenderOptions setPrefix(String prefix) {
    this.prefix = prefix;
    return this;
  }

  public MetricsSenderOptions setPollsPeriod(int pollsPeriod) {
    this.pollsPeriod = pollsPeriod;
    return this;
  }

  public GraphiteOptions getGraphite() {
    return graphite;
  }

  public String getPrefix() {
    return prefix;
  }

  public int getPollsPeriod() {
    return pollsPeriod;
  }
}
