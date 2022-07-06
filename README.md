![MORP](morp.svg)

MORP is a multi-tenant OpenID Connect reverse proxy. With MORP you can protect web services that do not support OpenID
Connect using multiple identity providers (for example, Google, GitHub, Okta, and Keycloak).

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

* Describe `application.properties` required to run proxy locally

# TODO

* Consider Spring profiles for a better `local` developer experience
* Move to Keycloak X distribution for improved startup time
* Documentation of how to set gateway request and connection timeouts (per route)
