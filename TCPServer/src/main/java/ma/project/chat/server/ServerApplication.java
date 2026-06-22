package ma.project.chat.server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import ma.project.chat.server.config.ServerConfig;
import ma.project.chat.server.model.ConnectedUser;
import ma.project.chat.server.model.ServerEventListener;
import ma.project.chat.server.net.ChatServer;

import java.io.IOException;
import java.util.List;

public final class ServerApplication extends Application implements ServerEventListener {
    private final ObservableList<ConnectedUser> users = FXCollections.observableArrayList();

    private ChatServer chatServer;
    private TextArea logArea;
    private Label statusLabel;
    private Circle statusCircle;
    private Button startButton;
    private Button stopButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        ServerConfig config = ServerConfig.loadDefault();
        chatServer = new ChatServer(config, this);

        Scene scene = new Scene(createRoot(config), 820, 560);
        scene.getStylesheets().add(getClass().getResource("/styles/server.css").toExternalForm());

        stage.setTitle("TCP Group Chat Server");
        stage.setScene(scene);
        stage.setMinWidth(720);
        stage.setMinHeight(480);
        stage.setOnCloseRequest(event -> stopServer());
        stage.show();

        startServer();
    }

    @Override
    public void stop() {
        stopServer();
    }

    @Override
    public void onLog(String line) {
        Platform.runLater(() -> {
            logArea.appendText(line + System.lineSeparator());
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    @Override
    public void onUsersChanged(List<ConnectedUser> updatedUsers) {
        Platform.runLater(() -> users.setAll(updatedUsers));
    }

    private GridPane createRoot(ServerConfig config) {
        GridPane root = new GridPane();
        root.getStyleClass().add("server-root");
        root.setPadding(new Insets(22));
        root.setHgap(16);
        root.setVgap(14);

        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(35);
        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(65);
        root.getColumnConstraints().addAll(left, right);

        Label title = new Label("TCP Group Chat Server");
        title.getStyleClass().add("title");
        root.add(title, 0, 0, 2, 1);

        HBox statusRow = new HBox(8);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusCircle = new Circle(7, Color.web("#ef4444"));
        statusLabel = new Label("Offline");
        statusLabel.getStyleClass().add("status-label");
        Label configLabel = new Label("Host " + config.host() + " | Port " + config.port());
        configLabel.getStyleClass().add("config-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        startButton = new Button("Start");
        stopButton = new Button("Stop");
        startButton.setOnAction(event -> startServer());
        stopButton.setOnAction(event -> stopServer());
        statusRow.getChildren().addAll(statusCircle, statusLabel, configLabel, spacer, startButton, stopButton);
        root.add(statusRow, 0, 1, 2, 1);

        Label usersLabel = new Label("Connected Users");
        usersLabel.getStyleClass().add("section-label");
        root.add(usersLabel, 0, 2);

        Label logLabel = new Label("Activity Log");
        logLabel.getStyleClass().add("section-label");
        root.add(logLabel, 1, 2);

        ListView<ConnectedUser> userList = new ListView<>(users);
        userList.getStyleClass().add("user-list");
        userList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(ConnectedUser user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                String suffix = user.readOnly() ? "  READ ONLY" : "";
                setText(user.displayName() + suffix);
                setStyle("-fx-background-color: " + user.color() + ";");
            }
        });
        GridPane.setVgrow(userList, Priority.ALWAYS);
        root.add(userList, 0, 3);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.getStyleClass().add("log-area");
        GridPane.setVgrow(logArea, Priority.ALWAYS);
        GridPane.setHgrow(logArea, Priority.ALWAYS);
        root.add(logArea, 1, 3);

        return root;
    }

    private void startServer() {
        if (chatServer == null || chatServer.isRunning()) {
            updateStatus(true);
            return;
        }

        try {
            chatServer.start();
            updateStatus(true);
        } catch (IOException | RuntimeException exception) {
            onLog("Could not start server: " + exception.getMessage());
            updateStatus(false);
        }
    }

    private void stopServer() {
        if (chatServer != null) {
            chatServer.stop();
        }
        updateStatus(false);
    }

    private void updateStatus(boolean online) {
        if (statusLabel == null || statusCircle == null) {
            return;
        }
        Platform.runLater(() -> {
            statusLabel.setText(online ? "Online" : "Offline");
            statusCircle.setFill(Color.web(online ? "#22c55e" : "#ef4444"));
            startButton.setDisable(online);
            stopButton.setDisable(!online);
        });
    }
}
