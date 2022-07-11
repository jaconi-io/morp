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

To shutdown the `docker-compose` backend run the following:

```shell
# via CLI
docker-compose down

# via gradle
./gradlew composeDown
```

## Demo Profile

This project contains a `demo` Spring profile that allows authentication using the Google and Okta cloud identity providers.
The `demo` profile can be activated by setting the VM option `-Dspring.profiles.active=demo`.

The credentials for the OAuth2 clients can be supplied by putting an `application-demo.properties` file next to the `application-demo.yaml` file in the folder `src/main/resources`.
Its contents should have the following format:
```properties
# Google
morp.oauth2-client.registration.google.client-id=...
morp.oauth2-client.registration.google.client-secret=...

# Okta
morp.oauth2-client.registration.okta.client-id=...
morp.oauth2-client.registration.okta.client-secret=...
```

# TODO

* Consider Spring profiles for a better `local` developer experience
* Move to Keycloak X distribution for improved startup time
* Documentation of how to set gateway request and connection timeouts (per route)
