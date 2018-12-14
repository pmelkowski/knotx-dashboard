/*
 * Copyright (C) 2018 Knot.x Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

  public GraphiteOptions setAddress(String address) {
    this.address = address;
    return this;
  }

  public GraphiteOptions setPort(int port) {
    this.port = port;
    return this;
  }

  public String getAddress() {
    return address;
  }

  public int getPort() {
    return port;
  }

}
