# Knot.x Dashboard
This repository will contain Dashboard that provides online Knot.x monitoring.
Currently only a `metrics-sender` module is implemented.

## Metrics Sender
This simple Knot.x module sends [Vert.x Dropwizard metrics](https://github.com/vert-x3/vertx-dropwizard-metrics/blob/master/src/main/asciidoc/java/index.adoc) 
gathered by another Knot.x modules to [Graphite](http://graphite.readthedocs.io).
