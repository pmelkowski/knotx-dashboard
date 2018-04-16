# Knot.x Dashboard
This repository will contain Dashboard that provides online Knot.x monitoring.
Currently only a `metrics-sender` module is implemented.

## Metrics Sender
This simple Knot.x module sends [Vert.x Dropwizard metrics](https://github.com/vert-x3/vertx-dropwizard-metrics/blob/master/src/main/asciidoc/java/index.adoc) 
gathered by another Knot.x modules to [Graphite](http://graphite.readthedocs.io).


### How to prepare your application to run with metrics
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
      "artifactId": "metrics-sender",
      "version": "X.Y.Z",
      "included": true
    }
```
* Run `bin\knotx resolve` to resolve new dependencies and add `metrics-sender` to the instance classpath
* Add `metrics-sender` entry to `application.conf` modules list
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
        "formant": "conf",
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
METRICS_OPTS="-Dvertx.metrics.options.enabled=true -Dvertx.metrics.options.registryName=vertx-dw"
```
* Optionally configure other metrics sender parameters in `conf/dashboardStack.conf`