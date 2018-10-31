package io.knotx.metrics;


import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import io.knotx.metrics.graphite.GraphiteOptions;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class SenderVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(SenderVerticle.class);

  private static final String REGISTRY_PROPERTY = "vertx.metrics.options.registryName";
  private static final String PREFIX_PROPERTY = "knotx.metrics.options.prefix";

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

    final String registryName = System.getProperty(REGISTRY_PROPERTY);
    if (registryName == null) {
      LOGGER.warn("Property '{}' not set. Exiting.", REGISTRY_PROPERTY);
      return;
    }

    MetricRegistry dropwizardRegistry = SharedMetricRegistries.getOrCreate(registryName);
    final GraphiteOptions graphiteOptions = options.getGraphite();
    final Graphite graphite = new Graphite(
        new InetSocketAddress(graphiteOptions.getAddress(), graphiteOptions.getPort()));
    reporter = GraphiteReporter.forRegistry(dropwizardRegistry)
        .prefixedWith(System.getProperty(PREFIX_PROPERTY, options.getPrefix()))
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
