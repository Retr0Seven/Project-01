package ma.project.chat.server.net;

import ma.project.chat.server.model.ConnectedUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

final class ClientSession implements Runnable {
    private final String id;
    private final Socket socket;
    private final ChatServer server;
    private final AtomicBoolean open = new AtomicBoolean(true);

    private volatile PrintWriter writer;
    private volatile String displayName = "Unknown";
    private volatile String color = "#f5f5f4";
    private volatile boolean readOnly;
    private volatile boolean registered;

    ClientSession(String id, Socket socket, ChatServer server) {
        this.id = id;
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (socket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            writer = output;
            Protocol.ClientHello hello = Protocol.parseJoin(reader.readLine());
            readOnly = hello.readOnly() || hello.username().isBlank();
            displayName = server.reserveDisplayName(hello.username(), readOnly);
            color = server.nextColor();
            registered = true;
            server.register(this);

            String line;
            while (open.get() && (line = reader.readLine()) != null) {
                server.handleClientLine(this, line);
            }
        } catch (IllegalArgumentException exception) {
            send("Server: Invalid client hello. " + exception.getMessage());
        } catch (IOException exception) {
            if (open.get()) {
                send("Server: Connection error. " + exception.getMessage());
            }
        } finally {
            open.set(false);
            if (registered) {
                server.unregister(this);
            }
        }
    }

    String id() {
        return id;
    }

    String displayName() {
        return displayName;
    }

    boolean readOnly() {
        return readOnly;
    }

    ConnectedUser connectedUser() {
        return new ConnectedUser(displayName, color, readOnly);
    }

    void send(String line) {
        PrintWriter output = writer;
        if (output == null || !open.get()) {
            return;
        }
        synchronized (output) {
            output.println(line);
            output.flush();
        }
    }

    void close() {
        if (!open.compareAndSet(true, false)) {
            return;
        }
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
