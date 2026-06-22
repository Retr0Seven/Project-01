package ma.project.chat.client.net;

import ma.project.chat.client.model.ClientConnectionListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ClientConnection implements AutoCloseable {
    private final String host;
    private final int port;
    private final ClientConnectionListener listener;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    private Socket socket;
    private PrintWriter writer;
    private ExecutorService readerExecutor;

    public ClientConnection(String host, int port, ClientConnectionListener listener) {
        this.host = Objects.requireNonNull(host, "host");
        this.port = port;
        this.listener = Objects.requireNonNull(listener, "listener");
    }

    public void connect(String username, boolean readOnly) throws IOException {
        if (!connected.compareAndSet(false, true)) {
            return;
        }

        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 3_000);
        socket.setTcpNoDelay(true);

        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer.println(Protocol.joinLine(username, readOnly));

        readerExecutor = Executors.newSingleThreadExecutor(namedThreadFactory());
        readerExecutor.submit(() -> readLoop(reader));
    }

    public boolean isConnected() {
        return connected.get();
    }

    public void sendMessage(String message) {
        if (!connected.get() || writer == null || message == null || message.isBlank()) {
            return;
        }
        writer.println(Protocol.messageLine(message));
        writer.flush();
    }

    public void sendDisconnectCommand() {
        if (connected.get()) {
            sendMessage(Protocol.BYE);
        }
    }

    @Override
    public void close() {
        if (!connected.compareAndSet(true, false)) {
            return;
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
        if (readerExecutor != null) {
            readerExecutor.shutdownNow();
        }
    }

    private void readLoop(BufferedReader reader) {
        String reason = "Connection closed.";
        try (reader) {
            String line;
            while (connected.get() && (line = reader.readLine()) != null) {
                listener.onMessage(line);
            }
        } catch (IOException exception) {
            if (connected.get()) {
                reason = "Connection error: " + exception.getMessage();
            }
        } finally {
            close();
            listener.onDisconnected(reason);
        }
    }

    private ThreadFactory namedThreadFactory() {
        return runnable -> {
            Thread thread = new Thread(runnable, "tcp-chat-client-reader");
            thread.setDaemon(true);
            return thread;
        };
    }
}
