package ma.project.chat.client.net;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientOptionsTest {
    @Test
    void defaultsWhenNoArgumentsAreSupplied() {
        ClientOptions options = ClientOptions.fromArguments(List.of());

        assertEquals("localhost", options.host());
        assertEquals(3000, options.port());
        assertTrue(options.usedDefaults());
    }

    @Test
    void parsesHostAndPortFromArguments() {
        ClientOptions options = ClientOptions.fromArguments(List.of("127.0.0.1", "4555"));

        assertEquals("127.0.0.1", options.host());
        assertEquals(4555, options.port());
        assertFalse(options.usedDefaults());
    }

    @Test
    void rejectsInvalidPort() {
        assertThrows(IllegalArgumentException.class, () -> ClientOptions.fromArguments(List.of("localhost", "bad")));
    }

    @Test
    void rejectsOutOfRangePort() {
        assertThrows(IllegalArgumentException.class, () -> ClientOptions.fromArguments(List.of("localhost", "70000")));
    }
}
