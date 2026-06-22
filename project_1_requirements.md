# Mini Project 1: Group Chat Application

**Instructor:** El Habib Nfaoui  
**Project Type:** Java TCP Socket + JavaFX Application  
**Goal:** Build a real-time group chat application using a central server and multiple clients.

---

## 1. Project Overview

This mini-project focuses on implementing a **group chat application** using **Java Sockets** with **TCP** and **JavaFX**.

The application must allow multiple clients to connect to a central server and communicate in a shared chat environment in real time.

---

## 2. System Architecture

The application must follow a **Server-Client architecture**.

### 2.1 Central Server

The server acts as a message distributor.

It must:

- Accept multiple client connections.
- Receive messages from one client.
- Format received messages.
- Send the messages to the other connected clients.
- Maintain a live list of connected users.

### 2.2 Multiple Clients

Each client is an individual application.

Each client must:

- Connect to the server using an IP address and port number.
- Send messages to the group chat.
- Receive messages from other connected clients.

---

## 3. Functional Requirements

## 3.1 Client Features

### 3.1.1 Authentication and Identity

Users must enter a username before accessing the full chat interface.

### 3.1.2 Read-Only Mode

If a user connects without providing a username, the client application must restrict the user to **READ-ONLY MODE**.

In read-only mode, the user must not be able to send messages.

### 3.1.3 Real-Time Messaging

Users must be able to send messages to the group chat by:

- Typing a message into a text area or input field.
- Clicking a **SEND** button.
- Pressing the **Enter** key.

### 3.1.4 Active User Inquiry

The client must support the command:

```text
allUsers
```

When a user enters this command, the server must return a list of all currently active clients to that specific user only.

### 3.1.5 Disconnection

A user must be able to disconnect from the server by typing:

```text
end
```

or

```text
bye
```

After this command, the application must:

- Close the client connection.
- Notify the server that the user has disconnected.

### 3.1.6 UI Status Indicators

The client UI must display:

- An **Online** status label.
- A visual status indicator, such as a circle.

---

## 3.2 Server Features

### 3.2.1 Connection Management

The server must accept multiple simultaneous client connections.

### 3.2.2 Message Distribution

When the server receives a message from a client, it must:

- Format the message with the sender's username.
- Add the message time.
- Distribute the formatted message to active connections.

### 3.2.3 Client Monitoring

The server UI must maintain a live `ListView` of all connected usernames.

### 3.2.4 Visual Distinction

The server must assign random background colors to different users in the client list to improve readability.

### 3.2.5 Logging

The server must display activity logs, including messages such as:

- `Server Started`
- `Waiting for Client`
- `Welcome [User]`

---

## 4. Technical Specifications

## 4.1 Technology Stack

The project must use:

- **Language:** Java
- **Network Communication:** Java Sockets using TCP
- **GUI Framework:** JavaFX
- **Layout:** GridPane
- **Styling:** CSS
- **IDE:** IntelliJ IDEA

---

## 4.2 Server-Side Model

The server must handle simultaneous client connections using one of the following approaches:

### Option 1: Thread-per-connection

Each client connection is handled by a separate thread.

### Option 2: I/O Multiplexing

A single thread and selector are used to handle multiple socket channels.

> Note: The thread-per-connection approach is easier to implement but does not scale well for very large numbers of clients.

---

## 4.3 Network Configuration Parameters

The server IP address and port number must be loaded from a standard configuration file at runtime.

Accepted configuration file formats include:

- `.properties`
- `.xml`

This allows the server IP address and port number to be changed without recompiling the code.

---

## 4.4 Model-View Decoupling

The project must follow **Separation of Concerns**.

The application logic must be separated from the JavaFX interface.

### Model Responsibilities

The model must handle:

- Data processing.
- Socket communication.
- Business logic.

### View Responsibilities

The view must handle:

- JavaFX interface rendering.
- UI layout.
- User interaction display.

The model must function independently of the view.

Changes to the presentation layer must not require changes to the underlying application logic.

---

## 5. Operational Requirements

## 5.1 Command-Line Arguments

The application must be started with specific parameters.

### 5.1.1 Client Startup Command

```bash
java TCPClient <ServerIPAddress> <PortNumber>
```

Example:

```bash
java TCPClient localhost 3000
```

### 5.1.2 Server Startup Command

```bash
java TCPServer
```

---

## 6. Deliverables

To complete the project, the following deliverables must be submitted.

### 6.1 Fully Functional Maven Projects

Submit the source code for:

- `TCPServer`
- `TCPClient`

Both must be fully functional Maven projects.

### 6.2 Executable JAR Files

Submit executable JAR files for both applications:

- Server executable JAR
- Client executable JAR

### 6.3 Demo Video

Submit a **3-minute demo video** showing:

- A walkthrough of the source code.
- A live demonstration of the application features.

### 6.4 Technical Architecture UML

Submit UML diagrams explaining the architecture.

Required diagrams:

- **Class Diagram**: shows the software structure and relationships between classes.
- **Deployment Diagram**: shows the physical network nodes, such as server and clients, and the TCP/IP communication links.

Optional diagrams:

- Use Case Diagram
- Sequence Diagram

### 6.5 GitHub Link

Submit a shared GitHub repository link containing:

- Full source code.
- Commit history.
- Descriptive `README.md`.

### 6.6 Optional Technical Post

Optionally submit a link to a technical post about the development process.

Possible platforms:

- Medium
- Dev.to
- Personal forum or blog

The post may describe:

- Development process.
- Technical challenges.
- Final implementation.

---

## 7. Requirement Checklist

| Category | Requirement | Required |
|---|---|---|
| Architecture | Server-client architecture | Yes |
| Architecture | Central server distributes messages | Yes |
| Architecture | Multiple clients connect to server | Yes |
| Client | Username before full chat access | Yes |
| Client | Read-only mode for empty username | Yes |
| Client | Send messages with button | Yes |
| Client | Send messages with Enter key | Yes |
| Client | `allUsers` command | Yes |
| Client | `end` / `bye` disconnection | Yes |
| Client | Online status label | Yes |
| Client | Visual online indicator | Yes |
| Server | Accept multiple clients | Yes |
| Server | Format message with username | Yes |
| Server | Format message with time | Yes |
| Server | Distribute messages to active clients | Yes |
| Server | Live connected-user ListView | Yes |
| Server | Random background colors for users | Yes |
| Server | Activity logging | Yes |
| Technical | Java | Yes |
| Technical | TCP sockets | Yes |
| Technical | JavaFX | Yes |
| Technical | GridPane layout | Yes |
| Technical | CSS styling | Yes |
| Technical | Runtime config file | Yes |
| Technical | Model-view separation | Yes |
| Operational | Client command-line arguments | Yes |
| Operational | Server startup command | Yes |
| Deliverable | Maven project for server | Yes |
| Deliverable | Maven project for client | Yes |
| Deliverable | Executable server JAR | Yes |
| Deliverable | Executable client JAR | Yes |
| Deliverable | 3-minute demo video | Yes |
| Deliverable | UML class diagram | Yes |
| Deliverable | UML deployment diagram | Yes |
| Deliverable | GitHub repository link | Yes |
| Deliverable | Technical post | Optional |
