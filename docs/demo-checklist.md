# 3-Minute Demo Checklist

## Suggested Timing

1. 0:00-0:25 - Show the two Maven projects: `TCPServer` and `TCPClient`.
2. 0:25-0:45 - Point out model/view separation: socket services, protocol helpers, and JavaFX classes.
3. 0:45-1:05 - Build or launch with `.\run-chat.bat -Clients 2 -ReadOnlyClient`.
4. 1:05-1:25 - Show the server status, log area, and connected-user list.
5. 1:25-1:45 - Connect two named clients and one empty-name read-only client.
6. 1:45-2:05 - Send a message using the Send button.
7. 2:05-2:20 - Send another message with the Enter key.
8. 2:20-2:35 - Type `allUsers` and show that only that client receives the active-user list.
9. 2:35-2:50 - Try sending from the read-only client and show the warning.
10. 2:50-3:00 - Type `bye` or `end` and show the client disconnect and server list update.

## Short Script

This project is a Java TCP socket group chat with a central JavaFX server and multiple JavaFX clients. The server accepts concurrent socket connections, keeps a live user list, formats messages with usernames and timestamps, and broadcasts chat messages to active clients. The client asks for a username before entering chat; if the username is empty, it enters read-only mode. The commands `allUsers`, `bye`, and `end` demonstrate server-side command handling and connection cleanup.
