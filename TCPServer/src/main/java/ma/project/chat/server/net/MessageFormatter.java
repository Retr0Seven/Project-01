package ma.project.chat.server.net;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.stream.Collectors;

public final class MessageFormatter {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public String chat(String sender, String message) {
        return "[" + now() + "] " + sender + ": " + message;
    }

    public String server(String message) {
        return "[" + now() + "] Server: " + message;
    }

    public String activeUsers(Collection<String> users) {
        String names = users.isEmpty()
                ? "No active users"
                : users.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.joining(", "));
        return server("Active users: " + names);
    }

    private String now() {
        return LocalTime.now().format(TIME_FORMAT);
    }
}
