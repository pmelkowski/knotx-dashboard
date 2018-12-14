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
package io.knotx.metrics;


import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.graphite.Graphite;
import io.knotx.metrics.graphite.GraphiteOptions;
import io.knotx.metrics.graphite.GraphiteReporter;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class SenderVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(SenderVerticle.class);

  private MetricsSenderOptions options;

  private GraphiteReporter reporter;

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    options = new MetricsSenderOptions(config());
  }

  @Override
  public void start() throws Exception {
    LOGGER.info("Starting <{}>", this.getClass().getSimpleName());
    LOGGER.debug("Metrics config: {}", options);

    MetricRegistry dropwizardRegistry = SharedMetricRegistries.getOrCreate(
        System.getProperty("vertx.metrics.options.registryName")
    );
    final GraphiteOptions graphiteOptions = options.getGraphite();
    final Graphite graphite = new Graphite(
        new InetSocketAddress(graphiteOptions.getAddress(), graphiteOptions.getPort()));
    reporter = GraphiteReporter.useRegistry(dropwizardRegistry)
        .prefixedWith(options.getPrefix())
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .filter(MetricFilter.ALL)
        .build(graphite);
    reporter.start(options.getPollsPeriod(), TimeUnit.MILLISECONDS);
  }

  @Override
  public void stop() throws Exception {
    if (reporter != null) {
      reporter.stop();
    }
  }
}
