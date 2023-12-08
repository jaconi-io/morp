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
This allows multiple registrations per provider.
Registrations are configured in the `morp.oauth-client.registration` section:

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

A tenant is the entity determining the registration to be used. This might be a customer, an application, or a
department. To figure out the tenant for a request, MORP uses predicates. See
[Tenant Extraction](user-guide/routing.md#tenant-extraction) for details.
