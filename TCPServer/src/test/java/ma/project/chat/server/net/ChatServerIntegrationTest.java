package ma.project.chat.server.net;

import ma.project.chat.server.config.ServerConfig;
import ma.project.chat.server.model.ConnectedUser;
import ma.project.chat.server.model.ServerEventListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatServerIntegrationTest {
    private ChatServer server;
    private final List<TestClient> clients = new CopyOnWriteArrayList<>();
    private final RecordingListener listener = new RecordingListener();

    @AfterEach
    void tearDown() {
        clients.forEach(TestClient::close);
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void broadcastsTimestampedMessagesToConnectedClients() throws Exception {
        startServer();
        TestClient alice = connect("Alice", false);
        TestClient bob = connect("Bob", false);
        alice.drainQuietly();
        bob.drainQuietly();

        alice.send("MSG|Hello Bob");

        String received = bob.readUntil(line -> line.contains("Alice: Hello Bob"));
        assertTrue(received.matches("\\[\\d{2}:\\d{2}:\\d{2}] Alice: Hello Bob"));
        assertTrue(listener.logs.stream().anyMatch(line -> line.contains("Alice: Hello Bob")));
    }

    @Test
    void activeUserInquiryRespondsOnlyToRequester() throws Exception {
        startServer();
        TestClient alice = connect("Alice", false);
        TestClient bob = connect("Bob", false);
        alice.drainQuietly();
        bob.drainQuietly();

        alice.send("MSG|allUsers");

        String response = alice.readUntil(line -> line.contains("Active users"));
        assertTrue(response.contains("Alice"));
        assertTrue(response.contains("Bob"));
        assertFalse(bob.canReadWithin(Duration.ofMillis(350)));
    }

    @Test
    void disconnectCommandRemovesUserAndNotifiesOthers() throws Exception {
        startServer();
        TestClient alice = connect("Alice", false);
        TestClient bob = connect("Bob", false);
        alice.drainQuietly();
        bob.drainQuietly();

        alice.send("MSG|bye");

        String notice = bob.readUntil(line -> line.contains("Alice disconnected"));
        assertTrue(notice.contains("Alice disconnected"));
        assertTrue(waitUntil(() -> server.connectedUsers().stream()
                .map(ConnectedUser::displayName)
                .noneMatch("Alice"::equals)));
    }

    @Test
    void readOnlyClientsCannotBroadcastMessages() throws Exception {
        startServer();
        TestClient observer = connect("Observer", false);
        TestClient readOnly = connect("", true);
        observer.drainQuietly();
        readOnly.drainQuietly();

        readOnly.send("MSG|This should not broadcast");

        String warning = readOnly.readUntil(line -> line.contains("Read-only mode"));
        assertTrue(warning.contains("Read-only mode"));
        assertFalse(observer.canReadWithin(Duration.ofMillis(350)));
    }

    @Test
    void readOnlyClientsCanStillRequestActiveUsers() throws Exception {
        startServer();
        TestClient alice = connect("Alice", false);
        TestClient readOnly = connect("", true);
        alice.drainQuietly();
        readOnly.drainQuietly();

        readOnly.send("MSG|allUsers");

        String response = readOnly.readUntil(line -> line.contains("Active users"));
        assertTrue(response.contains("Alice"));
        assertTrue(response.contains("Guest-1 (Read Only)"));
        assertFalse(alice.canReadWithin(Duration.ofMillis(350)));
    }

    @Test
    void duplicateUsernamesReceiveUniqueDisplayNames() throws Exception {
        startServer();
        TestClient firstAlice = connect("Alice", false);
        TestClient secondAlice = connect("Alice", false);
        firstAlice.drainQuietly();
        secondAlice.drainQuietly();

        firstAlice.send("MSG|allUsers");

        String response = firstAlice.readUntil(line -> line.contains("Active users"));
        assertTrue(response.contains("Alice"));
        assertTrue(response.contains("Alice-2"));
    }

    private void startServer() throws Exception {
        server = new ChatServer(new ServerConfig("127.0.0.1", 0, "test"), listener);
        server.start();
    }

    private TestClient connect(String username, boolean readOnly) throws Exception {
        TestClient client = new TestClient(server.getPort());
        clients.add(client);
        client.send("JOIN|" + username + "|" + readOnly);
        client.readUntil(line -> line.contains("joined the chat"));
        return client;
    }

    private boolean waitUntil(CheckedBooleanSupplier supplier) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(2).toNanos();
        while (System.nanoTime() < deadline) {
            if (supplier.getAsBoolean()) {
                return true;
            }
            Thread.sleep(25);
        }
        return false;
    }

    @FunctionalInterface
    private interface CheckedBooleanSupplier {
        boolean getAsBoolean() throws Exception;
    }

    private static final class RecordingListener implements ServerEventListener {
        private final List<String> logs = new CopyOnWriteArrayList<>();

        @Override
        public void onLog(String line) {
            logs.add(line);
        }

        @Override
        public void onUsersChanged(List<ConnectedUser> users) {
        }
    }

    private static final class TestClient implements AutoCloseable {
        private final Socket socket;
        private final BufferedReader reader;
        private final PrintWriter writer;

        private TestClient(int port) throws IOException {
            socket = new Socket("127.0.0.1", port);
            socket.setSoTimeout(900);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        }

        private void send(String line) {
            writer.println(line);
            writer.flush();
        }

        private String readUntil(Predicate<String> predicate) throws IOException {
            long deadline = System.nanoTime() + Duration.ofSeconds(2).toNanos();
            while (System.nanoTime() < deadline) {
                String line = reader.readLine();
                if (line != null && predicate.test(line)) {
                    return line;
                }
            }
            throw new SocketTimeoutException("Expected line was not received.");
        }

        private boolean canReadWithin(Duration duration) throws IOException {
            int previousTimeout = socket.getSoTimeout();
            socket.setSoTimeout((int) duration.toMillis());
            try {
                return reader.readLine() != null;
            } catch (SocketTimeoutException exception) {
                return false;
            } finally {
                socket.setSoTimeout(previousTimeout);
            }
        }

        private void drainQuietly() {
            try {
                while (canReadWithin(Duration.ofMillis(75))) {
                    // Drain queued join/system lines.
                }
            } catch (IOException ignored) {
            }
        }

        @Override
        public void close() {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
