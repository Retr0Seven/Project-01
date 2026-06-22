package ma.project.chat.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public record ServerConfig(String host, int port, String source) {
    public static final String DEFAULT_HOST = "0.0.0.0";
    public static final int DEFAULT_PORT = 3000;

    public ServerConfig {
        host = normalizeHost(host);
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Server port must be between 0 and 65535.");
        }
        source = Objects.requireNonNullElse(source, "defaults");
    }

    public static ServerConfig defaults() {
        return new ServerConfig(DEFAULT_HOST, DEFAULT_PORT, "defaults");
    }

    public static ServerConfig loadDefault() {
        String configuredPath = System.getProperty("server.config");
        if (configuredPath != null && !configuredPath.isBlank()) {
            return loadUnchecked(Path.of(configuredPath.trim()));
        }

        for (Path candidate : defaultCandidates()) {
            if (Files.isRegularFile(candidate)) {
                return loadUnchecked(candidate);
            }
        }

        try (InputStream stream = ServerConfig.class.getResourceAsStream("/server.properties")) {
            if (stream != null) {
                Properties properties = new Properties();
                properties.load(stream);
                return fromProperties(properties, "classpath:/server.properties");
            }
        } catch (IOException ignored) {
            return defaults();
        }

        return defaults();
    }

    public static ServerConfig load(Path path) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
        }
        return fromProperties(properties, path.toAbsolutePath().normalize().toString());
    }

    public static ServerConfig fromProperties(Properties properties, String source) {
        String host = properties.getProperty("server.host", DEFAULT_HOST);
        String portText = properties.getProperty("server.port", Integer.toString(DEFAULT_PORT)).trim();
        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid server.port value: " + portText, exception);
        }
        return new ServerConfig(host, port, source);
    }

    private static ServerConfig loadUnchecked(Path path) {
        try {
            return load(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load server configuration from " + path, exception);
        }
    }

    private static List<Path> defaultCandidates() {
        List<Path> candidates = new ArrayList<>();
        candidates.add(Path.of("server.properties"));
        candidates.add(Path.of("TCPServer", "server.properties"));

        Path applicationDirectory = applicationDirectory();
        if (applicationDirectory != null) {
            candidates.add(applicationDirectory.resolve("server.properties"));
            Path parent = applicationDirectory.getParent();
            if (parent != null) {
                candidates.add(parent.resolve("server.properties"));
            }
        }

        return candidates;
    }

    private static Path applicationDirectory() {
        CodeSource codeSource = ServerConfig.class.getProtectionDomain().getCodeSource();
        if (codeSource == null || codeSource.getLocation() == null) {
            return null;
        }

        try {
            URI uri = codeSource.getLocation().toURI();
            Path path = Path.of(uri);
            if (Files.isRegularFile(path)) {
                return path.getParent();
            }
            return path;
        } catch (URISyntaxException | IllegalArgumentException exception) {
            return null;
        }
    }

    private static String normalizeHost(String host) {
        if (host == null || host.isBlank()) {
            return DEFAULT_HOST;
        }
        return host.trim();
    }
}
