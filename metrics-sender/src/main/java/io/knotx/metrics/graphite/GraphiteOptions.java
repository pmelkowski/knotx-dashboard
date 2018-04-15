package io.knotx.metrics.graphite;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true, publicConverter = false)
public class GraphiteOptions {

  private String address;
  private int port;

  public GraphiteOptions() {
    //nothing here
  }

  public GraphiteOptions(JsonObject config) {
    GraphiteOptionsConverter.fromJson(config, this);
  }

  public String getAddress() {
    return address;
  }

  public int getPort() {
    return port;
  }

}
