By default, MORP exposes [Prometheus](https://prometheus.io) metrics at
[http://localhost:8081/actuator/prometheus](http://localhost:8081/actuator/prometheus).

Metrics can be configured to include the tenant as an additional dimension:

```yaml
morp:
  metrics:
    tenantdimension:
      enabled: true
```

!!! warning

    When having many tenants, enabling this feature can lead to a vast amount of timeseries being created in Prometheus!
