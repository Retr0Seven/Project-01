package ma.project.chat.client.model;

public interface ClientConnectionListener {
    void onMessage(String line);

    void onDisconnected(String reason);
}
