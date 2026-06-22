package ma.project.chat.client.net;

public final class Protocol {
    public static final String JOIN = "JOIN";
    public static final String MESSAGE = "MSG";
    public static final String ALL_USERS = "allUsers";
    public static final String END = "end";
    public static final String BYE = "bye";

    private Protocol() {
    }

    public static String joinLine(String username, boolean readOnly) {
        return JOIN + "|" + clean(username) + "|" + readOnly;
    }

    public static String messageLine(String message) {
        return MESSAGE + "|" + clean(message);
    }

    public static boolean isDisconnectCommand(String message) {
        String clean = clean(message);
        return END.equalsIgnoreCase(clean) || BYE.equalsIgnoreCase(clean);
    }

    public static boolean isActiveUsersCommand(String message) {
        return ALL_USERS.equalsIgnoreCase(clean(message));
    }

    private static String clean(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('|', ' ')
                .replace('\r', ' ')
                .replace('\n', ' ')
                .trim();
    }
}
