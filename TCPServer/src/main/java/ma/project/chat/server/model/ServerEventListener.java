package ma.project.chat.server.model;

import java.util.List;

public interface ServerEventListener {
    void onLog(String line);

    void onUsersChanged(List<ConnectedUser> users);

    static ServerEventListener noop() {
        return new ServerEventListener() {
            @Override
            public void onLog(String line) {
            }

            @Override
            public void onUsersChanged(List<ConnectedUser> users) {
            }
        };
    }
}
