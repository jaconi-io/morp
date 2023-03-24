![MORP](mkdocs/docs/assets/morp.svg)

[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/jaconi-io/morp/continuous.yaml?branch=main&style=for-the-badge)](https://github.com/jaconi-io/morp/actions/workflows/continuous.yaml)
[![Codecov](https://img.shields.io/codecov/c/github/jaconi-io/morp?style=for-the-badge)](https://codecov.io/gh/jaconi-io/morp)
[![GitHub tag (latest SemVer)](https://img.shields.io/github/v/tag/jaconi-io/morp?label=Image&style=for-the-badge)](https://github.com/jaconi-io/morp/pkgs/container/morp)

[![GitHub license](https://img.shields.io/github/license/jaconi-io/morp?style=for-the-badge)](https://github.com/jaconi-io/morp/blob/main/LICENSE.md)
[![Conventional Commits](https://img.shields.io/badge/Conventional%20Commits-1.0.0-%23FE5196?logo=conventionalcommits&logoColor=white&style=for-the-badge)](https://conventionalcommits.org)
[![semantic-release: angular](https://img.shields.io/badge/semantic--release-angular-e10079?logo=semantic-release&style=for-the-badge)](https://github.com/semantic-release/semantic-release)

# THIS REPO IS IN PRE-RELEASE, DO NOT USE IN PRODUCTION

MORP is a multi-tenant OpenID Connect reverse proxy. With MORP you can protect web services and applications that do not
support authentication on their own. MORP takes care of the OpenID Connect "dance" using common identity providers
(e.g. Google, GitHub, Okta, and Keycloak).

# Documentation

Documentation can be found at [https://jaconi-io.github.io/morp](https://jaconi-io.github.io/morp).

# Tech Stack

We strongly believe in open source to build great things on top established components and frameworks. Implementing
OIDC and a high performance proxy is hard. We did not want to do it again as these are solved problems. We therefore
created MORP on top of the excellent Java Spring Boot stack combining the following technologies with a thin layer of
MORP "glue".

* **Spring Cloud Gateway** - for flexible, declarative routing and a high performance, reactive HTTP proxy
* **Spring OAuth2 Client** - for a mature integration with a wide range of identity providers
* **Spring Security** - for flexible request authentication and authorization
* **Spring Native** - for producing native Docker images with a small resource footprint and fast startup times

# Development

## Startup

The project comes with a `docker compose` setup that runs Keycloak with a couple of test realms for an interactive
developer experience. You can optionally run Morp itself as part of the compose setup.

Bring up the setup via CLI:

```shell
# via CLI
cd compose

# if you want to run Morp locally in your IDE
docker compose -f docker-compose.yaml up -d

# if you want to run Morp as part of compose
docker compose up -d
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

## Obtain Secrets
For Google and Okta as well as integration tests we need additional credentials that can be put into a (git-ignored)
`secret.properties` file in the project root directory:

```properties
# Google
morp.oauth2-client.registration.google.client-id=...
morp.oauth2-client.registration.google.client-secret=...

# Okta
morp.oauth2-client.registration.okta.client-id=...
morp.oauth2-client.registration.okta.client-secret=...

test.okta.password=...
```

This `secret.properties` file can also be created using the 1Password CLI: https://developer.1password.com/docs/cli.
After installing the CLI on your machine (https://developer.1password.com/docs/cli/get-started#install) and signing in (https://developer.1password.com/docs/cli/get-started#sign-in), the `secret.properties` file can be created using the follwing command:
```shell
op inject -i secret.properties.tpl -o secret.properties
```

## Shutdown

To shut down the `docker compose` backend run the following:

```shell
# via CLI
docker compose down

# via gradle
./gradlew composeDown
```
