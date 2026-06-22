# TCP Group Chat — Mini Project 1

A real-time group chat application built with Java, TCP sockets, and JavaFX. Multiple clients connect to a central server and exchange messages in a shared chat. This implements the Mini Project 1 requirements (Server–Client architecture, authentication, read-only mode, and Model–View separation).

## Table of Contents

- Overview
- Features
- Architecture
- Technology Stack
- Requirements
- Build & Run
- Configuration
- Usage & Commands
- Multiple Clients
- Testing the Chat
- Project Structure
- Technical Architecture (UML)

## Overview

The application has two parts:

- Server (TCPServer): Listens for client connections, receives messages from any client, and broadcasts them to all other connected clients. Shows a live list of connected users and an activity log in a JavaFX window.
- Client (TCPClient): Connects to the server with a username, sends and receives messages in real time, and supports special commands (allUsers, bye/end). The client UI has a login screen, chat area, and message input with a SEND button.

Communication is TCP only. The server's host/port are loaded from server.properties at runtime; the client receives the server's host/port as command-line arguments.

## Features

Client

- Authentication: User enters a username before joining the chat, or leaves it empty for read-only mode.
- Read-only mode: If the username is blank, the user can only read messages — the input field and SEND button stay disabled.
- Real-time messaging: Type in the text field and send with the SEND button or Enter.
- Active users: Type allUsers to receive the list of all currently connected clients.
- Disconnect: Type bye or end to disconnect; the connection closes and the server is notified.
- UI: "Online" status label with a colored circle indicator; sent messages appear in your own chat area too.

Server

- Multiple connections: Accepts many clients at once (one thread per client connection).
- Message distribution: Each incoming message is stamped with the sender's username and timestamp, then broadcast to all other connected clients.
- Client list: A live ListView of connected usernames in the server UI.
- Visual distinction: Each user in the list gets a randomly assigned background color.
- Activity log: Entries such as "Server Started," "Waiting for Client," and "Welcome [User]."

General

- Model–View separation: Networking/business logic (ChatServer, ClientSession, ClientConnection) is fully independent of the JavaFX UI (ServerApplication, ClientApplication) — they communicate only through listener interfaces.
- Config-driven server: Server host and port are read from server.properties (no recompilation needed for environment changes).

## Architecture

- Server–Client (TCP): One central server, multiple clients. The server listens on a configurable host/port; clients connect and exchange messages over TCP sockets.
- Server: Thread-per-connection — each ClientSession runs on its own thread; messages are broadcast to every other connected session.
- Client: One background thread reads from the server socket; UI updates are marshalled back onto the JavaFX Application Thread.
- Model–View:
  - Server: ChatServer / ClientSession (sockets, broadcast, session registry) and ServerApplication (JavaFX), connected via the ServerEventListener interface.
  - Client: ClientConnection (socket I/O) and ClientApplication (JavaFX), connected via the ClientConnectionListener interface.

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| Network | Java Sockets (TCP) |
| GUI | JavaFX (GridPane layout, CSS) |
| Build | Maven (two independent modules) |
| Config | .properties (server host/port) |

## Requirements

- JDK 17 (JavaFX is not bundled in JDK 11+, so the JavaFX dependency is pulled in via Maven).
- Maven 3.6+ is optional — the repo includes the Maven Wrapper (./mvnw / mvnw.cmd).

## Build & Run

From the project root:

./mvnw -f TCPServer/pom.xml clean package
./mvnw -f TCPClient/pom.xml clean package

On Windows PowerShell:

.\mvnw.cmd -f TCPServer\pom.xml clean package
.\mvnw.cmd -f TCPClient\pom.xml clean package

This produces the executable jars at:

- TCPServer/target/TCPServer.jar (with its dependencies copied to TCPServer/target/lib/)
- TCPClient/target/TCPClient.jar (with its dependencies copied to TCPClient/target/lib/)

Run server

java -jar TCPServer/target/TCPServer.jar

Run client

java -jar TCPClient/target/TCPClient.jar localhost 3000

Windows shortcut

A launcher script is included for quickly spinning up a server plus several clients:

.\run-chat.bat -Clients 2 -ReadOnlyClient

Useful options:

.\run-chat.bat            (default: start server + 2 clients)
.\run-chat.bat -Build     (rebuild both jars first)
.\run-chat.bat -Clients 3
.\run-chat.bat -Clients 2 -ReadOnlyClient

## Configuration

Server network settings live in:

TCPServer/server.properties

server.host=0.0.0.0
server.port=3000

Change server.port (and optionally server.host) as needed, then rebuild so the updated file is copied into target/. The client must be started with the matching host and port, e.g. java -jar TCPClient.jar localhost 3000.

## Usage & Commands

| Action | What to do |
|---|---|
| Send a message | Type in the message box → SEND or Enter |
| List all users | Type allUsers and send |
| Disconnect | Type bye or end and send |

Sent messages appear in your own chat area and are broadcast to every other connected client, with the sender's username and timestamp prepended by the server.

## Multiple Clients

You can have several clients in the same chat; each one sees messages from all the others.

1. Start the server once: java -jar TCPServer/target/TCPServer.jar
2. Start the first client: java -jar TCPClient/target/TCPClient.jar localhost 3000. Enter a username (e.g. Alice) and join.
3. Start a second client the same way in another terminal. Enter a different username (e.g. Bob) and join.
4. A message sent from Alice's window appears in both Alice's and Bob's chat area, and vice versa.

Every message (aside from commands like allUsers) is broadcast by the server to every other connected client, so all participants share the same conversation. Repeat step 3 for any additional clients.

## Testing the Chat

1. Start the server — a "Group Chat — Server" window appears with status and activity log.
2. Start a client — a "Group Chat — Client" window appears.
3. Enter a username and join. You should see the chat screen with an "Online" status and the message input enabled.
4. Send a message: type in the box and press SEND or Enter. It appears in your own chat area, and in any other connected client's window.
5. Start a second client. Both clients see join/leave activity, and the server's user list shows both, each with a different color.
6. Type allUsers in any client and send — the chat area displays the current list of active users.
7. Type bye or end in one client to disconnect — the server's user list and the other client update accordingly.
8. Read-only check: start a client, leave the username blank, and join. The message input and SEND button stay disabled — you can only read.

## Project Structure

Project-01/
├── mvnw / mvnw.cmd                # Maven wrapper scripts
├── run-chat.bat / run-chat.ps1    # Launcher: build + start server/clients
├── TCPServer/
│   ├── pom.xml
│   ├── server.properties          # server.host, server.port
│   └── src/main/java/
│       ├── TCPServer.java                          # main()
│       └── ma/project/chat/server/
│           ├── ServerApplication.java              # JavaFX UI, ServerEventListener
│           ├── config/ServerConfig.java            # config record + loader
│           ├── model/ConnectedUser.java            # record (name, color, readOnly)
│           ├── model/ServerEventListener.java      # callback interface
│           └── net/
│               ├── ChatServer.java                 # accepts/broadcasts/registry
│               ├── ClientSession.java               # per-connection thread
│               ├── MessageFormatter.java
│               └── Protocol.java                    # wire format, command constants
├── TCPClient/
│   ├── pom.xml
│   └── src/main/java/
│       ├── TCPClient.java                          # main()
│       └── ma/project/chat/client/
│           ├── ClientApplication.java               # JavaFX UI, ClientConnectionListener
│           ├── model/ClientConnectionListener.java  # callback interface
│           └── net/
│               ├── ClientConnection.java             # socket I/O
│               ├── ClientOptions.java                # CLI args record
│               └── Protocol.java
└── docs/
    ├── class-diagram.puml / deployment-diagram.puml
    └── diagrams/                                    # rendered PNG/SVG versions

## Technical Architecture (UML)

### Class diagram

<img width="3979" height="2865" alt="class-diagram" src="https://github.com/user-attachments/assets/2b735d65-03a2-42fe-a362-b13e8020041b" />


### Deployment diagram
<img width="1425" height="1708" alt="de![Uploading class-diagram.png…]()loyment-diagram" src="https://github.com/user-attachments/assets/d7754fb8-f8a8-4896-acf2-88ea48d3a3a4" />



Diagram sources are in docs/ for anyone who wants to regenerate or edit them.
