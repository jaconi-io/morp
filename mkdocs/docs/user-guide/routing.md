# Routing

Routing in MORP uses [Spring Cloud Gateway](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/). A
basic route could be configured like this:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: my_route
          uri: https://example.org
          predicates:
            - Path=/{tenant}/example
```

With this routing configuration, MORP will forward any traffic arriving at `/{tenant}/example` to
[https://example.com](https://example.com). `{tenant}` is a special URI template variable used by MORP to determine the
tenant ID.

## Tenant ID Extraction

A tenant is how MORP names a customer, an application or a department. Each tenant is identified by a unique tenant ID.
To figure out the tenant for a request, MORP uses predicates. MORP supports the following predicates:

| Tenant Extractor   | Example Configuration                                                                                                                           | Example Request               | Example Tenant |
|--------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------|----------------|
| `Host`             | <pre>Host={tenant}.example.com</pre>                                                                                                            | foo.example.com               | foo            |
| `Host`             | <pre>Host={stage}-{tenant}.example.com                                                                                                          | dev-foo.example.com           | foo            |
| `Path`             | <pre>Path=example.com/tenant/{tenant}</pre>                                                                                                     | example.com/tenant/foo        | foo            |
| `Path`             | <pre>Path=example.com/api/{version}/tenant/{tenant}</pre>                                                                                       | example.com/api/v1/tenant/foo | foo            |
| `TenantFromHost`   | <pre>name: TenantFromHost<br/>args:<br/>  patterns:<br/>    - static.localtest.me<br/>    - another-static.localtest.me<br/>  tenant: foo</pre> | static.example.com            | foo            |
| `TenantFromHeader` | <pre>TenantFromHeader=X-Tenant-ID,{tenant}</pre>                                                                                                | X-Tenant-ID: foo              | foo            |

Predicates are configured in the `spring.cloud.gateway.routes[*].predicates` section and are applied per route. When no
tenant is extracted for a request, the request will fail.

The extracted tenant ID is used to determine how the request will be authenticated. See
[Configuration](../configuration.md) on how to configure authentication.
