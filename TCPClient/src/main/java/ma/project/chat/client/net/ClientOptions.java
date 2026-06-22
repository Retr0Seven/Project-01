package ma.project.chat.client.net;

import java.util.List;

public record ClientOptions(String host, int port, boolean usedDefaults) {
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 3000;

    public static ClientOptions fromArguments(List<String> arguments) {
        if (arguments == null || arguments.size() < 2) {
            return new ClientOptions(DEFAULT_HOST, DEFAULT_PORT, true);
        }

        String host = arguments.get(0).isBlank() ? DEFAULT_HOST : arguments.get(0).trim();
        int port = parsePort(arguments.get(1));
        return new ClientOptions(host, port, false);
    }

    private static int parsePort(String value) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < 0 || parsed > 65535) {
                throw new IllegalArgumentException("Port must be between 0 and 65535.");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Port must be a number: " + value, exception);
        }
    }
}
