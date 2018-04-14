package io.knotx.metrics;


import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import io.knotx.metrics.MetricsSenderConfiguration.GraphiteConfiguration;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class SenderVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(SenderVerticle.class);

  private MetricsSenderConfiguration configuration;

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    configuration = new MetricsSenderConfiguration(config());
  }

  @Override
  public void start() throws Exception {
    LOGGER.info("Starting <{}>", this.getClass().getSimpleName());
    LOGGER.debug("Metrics config: {}", configuration);

    MetricRegistry dropwizardRegistry = SharedMetricRegistries.getOrCreate(
        System.getProperty("vertx.metrics.options.registryName")
    );
    final GraphiteConfiguration graphiteConfiguration = configuration.getGraphite();
    final Graphite graphite = new Graphite(
        new InetSocketAddress(graphiteConfiguration.getAddress(), graphiteConfiguration.getPort()));
    final GraphiteReporter reporter = GraphiteReporter.forRegistry(dropwizardRegistry)
        .prefixedWith(configuration.getPrefix())
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .filter(MetricFilter.ALL)
        .build(graphite);
    reporter.start(configuration.getPollsPeriod(), TimeUnit.SECONDS);
  }

  @Override
  public void stop() throws Exception {
    //Nothing to do
  }
}
