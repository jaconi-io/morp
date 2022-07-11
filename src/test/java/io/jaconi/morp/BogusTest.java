package io.jaconi.morp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BogusTest {

    @Test
    void failingTest() {
        assertTrue(false);
    }

    @Test
    void passingTest() {
        assertTrue(true);
    }
}
