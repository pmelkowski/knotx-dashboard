/*
 * Copyright (C) 2018, Coda Hale, Yammer Inc.
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
/*
 * This code was partially copied from metrics-graphite project by Coda Hale, Yammer Inc.
 * The copied code:
 *      GraphiteReporter.Builder class and its methods
 * The new code:
 *      GraphiteReporter's overridden and new methods
 */
package io.knotx.metrics.graphite;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteSender;

public class GraphiteReporter extends com.codahale.metrics.graphite.GraphiteReporter {

  /**
   * Returns a new {@link Builder} for {@link GraphiteReporter}.
   *
   * @param registry the registry to report
   * @return a {@link Builder} instance for a {@link GraphiteReporter}
   */
  public static Builder useRegistry(MetricRegistry registry) {
      return new Builder(registry);
  }

  /**
   * A builder for {@link GraphiteReporter} instances. Defaults to not using a prefix, using the
   * default clock, converting rates to events/second, converting durations to milliseconds, and
   * not filtering metrics.
   */
  public static class Builder {
      private final MetricRegistry registry;
      private Clock clock;
      private String prefix;
      private TimeUnit rateUnit;
      private TimeUnit durationUnit;
      private MetricFilter filter;
      private ScheduledExecutorService executor;
      private boolean shutdownExecutorOnStop;
      private Set<MetricAttribute> disabledMetricAttributes;

      private Builder(MetricRegistry registry) {
          this.registry = registry;
          this.clock = Clock.defaultClock();
          this.prefix = null;
          this.rateUnit = TimeUnit.SECONDS;
          this.durationUnit = TimeUnit.MILLISECONDS;
          this.filter = MetricFilter.ALL;
          this.executor = null;
          this.shutdownExecutorOnStop = true;
          this.disabledMetricAttributes = Collections.emptySet();
      }

      /**
       * Specifies whether or not, the executor (used for reporting) will be stopped with same time with reporter.
       * Default value is true.
       * Setting this parameter to false, has the sense in combining with providing external managed executor via {@link #scheduleOn(ScheduledExecutorService)}.
       *
       * @param shutdownExecutorOnStop if true, then executor will be stopped in same time with this reporter
       * @return {@code this}
       */
      public Builder shutdownExecutorOnStop(boolean shutdownExecutorOnStop) {
          this.shutdownExecutorOnStop = shutdownExecutorOnStop;
          return this;
      }

      /**
       * Specifies the executor to use while scheduling reporting of metrics.
       * Default value is null.
       * Null value leads to executor will be auto created on start.
       *
       * @param executor the executor to use while scheduling reporting of metrics.
       * @return {@code this}
       */
      public Builder scheduleOn(ScheduledExecutorService executor) {
          this.executor = executor;
          return this;
      }

      /**
       * Use the given {@link Clock} instance for the time.
       *
       * @param clock a {@link Clock} instance
       * @return {@code this}
       */
      public Builder withClock(Clock clock) {
          this.clock = clock;
          return this;
      }

      /**
       * Prefix all metric names with the given string.
       *
       * @param prefix the prefix for all metric names
       * @return {@code this}
       */
      public Builder prefixedWith(String prefix) {
          this.prefix = prefix;
          return this;
      }

      /**
       * Convert rates to the given time unit.
       *
       * @param rateUnit a unit of time
       * @return {@code this}
       */
      public Builder convertRatesTo(TimeUnit rateUnit) {
          this.rateUnit = rateUnit;
          return this;
      }

      /**
       * Convert durations to the given time unit.
       *
       * @param durationUnit a unit of time
       * @return {@code this}
       */
      public Builder convertDurationsTo(TimeUnit durationUnit) {
          this.durationUnit = durationUnit;
          return this;
      }

      /**
       * Only report metrics which match the given filter.
       *
       * @param filter a {@link MetricFilter}
       * @return {@code this}
       */
      public Builder filter(MetricFilter filter) {
          this.filter = filter;
          return this;
      }

      /**
       * Don't report the passed metric attributes for all metrics (e.g. "p999", "stddev" or "m15").
       * See {@link MetricAttribute}.
       *
       * @param disabledMetricAttributes a {@link MetricFilter}
       * @return {@code this}
       */
      public Builder disabledMetricAttributes(Set<MetricAttribute> disabledMetricAttributes) {
          this.disabledMetricAttributes = disabledMetricAttributes;
          return this;
      }

      /**
       * Builds a {@link GraphiteReporter} with the given properties, sending metrics using the
       * given {@link GraphiteSender}.
       * <p>
       * Present for binary compatibility
       *
       * @param graphite a {@link Graphite}
       * @return a {@link GraphiteReporter}
       */
      public GraphiteReporter build(Graphite graphite) {
          return build((GraphiteSender) graphite);
      }

      /**
       * Builds a {@link GraphiteReporter} with the given properties, sending metrics using the
       * given {@link GraphiteSender}.
       *
       * @param graphite a {@link GraphiteSender}
       * @return a {@link GraphiteReporter}
       */
      public GraphiteReporter build(GraphiteSender graphite) {
          return new GraphiteReporter(registry,
                  graphite,
                  clock,
                  prefix,
                  rateUnit,
                  durationUnit,
                  filter,
                  executor,
                  shutdownExecutorOnStop,
                  disabledMetricAttributes);
      }
  }

  private Map<String, Object> lastGauges;
  private Map<String, Long> lastCounters;
  private Map<String, Long> lastHistograms;
  private Map<String, Long> lastMeters;
  private Map<String, Long> lastTimers;

  /**
   * Creates a new {@link GraphiteReporter} instance.
   *
   * @param registry               the {@link MetricRegistry} containing the metrics this
   *                               reporter will report
   * @param graphite               the {@link GraphiteSender} which is responsible for sending metrics to a Carbon server
   *                               via a transport protocol
   * @param clock                  the instance of the time. Use {@link Clock#defaultClock()} for the default
   * @param prefix                 the prefix of all metric names (may be null)
   * @param rateUnit               the time unit of in which rates will be converted
   * @param durationUnit           the time unit of in which durations will be converted
   * @param filter                 the filter for which metrics to report
   * @param executor               the executor to use while scheduling reporting of metrics (may be null).
   * @param shutdownExecutorOnStop if true, then executor will be stopped in same time with this reporter
   */
  public GraphiteReporter(MetricRegistry registry, GraphiteSender graphite, Clock clock,
      String prefix, TimeUnit rateUnit, TimeUnit durationUnit, MetricFilter filter,
      ScheduledExecutorService executor, boolean shutdownExecutorOnStop,
      Set<MetricAttribute> disabledMetricAttributes) {
    super(registry, graphite, clock, prefix, rateUnit, durationUnit, filter, executor,
        shutdownExecutorOnStop, disabledMetricAttributes);
    lastGauges = new HashMap<>();
    lastCounters = new HashMap<>();
    lastHistograms = new HashMap<>();
    lastMeters = new HashMap<>();
    lastTimers = new HashMap<>();
  }

  @Override
  public synchronized void report(@SuppressWarnings("rawtypes") SortedMap<String, Gauge> gauges,
      SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms,
      SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
    super.report(
        removeRepeating(gauges, lastGauges, Gauge::getValue),
        removeRepeating(counters, lastCounters, Counter::getCount),
        removeRepeating(histograms, lastHistograms, Histogram::getCount),
        removeRepeating(meters, lastMeters, Meter::getCount),
        removeRepeating(timers, lastTimers, Timer::getCount));
  }

  protected static <M extends Metric, V> SortedMap<String, M> removeRepeating(
      SortedMap<String, M> currentMetrics, Map<String, V> previousValues, 
      Function<M, V> valueFunction) {
    SortedMap<String, M> result = new TreeMap<>(currentMetrics);
    Map<String, V> currentValues = currentMetrics.entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, e -> valueFunction.apply(e.getValue())));
    currentValues.entrySet().stream()
      .filter(e -> previousValues.containsKey(e.getKey()))
      .filter(e -> Objects.equals(previousValues.get(e.getKey()), e.getValue()))
      .forEach(e -> result.remove(e.getKey()));
    previousValues.putAll(currentValues);
    return result;
  }

}
