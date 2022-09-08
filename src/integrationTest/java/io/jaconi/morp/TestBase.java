package io.jaconi.morp;

import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Base class for test cases that rely on a TestContainers based environment running Keycloak, Morp and MockServer.
 * The base class ensures that the expensive container setup can be shared across test cases.
 */
public abstract class TestBase {

    @RegisterExtension
    static TestContainerSetup containerSetup = new TestContainerSetup();

}
