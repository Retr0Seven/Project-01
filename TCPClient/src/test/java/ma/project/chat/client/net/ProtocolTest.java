package ma.project.chat.client.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProtocolTest {
    @Test
    void buildsJoinLine() {
        assertEquals("JOIN|Alice|false", Protocol.joinLine("Alice", false));
    }

    @Test
    void sanitizesMessageLine() {
        assertEquals("MSG|Hello there", Protocol.messageLine("Hello|there\n"));
    }

    @Test
    void recognizesDisconnectCommands() {
        assertTrue(Protocol.isDisconnectCommand("bye"));
        assertTrue(Protocol.isDisconnectCommand("END"));
        assertFalse(Protocol.isDisconnectCommand("hello"));
    }

    @Test
    void recognizesActiveUsersCommand() {
        assertTrue(Protocol.isActiveUsersCommand("allUsers"));
        assertTrue(Protocol.isActiveUsersCommand("ALLUSERS"));
        assertFalse(Protocol.isActiveUsersCommand("users"));
    }
}
