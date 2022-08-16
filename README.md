![MORP](morp.svg)

<p align="center">

[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/jaconi-io/morp/Continuous%20Integration?style=for-the-badge)](https://github.com/jaconi-io/morp/actions/workflows/continuous.yaml)
[![Codecov](https://img.shields.io/codecov/c/github/jaconi-io/morp?style=for-the-badge)](https://codecov.io/gh/jaconi-io/morp)
[![GitHub tag (latest SemVer)](https://img.shields.io/github/v/tag/jaconi-io/morp?label=Image&style=for-the-badge)](https://github.com/jaconi-io/morp/pkgs/container/morp)

[![GitHub license](https://img.shields.io/github/license/jaconi-io/morp?style=for-the-badge)](https://github.com/jaconi-io/morp/blob/main/LICENSE.md)
[![Conventional Commits](https://img.shields.io/badge/Conventional%20Commits-1.0.0-%23FE5196?logo=conventionalcommits&logoColor=white&style=for-the-badge)](https://conventionalcommits.org)
[![semantic-release: angular](https://img.shields.io/badge/semantic--release-angular-e10079?logo=semantic-release&style=for-the-badge)](https://github.com/semantic-release/semantic-release)

<p/>


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

# Demo

This project contains an example showing how to protect a httpbin instance using authentication with Keycloak. Run the
example by switching to the example folder and using `docker compose`:

```shell
cd compose
docker compose up -d
```

Open [http://foo.localtest.me:8080](http://foo.localtest.me:8080) in your browser and log in with the username
`test.user@jaconi.io` and the password `user`.

To use a different tenant, open [http://bar.localtest.me:8080](http://bar.localtest.me:8080) in your browser and
log in with username `test.user@example.com` and the password `user`.

## DNS Rebind Protection

If you are having troubles accessing [http://localtest.me](http://localtest.me), check if your router does DNS rebind
protection. There are various options to avoid this problem.

### Add Exception

You can add an exception for the following domains in your routers configuration:
```
bar.localtest.me
foo.localtest.me
keycloak.localtest.me
```

For instructions see your router vendors documentation. For example:
* [AVM FRITZ!Box](https://en.avm.de/service/knowledge-base/dok/FRITZ-Box-6660-Cable/3565_FRITZ-Box-reports-Your-FRITZ-Box-s-DNS-rebind-protection-rejected-your-query-for-reasons-of-security/)
* [Google Nest](https://support.google.com/googlenest/answer/9144137)

### Use external DNS

Use an external DNS service. For example:

[Cloudflare](https://1.1.1.1)
[Google](https://developers.google.com/speed/public-dns/docs/using)

### Add to `/etc/hosts`

Add this to your `/etc/hosts`:

```
127.0.0.1 bar.localtest.me
127.0.0.1 foo.localtest.me
127.0.0.1 keycloak.localtest.me
```

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
        issuer-uri: https://keycloak.example.com/realms/example
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
        issuer-uri: https://keycloak.example.com/realms/{tenant}
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

## Startup

The project comes with a `docker compose` setup that runs Keycloak with a couple of test realms for an interactive
developer experience. This setup is also used for running automated integration test.
Bring up the setup via CLI:

```shell
# via CLI
cd compose
docker compose -f docker-compose.yaml up -d
```

## Keycloak

Once this is up you will have a Keycloak running. You can access the UI via port `9000`. Test credentials for the
admin user are `admin/admin`.

```shell
open http://localhost:9000/admin/master/console
```

## Run MORP

You can then start MORP with a dedicated `dev` profile which allows logging in via the Keycloak as well as via Google or Okta:
```shell
./gradlew bootRun --args='--spring.profiles.active=dev'
```
You can also start MORP from your favorite IDE.

For Google and Okta we need additional credentials that can be put into an `application-dev.properties` file:
```properties
# Google
morp.oauth2-client.registration.google.client-id=...
morp.oauth2-client.registration.google.client-secret=...

# Okta
morp.oauth2-client.registration.okta.client-id=...
morp.oauth2-client.registration.okta.client-secret=...
```

## Shutdown

To shut down the `docker compose` backend run the following:

```shell
# via CLI
docker compose down

# via gradle
./gradlew composeDown
```

# Metrics

By default, MORP exposes [Prometheus](https://prometheus.io) metrics at
[http://localhost:8081/actuator/prometheus](http://localhost:8081/actuator/prometheus).

# TODO

* Consider Spring profiles for a better `local` developer experience
* Documentation of how to set gateway request and connection timeouts (per route)
