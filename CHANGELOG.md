# [2.0.0](https://github.com/jaconi-io/morp/compare/v1.3.1...v2.0.0) (2025-01-02)


### Bug Fixes

* remove Spring Security deprecations ([87f36dc](https://github.com/jaconi-io/morp/commit/87f36dc32b54e1edd4cdbf4cc995deb9fe0437d0))


### Build System

* update to Spring Boot 3.2 ([2aa807a](https://github.com/jaconi-io/morp/commit/2aa807a0f1d879603c3bbbcd202a4b330bddec2a))


### BREAKING CHANGES

* Host mappings do not support ports anymore.

```yaml
- id: invalid-host-mapping
   uri: https://example.com
   predicates:
     - Host={tenant}.localtest.me:8080

- id: valid-host-mapping
   uri: https://example.com
   predicates:
     - Host={tenant}.localtest.me
```

## [1.3.1](https://github.com/jaconi-io/morp/compare/v1.3.0...v1.3.1) (2023-04-18)


### Bug Fixes

* mockserver version ([64a0f80](https://github.com/jaconi-io/morp/commit/64a0f80661e1190adf992b811383851282f36198))

# [1.3.0](https://github.com/jaconi-io/morp/compare/v1.2.3...v1.3.0) (2023-03-24)


### Bug Fixes

* make Sonar happy ([d487443](https://github.com/jaconi-io/morp/commit/d487443a421d35650f98a206ee14a393265279d9))
* remove all milestones ([0620829](https://github.com/jaconi-io/morp/commit/0620829ee5d186fd54301792a0ebd04e33c58bdf))
* remove plain OAuth2 test since Keycloak 20 no longer supports this ([3d4c4f2](https://github.com/jaconi-io/morp/commit/3d4c4f20504ed7584dbd679cc0bbbe4e30460b1d))
* version updates ([83f54ae](https://github.com/jaconi-io/morp/commit/83f54ae6cc22d50f173203554c2e735c02656d14))
* workarounds for native image ([4a2a38e](https://github.com/jaconi-io/morp/commit/4a2a38ec62aa9fa023c49d308d2bd7af6076d582))


### Features

* multi-arch images ([20af4bb](https://github.com/jaconi-io/morp/commit/20af4bbfd2b53de1b74cae2aa39b8530ee3f296d))
* upgrade Spring Boot 3.0.0 ([ef09508](https://github.com/jaconi-io/morp/commit/ef09508387a16d0b18e1061ec0083ab49245f8dd))

## [1.2.3](https://github.com/jaconi-io/morp/compare/v1.2.2...v1.2.3) (2022-11-23)


### Bug Fixes

* minor code smells ([590ab5f](https://github.com/jaconi-io/morp/commit/590ab5fb83fe8cff908b297398fe271657e79aa8))

## [1.2.2](https://github.com/jaconi-io/morp/compare/v1.2.1...v1.2.2) (2022-11-07)


### Bug Fixes

* code smells ([ae22084](https://github.com/jaconi-io/morp/commit/ae22084e99e20f831f2a736a8ff6a0e25927f11c))
* potential NPE ([b684c2e](https://github.com/jaconi-io/morp/commit/b684c2e5d0532540323523bdac32fb23877cc6d5))

## [1.2.1](https://github.com/jaconi-io/morp/compare/v1.2.0...v1.2.1) (2022-11-03)


### Bug Fixes

* disable CSRF ([f549d24](https://github.com/jaconi-io/morp/commit/f549d242df9562e28629eb86ea0d4f8dc42e5b5b))

# [1.2.0](https://github.com/jaconi-io/morp/compare/v1.1.0...v1.2.0) (2022-10-13)


### Bug Fixes

* build native image only on x86_64 architecture ([e6a555d](https://github.com/jaconi-io/morp/commit/e6a555dfc5ddbed6060a301af96a7cdf1a6d4e37))
* fix bar client secret ([8d1c087](https://github.com/jaconi-io/morp/commit/8d1c0875078be6b8d1ef46859ea69a42b24d9f74))
* shorten file names for selenium video recording ([61227ec](https://github.com/jaconi-io/morp/commit/61227ec9f86c540f81cc7ca842f1f1b7d5414436))
* support non-OIDC clients ([53b3390](https://github.com/jaconi-io/morp/commit/53b3390a9844a1833a25b87f3f5829e30648676e))


### Features

* actuator endpoint for oauth client registry ([9652e69](https://github.com/jaconi-io/morp/commit/9652e6944f86b1d1aa383149dc13d781ac7789fa))
* add tenant as optional metric dimension ([761f1be](https://github.com/jaconi-io/morp/commit/761f1be78436da524fff1d2391c1223ad20bb9b0))

# [1.1.0](https://github.com/jaconi-io/morp/compare/v1.0.0...v1.1.0) (2022-08-25)


### Bug Fixes

* remove session cookie from backend request ([f4c24df](https://github.com/jaconi-io/morp/commit/f4c24df497c8e64e942638dc4cf196fe2d0aa405))


### Features

* json logging by profile ([3f2fb85](https://github.com/jaconi-io/morp/commit/3f2fb85a8338d0495f0ac517eaca0f28a17e8a66))

# [1.0.0](https://github.com/jaconi-io/morp/compare/v0.3.0...v1.0.0) (2022-08-19)


* feat!: use URI template variables for tenant discovery ([651e6a9](https://github.com/jaconi-io/morp/commit/651e6a950fbad61d5fef5ec67bc04c716b8f2a36))


### BREAKING CHANGES

* Tenant extraction config changed heavily!

# [0.3.0](https://github.com/jaconi-io/morp/compare/v0.2.3...v0.3.0) (2022-08-19)


### Bug Fixes

* add sensible defaults ([8f5e261](https://github.com/jaconi-io/morp/commit/8f5e2618935b65405d6f9a20880597a9006600b7))
* fix native image ([ed2bc0f](https://github.com/jaconi-io/morp/commit/ed2bc0f0a2509ae4007264f8ee7f23793109c705))
* improve test ([6d9197d](https://github.com/jaconi-io/morp/commit/6d9197d98d93170c6d720f5354ff49f611a3402c))
* make sonar happy ([9e4f33b](https://github.com/jaconi-io/morp/commit/9e4f33b397b9fa94599ad05cacc9e6a54000f272))
* migrate to sha256 ([fc5c85e](https://github.com/jaconi-io/morp/commit/fc5c85ea44241b8c70963d5e1c47301d542a3778))
* remove remaining code smells ([d5b84ff](https://github.com/jaconi-io/morp/commit/d5b84ff7aa944e477fee8d0359a40a54c41ae478))
* use only AssertJ ([8d17958](https://github.com/jaconi-io/morp/commit/8d17958a47a551d6c233ae959991247d2eee370b))


### Features

* add caching for client registration ([99c130b](https://github.com/jaconi-io/morp/commit/99c130b8be8d0ecf657e9475b9429d938bc11d18))
* add dev profile ([cf68883](https://github.com/jaconi-io/morp/commit/cf68883c21aba993d1e2f16d53ef77b7a8744bee))

## [0.2.3](https://github.com/jaconi-io/morp/compare/v0.2.2...v0.2.3) (2022-07-20)


### Bug Fixes

* user name attribute templating ([d28f15f](https://github.com/jaconi-io/morp/commit/d28f15f3406ca507ee01d69602dc67a5179f77d6))

## [0.2.2](https://github.com/jaconi-io/morp/compare/v0.2.1...v0.2.2) (2022-07-20)


### Bug Fixes

* add more generic Spring Cloud Gateway support ([e7b77d1](https://github.com/jaconi-io/morp/commit/e7b77d11706d771c9fec4e378e33f291a4437120))
* add more native type hints ([323e19c](https://github.com/jaconi-io/morp/commit/323e19c19d87d3a3234d4ac6a7facee9fb1cf55e))

## [0.2.1](https://github.com/jaconi-io/morp/compare/v0.2.0...v0.2.1) (2022-07-15)


### Bug Fixes

* **gh-actions:** finalize Release automation ([578bb76](https://github.com/jaconi-io/morp/commit/578bb76e52137d11392c5dd794ba46066705e20c))
