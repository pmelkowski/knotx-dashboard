package io.knotx.metrics;

import io.vertx.core.json.JsonObject;

public class MetricsSenderConfiguration {

  private final GraphiteConfiguration graphite;
  private final String prefix;
  private final int pollsPeriod;

  MetricsSenderConfiguration(JsonObject config) {
    prefix = config.getString("prefix", "io.knotx");
    graphite = new GraphiteConfiguration(config.getJsonObject("graphite", new JsonObject()));
    pollsPeriod = config.getInteger("pollsPeriod", 10);
  }

  GraphiteConfiguration getGraphite() {
    return graphite;
  }

  String getPrefix() {
    return prefix;
  }

  int getPollsPeriod() {
    return pollsPeriod;
  }

  static class GraphiteConfiguration {

    private String address;
    private int port;

    GraphiteConfiguration(JsonObject config) {
      this.address = config.getString("address", "localhost");
      this.port = config.getInteger("port", 2003);
    }

    String getAddress() {
      return address;
    }

    int getPort() {
      return port;
    }

    @Override
    public String toString() {
      return "{" +
          "address: '" + address + '\'' +
          ", port: " + port +
          '}';
    }
  }

  @Override
  public String toString() {
    return "MetricsKnotConfiguration={" +
        "graphite: " + graphite +
        ", prefix: '" + prefix + '\'' +
        ", pollsPeriod: " + pollsPeriod +
        '}';
  }
}
