# Manual Test Report

Tester:

Date:

Java version:

Operating system:

## Setup

Command used:

```powershell
.\run-chat.bat -Clients 2 -ReadOnlyClient
```

## Results

| Scenario | Expected Result | Actual Result | Pass/Fail |
|---|---|---|---|
| Server starts | Server window opens and shows Online |  |  |
| First named client connects | User appears in server list |  |  |
| Second named client connects | Both users appear in server list |  |  |
| Message sent with Send button | Other clients receive timestamped message |  |  |
| Message sent with Enter key | Other clients receive timestamped message |  |  |
| `allUsers` command | Requesting client receives active-user list |  |  |
| Empty username client connects | Client enters read-only mode |  |  |
| Read-only client sends normal text | Message is blocked |  |  |
| Read-only client sends `allUsers` | Active-user list is returned |  |  |
| Client sends `bye` | Client disconnects and server list updates |  |  |
| Client sends `end` | Client disconnects and server list updates |  |  |

## Notes

Add screenshots or observations here.
