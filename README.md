# kCheatCheck

**kCheatCheck** is a professional Minecraft plugin designed for PvP servers to facilitate the process of checking players suspected of cheating. It provides staff members with essential tools to isolate, communicate with, and log checks for suspected players.

## Features

- **Isolate Players:** Teleport players to specific "Check Areas" and restrict their movement and actions.
- **Private Communication:** A dedicated chat system for staff and the player being checked.
- **Persistent Checks:** Players remain in check even if they log out and back in.
- **Multiple Check Areas:** Support for multiple rooms/locations for simultaneous checks.
- **GUI System:** Visual interface to browse through the entire check history or specific player records.
- **Multi-Language Support:** Fully customizable messages with English and Turkish support out of the box.
- **Hex Color Support:** Support for modern Hex color codes (`&#ffffff`) in all messages.
- **PlaceholderAPI (PAPI) Support:** Use any PAPI placeholders in your messages.

## Commands

- `/kont al <player>`: Starts a check for a player.
- `/kont konuş <player> <message>`: Private chat with the player being checked.
- `/kont bitir <player> <result>`: Ends the check and saves the result (e.g., "Cheater", "Clean").
- `/kont liste`: Opens a GUI to browse through all past check histories.
- `/kont aktifliste`: Lists players currently being checked in the chat.
- `/kont geçmiş <player>`: Opens a GUI (for players) or displays (for console) a specific player's check history.
- `/kont alanekle <area_name>`: Sets the current location as a check area.
- `/kont alansil <area_name>`: Removes a check area.
- `/kont reload`: Reloads the configuration and language files.

## Permissions

- `kcheatcheck.admin`: Allows use of all `/kont` commands.

## Installation

1. Download the `kCheatCheck.jar`.
2. Place it in your server's `plugins` folder.
3. (Optional) Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) for placeholder support.
4. Restart your server.
5. Configure your settings in `plugins/kCheatCheck/config.yml`.

## Developer

Developed by **kitamor**.
