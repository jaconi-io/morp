package io.jaconi.morp.oauth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientRegistrationSourceTest {

	@Test
	void sha256Empty() {
		assertEquals(
				"072c7788bbaa6d3667b1654ce921c4dbfe18249aa3f6cf24f3db8b0640e34a1e",
				new ClientRegistrationSource().sha256()
		);
	}
}
