# CommandBlocker

Block commands.

### Permissions

* **commandblocker.allow.\<command\>** - Explicitly allow a user to use a filtered command
* **commandblocker.block.\<command\>** - Explicitly prevent a user from using a non-filtered command

### Config
```
# Commands to block by default, unless the user has the "commandblocker.allow.<name>" permission
filtered = [
  "gamemode"
  "op"
  "deop"
]

block-message="&cNo Permission."
```
