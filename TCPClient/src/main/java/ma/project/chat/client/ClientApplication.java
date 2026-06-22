package ma.project.chat.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import ma.project.chat.client.model.ClientConnectionListener;
import ma.project.chat.client.net.ClientConnection;
import ma.project.chat.client.net.ClientOptions;
import ma.project.chat.client.net.Protocol;

public final class ClientApplication extends Application implements ClientConnectionListener {
    private ClientOptions options;
    private ClientConnection connection;
    private Stage stage;

    private TextArea chatArea;
    private TextField messageField;
    private Button sendButton;
    private Button disconnectButton;
    private Label statusLabel;
    private Circle statusCircle;
    private boolean readOnly;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        options = ClientOptions.fromArguments(getParameters().getRaw());

        Scene scene = new Scene(createIdentityRoot(), 520, 300);
        scene.getStylesheets().add(getClass().getResource("/styles/client.css").toExternalForm());

        stage.setTitle("TCP Group Chat Client");
        stage.setScene(scene);
        stage.setMinWidth(500);
        stage.setMinHeight(300);
        stage.setOnCloseRequest(event -> disconnectCleanly());
        stage.show();
    }

    @Override
    public void stop() {
        disconnectCleanly();
    }

    @Override
    public void onMessage(String line) {
        Platform.runLater(() -> appendChat(line));
    }

    @Override
    public void onDisconnected(String reason) {
        Platform.runLater(() -> {
            appendChat(reason);
            updateOnline(false);
            if (messageField != null) {
                messageField.setDisable(true);
            }
            if (sendButton != null) {
                sendButton.setDisable(true);
            }
            if (disconnectButton != null) {
                disconnectButton.setDisable(true);
            }
        });
    }

    private GridPane createIdentityRoot() {
        GridPane root = new GridPane();
        root.getStyleClass().add("client-root");
        root.setPadding(new Insets(24));
        root.setHgap(12);
        root.setVgap(14);

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(100);
        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setHgrow(Priority.ALWAYS);
        root.getColumnConstraints().addAll(labelColumn, fieldColumn);

        Label title = new Label("TCP Group Chat");
        title.getStyleClass().add("title");
        root.add(title, 0, 0, 2, 1);

        Label serverLabel = new Label("Server");
        serverLabel.getStyleClass().add("field-label");
        Label serverValue = new Label(options.host() + ":" + options.port());
        serverValue.getStyleClass().add("server-value");
        root.add(serverLabel, 0, 1);
        root.add(serverValue, 1, 1);

        Label usernameLabel = new Label("Username");
        usernameLabel.getStyleClass().add("field-label");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Leave blank for read-only");
        root.add(usernameLabel, 0, 2);
        root.add(usernameField, 1, 2);

        Label feedback = new Label();
        feedback.getStyleClass().add("feedback");
        root.add(feedback, 1, 3);

        Button connectButton = new Button("Connect");
        connectButton.setDefaultButton(true);
        connectButton.setOnAction(event -> connect(usernameField.getText(), feedback));
        root.add(connectButton, 1, 4);

        return root;
    }

    private GridPane createChatRoot(String username) {
        GridPane root = new GridPane();
        root.getStyleClass().add("client-root");
        root.setPadding(new Insets(18));
        root.setHgap(10);
        root.setVgap(12);

        ColumnConstraints messageColumn = new ColumnConstraints();
        messageColumn.setHgrow(Priority.ALWAYS);
        ColumnConstraints buttonColumn = new ColumnConstraints();
        buttonColumn.setMinWidth(100);
        root.getColumnConstraints().addAll(messageColumn, buttonColumn);

        Label title = new Label(readOnly ? "Read-Only Chat" : "Group Chat");
        title.getStyleClass().add("title");
        root.add(title, 0, 0);

        HBox statusRow = new HBox(8);
        statusRow.setAlignment(Pos.CENTER_RIGHT);
        statusCircle = new Circle(7, Color.web("#22c55e"));
        statusLabel = new Label("Online");
        statusLabel.getStyleClass().add("status-label");
        statusRow.getChildren().addAll(statusCircle, statusLabel);
        root.add(statusRow, 1, 0);

        Label identity = new Label((readOnly ? "Guest" : username) + " | " + options.host() + ":" + options.port());
        identity.getStyleClass().add("identity-label");
        root.add(identity, 0, 1, 2, 1);

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.getStyleClass().add("chat-area");
        GridPane.setHgrow(chatArea, Priority.ALWAYS);
        GridPane.setVgrow(chatArea, Priority.ALWAYS);
        root.add(chatArea, 0, 2, 2, 1);

        messageField = new TextField();
        messageField.setPromptText(readOnly ? "Commands only" : "Type a message");
        messageField.setOnAction(event -> sendCurrentMessage());
        root.add(messageField, 0, 3);

        sendButton = new Button("Send");
        sendButton.setMaxWidth(Double.MAX_VALUE);
        sendButton.setOnAction(event -> sendCurrentMessage());
        root.add(sendButton, 1, 3);

        disconnectButton = new Button("Disconnect");
        disconnectButton.getStyleClass().add("secondary-button");
        disconnectButton.setMaxWidth(Double.MAX_VALUE);
        disconnectButton.setOnAction(event -> {
            if (connection != null) {
                connection.sendDisconnectCommand();
            }
        });
        root.add(disconnectButton, 1, 4);

        return root;
    }

    private void connect(String usernameText, Label feedback) {
        String username = usernameText == null ? "" : usernameText.trim();
        readOnly = username.isBlank();

        try {
            connection = new ClientConnection(options.host(), options.port(), this);
            connection.connect(username, readOnly);

            Scene scene = new Scene(createChatRoot(username), 720, 520);
            scene.getStylesheets().add(getClass().getResource("/styles/client.css").toExternalForm());
            stage.setScene(scene);
            stage.setMinWidth(620);
            stage.setMinHeight(460);
            appendChat("Connected to " + options.host() + ":" + options.port());
            if (readOnly) {
                appendChat("Read-only mode is active.");
            }
        } catch (Exception exception) {
            feedback.setText("Connection failed: " + exception.getMessage());
            if (connection != null) {
                connection.close();
            }
        }
    }

    private void sendCurrentMessage() {
        if (connection == null) {
            return;
        }

        String message = messageField.getText().trim();
        if (message.isBlank()) {
            return;
        }

        if (readOnly && !Protocol.isActiveUsersCommand(message) && !Protocol.isDisconnectCommand(message)) {
            appendChat("Read-only mode: you cannot send messages.");
            messageField.clear();
            return;
        }

        connection.sendMessage(message);
        messageField.clear();
        if (Protocol.isDisconnectCommand(message)) {
            messageField.setDisable(true);
            sendButton.setDisable(true);
            disconnectButton.setDisable(true);
        }
    }

    private void disconnectCleanly() {
        if (connection != null && connection.isConnected()) {
            connection.sendDisconnectCommand();
            connection.close();
        }
    }

    private void updateOnline(boolean online) {
        if (statusLabel == null || statusCircle == null) {
            return;
        }
        statusLabel.setText(online ? "Online" : "Offline");
        statusCircle.setFill(Color.web(online ? "#22c55e" : "#ef4444"));
    }

    private void appendChat(String line) {
        if (chatArea == null) {
            return;
        }
        chatArea.appendText(line + System.lineSeparator());
        chatArea.setScrollTop(Double.MAX_VALUE);
    }
}
