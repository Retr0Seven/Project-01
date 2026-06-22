# Submission Checklist

Use this list before submitting Project 1.

## Source Code

- [ ] `TCPServer` Maven project is present.
- [ ] `TCPClient` Maven project is present.
- [ ] Server and client source code are committed.
- [ ] `README.md` explains build, run, commands, and troubleshooting.
- [ ] Runtime config file exists at `TCPServer/server.properties`.

## Build Artifacts

- [ ] Run `.\mvnw.cmd -f TCPServer\pom.xml clean package`.
- [ ] Run `.\mvnw.cmd -f TCPClient\pom.xml clean package`.
- [ ] Confirm `TCPServer/target/TCPServer.jar` exists.
- [ ] Confirm `TCPClient/target/TCPClient.jar` exists.
- [ ] Keep each `target/lib/` folder with its matching JAR when submitting executable artifacts.

## Feature Verification

- [ ] Multiple clients can connect at the same time.
- [ ] Named users can send messages.
- [ ] Send button works.
- [ ] Enter key works.
- [ ] Empty username creates read-only mode.
- [ ] Read-only mode blocks chat messages.
- [ ] `allUsers` returns active users only to the requesting client.
- [ ] `bye` and `end` disconnect the client.
- [ ] Server shows connected users in the `ListView`.
- [ ] Server assigns visible user colors.
- [ ] Server logs startup, waiting, welcome, messages, and disconnects.

## Diagrams And Demo

- [ ] Class diagram is available from `docs/class-diagram.puml`.
- [ ] Deployment diagram is available from `docs/deployment-diagram.puml`.
- [ ] Optional: render `.puml` files to PNG/PDF using PlantUML.
- [ ] Record a 3-minute demo using `docs/demo-checklist.md`.
- [ ] Add final GitHub repository link to the submission form.
