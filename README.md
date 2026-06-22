# Mini Project 1: TCP Group Chat

Java TCP Socket + JavaFX implementation of the Project 1 group chat requirements.

## Structure

- `TCPServer/` - JavaFX server application and TCP socket model.
- `TCPClient/` - JavaFX client application and TCP socket model.
- `docs/` - UML PlantUML sources, demo checklist, test report, and submission checklist.
- `project_1_requirements.md` - original assignment requirements.

## Build

This project includes a Maven Wrapper, so a global Maven installation is not required.

From `Paradigms_Class/Project-01`:

```bash
./mvnw -f TCPServer/pom.xml clean package
./mvnw -f TCPClient/pom.xml clean package
```

On Windows PowerShell:

```powershell
.\mvnw.cmd -f TCPServer\pom.xml clean package
.\mvnw.cmd -f TCPClient\pom.xml clean package
```

The executable jars are created at:

- `TCPServer/target/TCPServer.jar`
- `TCPClient/target/TCPClient.jar`

Each jar uses the dependency jars copied into its adjacent `target/lib/` folder.

## Run

The easiest way on Windows is:

```powershell
.\run-chat.bat -Clients 2 -ReadOnlyClient
```

Useful launcher options:

```powershell
.\run-chat.bat
.\run-chat.bat -Build
.\run-chat.bat -Clients 3
.\run-chat.bat -Clients 2 -ReadOnlyClient
```

Start the server first:

```bash
java -jar TCPServer/target/TCPServer.jar
```

Then start one or more clients:

```bash
java -jar TCPClient/target/TCPClient.jar localhost 3000
```

The client also supports the assignment-style launcher class when run from compiled classes:

```bash
java TCPClient localhost 3000
```

The server reads runtime configuration from `TCPServer/server.properties`.

```properties
server.host=0.0.0.0
server.port=3000
```

## Chat Commands

- `allUsers` - asks the server for the current active-user list. The response is sent only to the requester.
- `end` or `bye` - disconnects the client and updates the server user list.

If the username field is left empty, the client joins in read-only mode. Read-only users can still run `allUsers` and `bye`, but chat messages are blocked.

## Architecture Notes

- The server uses a thread-per-client model with `ServerSocket`, `Socket`, and an executor service.
- Socket handling and business logic live in model/service classes under `ma.project.chat.server.net` and `ma.project.chat.client.net`.
- JavaFX classes render the interface and call model APIs; background socket callbacks update the UI through `Platform.runLater`.
- Client-server messages use a small line protocol: `JOIN|username|readOnly` and `MSG|text`.

## Troubleshooting

- If the server exits immediately, port `3000` may already be in use. Change `server.port` in `TCPServer/server.properties`.
- If `java` is not recognized, install a JDK and make sure it is on PATH.
- If Maven is not installed, use the included `mvnw.cmd`; it downloads Maven automatically.
- If a client cannot connect, start the server first and confirm the client uses the same host and port.
- If JavaFX dependencies are missing, rebuild with `.\run-chat.bat -Build`.

## Demo Checklist

Use `docs/demo-checklist.md` as the 3-minute demo guide.

## Submission Checklist

Use `docs/submission-checklist.md` before uploading the project.

## Team Contribution

Use `docs/team-handoff.md` to divide remaining real work. Do not rewrite authorship for work someone did not do; the teammate should make their own commits or be listed as a co-author only on commits they actually helped with.
