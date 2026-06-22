package ma.project.chat.server.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServerConfigTest {
    @TempDir
    Path tempDir;

    @Test
    void defaultsUseAssignmentPortAndAllInterfaces() {
        ServerConfig config = ServerConfig.defaults();

        assertEquals("0.0.0.0", config.host());
        assertEquals(3000, config.port());
    }

    @Test
    void loadsHostAndPortFromPropertiesFile() throws Exception {
        Path configPath = tempDir.resolve("server.properties");
        Files.writeString(configPath, "server.host=127.0.0.1\nserver.port=4555\n");

        ServerConfig config = ServerConfig.load(configPath);

        assertEquals("127.0.0.1", config.host());
        assertEquals(4555, config.port());
    }

    @Test
    void rejectsInvalidPort() {
        Properties properties = new Properties();
        properties.setProperty("server.port", "not-a-number");

        assertThrows(IllegalArgumentException.class, () -> ServerConfig.fromProperties(properties, "test"));
    }

    @Test
    void rejectsOutOfRangePort() {
        Properties properties = new Properties();
        properties.setProperty("server.port", "70000");

        assertThrows(IllegalArgumentException.class, () -> ServerConfig.fromProperties(properties, "test"));
    }
}
