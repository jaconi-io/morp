package io.jaconi.morp;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test validating several Actuator endpoints against a running MORP container.
 * No authenticated session should be required to access those endpoints.
 */
public class ActuatorIT extends TestBase {

	@Test
	void testClientRegistrationEndpoints() {
		// step 1 - access the authorization endpoint for all tenants to make sure the cache contains all client registrations
		Set.of("tenant1", "tenant2").forEach(
				tenant ->
						containerSetup.getWebTestClient().get()
								.uri("/oauth2/authorization/" + tenant)
								.accept(MediaType.TEXT_HTML)
								.exchange()
								.expectStatus().is3xxRedirection()
								.expectHeader().exists("location")
								.expectBody().returnResult()
		);

		// step 2 - access the "actuator/clientregistrations" endpoint
		// expect a list of all tenant/client registrations
		var tenants = containerSetup.getManagementTestClient().get()
				.uri("/actuator/clientregistrations")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<List<String>>() {
				})
				.returnResult()
				.getResponseBody();

		assertThat(tenants).containsExactly("tenant1", "tenant2");

		// step 3 - access the "actuator/clientregistrations/tenant1" endpoint
		// expect the client registration for tenant1 to be returned
		containerSetup.getManagementTestClient().get()
				.uri("/actuator/clientregistrations/tenant1")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				// validate the returned client id with secret redaction
				.jsonPath("registrationId").isEqualTo("tenant1")
				.jsonPath("clientId").isEqualTo("morp")
				.jsonPath("clientSecret").isEqualTo("******");
	}

	@Test
	void testGatewayRefresh() {
		// step 1 - send a POST request to the "/actuator/gateway/refresh" endpoint
		// expect the call to be successful (no authentication or CSRF token required)
		containerSetup.getManagementTestClient().post()
				.uri("/actuator/gateway/refresh")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk();
	}
}
