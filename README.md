# spark-jdbc-sink

A Spark metrics sink that uses the JDBC reporter from
[WSO2 Carbon Metrics](https://github.com/wso2/carbon-metrics) to write
metrics to databases using JDBC.

## Usage

Configure the sink using Spark's `metrics.properties`.
Here's an example using all available configuration options:

```
*.sink.jdbc.class=org.apache.spark.metrics.sink.JdbcSink
*.sink.jdbc.period=1
*.sink.jdbc.unit=seconds
*.sink.jdbc.user=spark
*.sink.jdbc.password=sparkmetrics
*.sink.jdbc.driver=org.postgresql.Driver
*.sink.jdbc.url=jdbc:postgresql://localhost/spark
*.sink.jdbc.filter=OnlyMyMetrics
*.sink.jdbc.name=MyMetrics
```

Example database schemas for common databases are included in the
[carbon-metrics](https://github.com/wso2/carbon-metrics/tree/master/features/org.wso2.carbon.metrics.jdbc.core.feature/resources/sql)
repository.
