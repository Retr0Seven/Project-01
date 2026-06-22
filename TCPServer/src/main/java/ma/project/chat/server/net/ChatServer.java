package ma.project.chat.server.net;

import ma.project.chat.server.config.ServerConfig;
import ma.project.chat.server.model.ConnectedUser;
import ma.project.chat.server.model.ServerEventListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ChatServer implements AutoCloseable {
    private static final String[] USER_COLORS = {
            "#dcfce7", "#dbeafe", "#fef9c3", "#fce7f3", "#ede9fe",
            "#ccfbf1", "#fee2e2", "#e0f2fe", "#f5f5f4", "#fae8ff"
    };

    private final ServerConfig config;
    private final ServerEventListener listener;
    private final MessageFormatter formatter;
    private final Map<String, ClientSession> sessions = new ConcurrentHashMap<>();
    private final AtomicInteger guestSequence = new AtomicInteger(1);
    private final AtomicInteger duplicateSequence = new AtomicInteger(2);
    private final AtomicInteger colorSequence = new AtomicInteger();
    private final Object lifecycleLock = new Object();

    private volatile boolean running;
    private ServerSocket serverSocket;
    private ExecutorService acceptExecutor;
    private ExecutorService clientExecutor;

    public ChatServer(ServerConfig config, ServerEventListener listener) {
        this(config, listener, new MessageFormatter());
    }

    ChatServer(ServerConfig config, ServerEventListener listener, MessageFormatter formatter) {
        this.config = Objects.requireNonNull(config, "config");
        this.listener = listener == null ? ServerEventListener.noop() : listener;
        this.formatter = Objects.requireNonNull(formatter, "formatter");
    }

    public void start() throws IOException {
        synchronized (lifecycleLock) {
            if (running) {
                return;
            }

            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(config.host(), config.port()));

            acceptExecutor = Executors.newSingleThreadExecutor(namedThreadFactory("tcp-chat-accept"));
            clientExecutor = Executors.newCachedThreadPool(namedThreadFactory("tcp-chat-client"));
            running = true;

            listener.onLog("Server Started on " + config.host() + ":" + getPort());
            listener.onLog("Configuration loaded from " + config.source());
            acceptExecutor.submit(this::acceptLoop);
        }
    }

    public int getPort() {
        ServerSocket socket = serverSocket;
        return socket == null ? config.port() : socket.getLocalPort();
    }

    public boolean isRunning() {
        return running;
    }

    public List<ConnectedUser> connectedUsers() {
        return sessions.values().stream()
                .map(ClientSession::connectedUser)
                .sorted(Comparator.comparing(ConnectedUser::displayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public void stop() {
        synchronized (lifecycleLock) {
            if (!running) {
                return;
            }
            running = false;
        }

        closeServerSocket();
        List<ClientSession> openSessions = new ArrayList<>(sessions.values());
        openSessions.forEach(ClientSession::close);
        shutdownExecutor(acceptExecutor);
        shutdownExecutor(clientExecutor);
        sessions.clear();
        listener.onUsersChanged(List.of());
        listener.onLog("Server Stopped");
    }

    @Override
    public void close() {
        stop();
    }

    void register(ClientSession session) {
        sessions.put(session.id(), session);
        listener.onLog("Welcome " + session.displayName());
        listener.onUsersChanged(connectedUsers());
        broadcast(formatter.server(session.displayName() + " joined the chat"));
    }

    void unregister(ClientSession session) {
        ClientSession removed = sessions.remove(session.id());
        if (removed != null) {
            listener.onLog(session.displayName() + " disconnected");
            listener.onUsersChanged(connectedUsers());
            if (running) {
                broadcast(formatter.server(session.displayName() + " disconnected"));
            }
        }
    }

    void handleClientLine(ClientSession session, String line) {
        String message = Protocol.parseClientMessage(line);
        if (message.isBlank()) {
            return;
        }

        if (Protocol.isDisconnectCommand(message)) {
            session.send(formatter.server("Goodbye " + session.displayName()));
            session.close();
            return;
        }

        if (Protocol.isActiveUsersCommand(message)) {
            session.send(formatter.activeUsers(activeDisplayNames()));
            return;
        }

        if (session.readOnly()) {
            listener.onLog("Blocked read-only message from " + session.displayName());
            session.send(formatter.server("Read-only mode: you cannot send messages."));
            return;
        }

        String formatted = formatter.chat(session.displayName(), message);
        listener.onLog(formatted);
        broadcast(formatted);
    }

    synchronized String reserveDisplayName(String requestedUsername, boolean readOnly) {
        String base = requestedUsername == null || requestedUsername.isBlank()
                ? "Guest-" + guestSequence.getAndIncrement()
                : sanitizeDisplayName(requestedUsername);

        if (readOnly && (requestedUsername == null || requestedUsername.isBlank())) {
            base = base + " (Read Only)";
        }

        Set<String> existing = sessions.values().stream()
                .map(ClientSession::displayName)
                .collect(java.util.stream.Collectors.toSet());
        if (!existing.contains(base)) {
            return base;
        }

        String candidate;
        do {
            candidate = base + "-" + duplicateSequence.getAndIncrement();
        } while (existing.contains(candidate));
        return candidate;
    }

    String nextColor() {
        int index = Math.floorMod(colorSequence.getAndIncrement(), USER_COLORS.length);
        return USER_COLORS[index];
    }

    private void acceptLoop() {
        while (running) {
            try {
                listener.onLog("Waiting for Client");
                Socket socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
                clientExecutor.submit(new ClientSession(UUID.randomUUID().toString(), socket, this));
            } catch (SocketException exception) {
                if (running) {
                    listener.onLog("Server socket error: " + exception.getMessage());
                }
                break;
            } catch (IOException exception) {
                if (running) {
                    listener.onLog("Could not accept client: " + exception.getMessage());
                }
            }
        }
    }

    private void broadcast(String line) {
        sessions.values().forEach(session -> session.send(line));
    }

    private List<String> activeDisplayNames() {
        return sessions.values().stream()
                .map(ClientSession::displayName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private String sanitizeDisplayName(String requestedUsername) {
        String clean = requestedUsername.replace('|', ' ')
                .replace('\r', ' ')
                .replace('\n', ' ')
                .trim();
        return clean.isBlank() ? "Guest-" + guestSequence.getAndIncrement() : clean;
    }

    private void closeServerSocket() {
        ServerSocket socket = serverSocket;
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private void shutdownExecutor(ExecutorService executorService) {
        if (executorService == null) {
            return;
        }
        executorService.shutdownNow();
        try {
            if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                listener.onLog("Executor did not stop within timeout.");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private ThreadFactory namedThreadFactory(String prefix) {
        AtomicInteger sequence = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable, prefix + "-" + sequence.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
    }
}
