![MORP](morp.svg)

# THIS REPO IS IN PRE-RELEASE, DO NOT USE IN PRODUCTION

MORP is a multi-tenant OpenID Connect reverse proxy. With MORP you can protect web services and applications that do not
support authentication on their own. MORP takes care of the OpenID Connect "dance" using common identity providers
(e.g. Google, GitHub, Okta, and Keycloak).

# Why MORP

There are several excellent OAuth2 / OIDC proxies out there today that can be used to protect you backend apps. However,
existing implementations typically only support a single tenant (with a single identity provider). MORP aims at making
**multi-tenancy** a first class citizen so that you can run apps on behalf of different tenants that may be using
different IDPs to authenticate users.

## Key Features

* Flexible tenant identification based host name, request paths or headers
* Fully templatized IDP configuration
* Dynamic discovery of IDP secrets
* **Planned** Persistence of IDP state for high availability

## Tech Stack

We strongly believe in open source to build great things on top established components and frameworks. Implementing
OIDC and a high performance proxy is hard. We did not want to do it again as these are solved problems. We therefore
created MORP on top of the excellent Java Spring Boot stack combining the following technologies with a thin layer of
MORP "glue".

* **Spring Cloud Gateway** - for flexible, declarative routing and a high performance, reactive HTTP proxy
* **Spring OAuth2 Client** - for a mature integration with a wide range of identity providers
* **Spring Security** - for flexible request authentication and authorization
* **Spring Native** - for producing native Docker images with a small resource footprint and fast startup times

# Deployment

TODO

# Configuration

To configure MORP three terms are essential: provider, registration, and tenant. Configuration of all three is covered
in the next sections.

## Provider

A provider refers to an identity provider (IDP). Common IDPs include Google, Okta, and Keycloak. Providers are
configured in the `morp.oauth-client.provider` section. A minimal provider configuration for a single Keycloak realm
looks like this:

```yaml
morp:
  oauth2-client:
    provider:
      keycloak:
        issuer-uri: https://keycloak.example.com/auth/realms/example
```

The providers name (`keycloak`, in the example above) can be any identifier. Every provider can have the following
properties:

| Property                          | Example                         |
|-----------------------------------|---------------------------------|
| `authorization-uri`               | `https://example.com/authorize` |
| `token-uri`                       | `https://example.com/token`     |
| `jwk-set-uri`                     | `https://example.com/jwks.json` |
| `user-info-uri`                   | `https://example.com/userinfo`  |
| `user-info-authentication-method` | `header`                        |
| `userNameAttribute`               | `name`                          |

Alternatively, an `issuer-uri` can be specified to automatically get the identity providers
[configuration](https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfig).

Properties in the provider section can be templated, to include the tenant (see below). To use a Keycloak realm per
tenant, use the following configuration:

```yaml
morp:
  oauth2-client:
    provider:
      keycloak:
        issuer-uri: https://keycloak.example.com/auth/realms/{tenant}
```

## Registration

Providers cannot be used for authentication by default. A registration is required to specify additional configuration.
This allows multiple registrations per provider. Registrations are configured in the documentations
`morp.oauth-client.registration` section:

```yaml
morp:
  oauth2-client:
    registration:
      keycloak:
        client-id: my-client-id
        client-secret: my-client-secret
        scope:
          - openid
          - profile
          - email
```

The following properties are supported:

| Property                       | Example             |
|--------------------------------|---------------------|
| `client-id`                    | my-client-id        |
| `client-secret`                | my-client-secret    |
| `client-name`                  | My Client           |
| `provider`                     | keycloak            |
| `scope`                        | user                |               
| `redirect-uri-template`        | https://example.com |
| `client-authentication-method` | basic               |
| `authorization-grant-type`     | authorization_code  | 

Usually, only `client-id` and `client-secret` are required.

## Tenant

A tenant is the entity determining the registration to be used. This might be a customer, an application or a
department. To figure out the tenant for a request, MORP uses tenant extractors. MORP comes with the following tenant
extractors:

| Tenant Extractor | Example Configuration                                                                                            | Example Request                   | Example Tenant |
|------------------|------------------------------------------------------------------------------------------------------------------|-----------------------------------|----------------|
| `host-pattern`   | <pre>- host-pattern<br/>    pattern: ([a-z]+).example.com</pre>                                                  | foo.example.com                   | foo            |
| `host-pattern`   | <pre>- host-pattern<br/>    pattern: (dev&#124;prod)-([a-z]+).example.com<br/>    capture-group: 2</pre>         | dev-foo.example.com               | foo            |
| `host-mapping`   | <pre>- host-mapping<br/>    foo.example.com: foo<br/>    bar.example.com: bar</pre>                              | foo.example.com                   | foo            |
| `path-pattern`   | <pre>- path-pattern<br/>    pattern: example.com/tenant/([a-z]+)</pre>                                           | example.com/tenant/foo            | foo            |
| `path-pattern`   | <pre>- path-pattern<br/>    pattern: example.com/api/(v1&#124;v2)/tenant/([a-z]+)<br/>    capture-group: 2</pre> | foo.example.com/api/v1/tenant/foo | foo            |
| `header`         | <pre>- header<br/>    name: X-Tenant-ID</pre>                                                                    | -H 'X-Tenant-ID: foo' example.com | foo            |

Tenant extractors are configured in the `morp.tenant-extractors` section. Tenant extractors are applied in order. The
first non-empty match will be used. When no tenant match, the request will be denied.

# Development

The project comes with a `docker-compose` setup that runs Keycloak with a couple of test realms for an interactive
developer experience. This setup is also used for running automated integration test.
Bring up the setup either via CLI or via `gradle`.

```shell
# via CLI
docker-compose up -d 

# via gradle
./gradlew composeUp
```

Once this is up you will have a Keycloak running. You can access the UI via port `9000`. Test credentials for the
admin user are `admin/admin`.

```shell
open http://localhost:9000/auth
```

To shut down the `docker-compose` backend run the following:

```shell
# via CLI
docker-compose down

# via gradle
./gradlew composeDown
```

## Demo Profile

This project contains a `demo` Spring profile that allows authentication using the Google and Okta cloud identity
providers.
The `demo` profile can be activated by setting the VM option `-Dspring.profiles.active=demo`.

The credentials for the OAuth2 clients can be supplied by putting an `application-demo.properties` file next to
the `application-demo.yaml` file in the folder `src/main/resources`.
Its contents should have the following format:

```properties
# Google
morp.oauth2-client.registration.google.client-id=...
morp.oauth2-client.registration.google.client-secret=...
# Okta
morp.oauth2-client.registration.okta.client-id=...
morp.oauth2-client.registration.okta.client-secret=...
```

# Metrics

By default, MORP exposes [Prometheus](https://prometheus.io) metrics at
[http://localhost:8081/actuator/prometheus](http://localhost:8081/actuator/prometheus).

# TODO

* Consider Spring profiles for a better `local` developer experience
* Move to Keycloak X distribution for improved startup time
* Documentation of how to set gateway request and connection timeouts (per route)
