# Knot.x Dashboard
This repository will contain Dashboard that provides online Knot.x monitoring.
Currently only a `metrics-sender` module is implemented.

## Metrics Sender
This simple Knot.x module sends [Vert.x Dropwizard metrics](https://github.com/vert-x3/vertx-dropwizard-metrics/blob/master/src/main/asciidoc/java/index.adoc) 
gathered by another Knot.x modules to [Graphite](http://graphite.readthedocs.io).


##### How to prepare your application to run with metrics
This guide assumes that you already have running Knot.x instance that is setup with [Knot.x stack](https://github.com/Knotx/knotx-stack).

**Prerequisites**
```
 - Graphite instance setup
 - (Optional) Grafana setup
```

Do the following steps to start sending Knot.x application metrics to Graphite:
* Add `knotx-stack` dependency to `knotx-stack.json`:
```json
    {
      "groupId": "io.knotx",
      "artifactId": "knotx-dashboard",
      "version": "X.Y.Z",
      "included": true
    }
```
* Run `bin\knotx resolve` to resolve new dependencies and add `metrics-sender` to the instance classpath
* Add `metricSender` entry to `application.conf` modules list
```
"metricsSender=io.knotx.metrics.SenderVerticle"
```
* Copy `conf/dashboardStack.conf` to application `conf` folder
* Add `dashboardStack.conf` to application stores defined in `bootstrap.json`, e.g.
```json
...
    "stores": [
      ...
    
      {
        "type": "file",
        "format": "conf",
        "config": {
          "path": "${KNOTX_HOME}/conf/dashboardStack.conf"
        }
      }
    ]
...
```
* Define Graphite connection in `conf/dashboardStack.conf` (by default it's set to `localhost:2003`).
* Uncomment `METRICS_OPTS` line in `bin/knotx`:
```cmd
METRICS_OPTS="-Dvertx.metrics.options.enabled=true -Dvertx.metrics.options.registryName=knotx-dropwizard-registry"
```
* Optionally add `-Dknotx.metrics.options.prefix=<custom prefix>` to `METRICS_OPTS`
* Optionally configure other metrics sender parameters in `conf/includes/metricsSender.conf`
* Optionally configure metrics to gather event-bus data:
  - copy `conf/metrics-options.json` to `conf` directory of Knot.x instance
  - append `METRICS_OPTS` in `bin/knotx` with `-Dvertx.metrics.options.configPath=conf/metrics-options.json`
  - define datasource endpoint entries in `monitoredHttpClientEndpoints` section to monitor its traffic, e.g.
  ```json
      {
        "alias": "googleapis",
        "value": "www.googleapis.com:443"
      }
  ```
  - for more information about metrics settings see [vertx-dropwizard-metrics docs](https://github.com/vert-x3/vertx-dropwizard-metrics/blob/master/src/main/asciidoc/java/index.adoc)

#### Gathering and displaying metrics
Before running Knot.x with metrics you need a Graphite instance that metrics will be pushed to.
The easiest way to setup Graphite is [docker](https://github.com/graphite-project/docker-graphite-statsd#change-the-configuration).
To display metrics that are stored in Graphite, you may use [Grafana](https://grafana.com/). You may also run it
using [docker](https://hub.docker.com/r/grafana/grafana/).
You may find example Grafana board (in form of `JSON` file to import to Grafana) that displays Knot.x metrics 
in `misc/example-grafana-board.json` file in this repo.

## ToDo
Currently `metrics-sender` module enables sending ootb Vert.x metrics to Graphite. More metrics stores will be added in the future.
Also there will be a dedicated board that will enable live preview on Knot.x instance health.
