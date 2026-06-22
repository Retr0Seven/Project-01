package ma.project.chat.server.net;

public final class Protocol {
    public static final String JOIN = "JOIN";
    public static final String MESSAGE = "MSG";
    public static final String ALL_USERS = "allUsers";
    public static final String END = "end";
    public static final String BYE = "bye";

    private Protocol() {
    }

    public static ClientHello parseJoin(String line) {
        if (line == null) {
            throw new IllegalArgumentException("Missing JOIN line.");
        }

        String[] parts = line.split("\\|", 3);
        if (parts.length != 3 || !JOIN.equals(parts[0])) {
            throw new IllegalArgumentException("Expected JOIN|username|readOnly.");
        }

        return new ClientHello(clean(parts[1]), Boolean.parseBoolean(parts[2]));
    }

    public static String parseClientMessage(String line) {
        if (line == null) {
            return "";
        }
        if (line.startsWith(MESSAGE + "|")) {
            return clean(line.substring((MESSAGE + "|").length()));
        }
        return clean(line);
    }

    public static boolean isActiveUsersCommand(String message) {
        return ALL_USERS.equalsIgnoreCase(clean(message));
    }

    public static boolean isDisconnectCommand(String message) {
        String clean = clean(message);
        return END.equalsIgnoreCase(clean) || BYE.equalsIgnoreCase(clean);
    }

    private static String clean(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\r', ' ').replace('\n', ' ').trim();
    }

    public record ClientHello(String username, boolean readOnly) {
    }
}
