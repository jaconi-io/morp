MORP is a multi-tenant OpenID Connect reverse proxy. With MORP you can protect web services and applications that do not
support authentication on their own. MORP takes care of the OpenID Connect "dance" using common identity providers
like Google, GitHub, Okta, and Keycloak.

## Motivation 

There are several excellent OAuth2 and OpenID Connect proxies out there today that can be used to protect you backend
apps. However, existing implementations typically only support a single tenant (with a single identity provider). MORP
aims at making **multi-tenancy** a first class citizen so that you can run apps on behalf of different tenants that may
be using different identity providers to authenticate users.

## Key Features

* Flexible tenant identification based host name, request paths or headers
* Fully templatized IDP configuration
* Dynamic discovery of IDP secrets
* **Planned** Persistence of IDP state for high availability

## Demo

This project contains an example showing how to protect a httpbin instance using authentication with Keycloak. Run the
example by switching to the example folder and using `docker compose`:

```shell
docker compose -f compose/docker-compose.yaml -f compose/docker-compose.demo.yaml up
```

Open [http://foo.localtest.me:8080](http://foo.localtest.me:8080) in your browser and log in with the username
`test.user@jaconi.io` and the password `user`. To use a different tenant, open
[http://bar.localtest.me:8080](http://bar.localtest.me:8080) in your browser and log in with username
`test.user@example.com` and the password `user`.

If you are having troubles accessing [http://localtest.me](http://localtest.me), see
[DNS Rebind Protection](user-guide/dns-rebind-protection.md).
