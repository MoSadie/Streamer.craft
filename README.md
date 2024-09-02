# Streamer.craft

Trigger Streamer.bot Actions from inside Minecraft!

## Setup guide

Video setup guide: https://youtu.be/-GzuuOlfyGE

### Step 1: Install the mod

There are versions of this mod available for NeoForge and Fabric. You can download the latest version of the mod from the [releases page](https://github.com/MoSadie/Streamer.craft/releases/latest). (Coming soon to CurseForge and Modrinth)

### Step 2: Set up Streamer.bot

This mod is designed to work with the stream automation tool [Streamer.bot](https://streamer.bot). Download the program and unzip it.

The settings needed to connect Streamer.bot to the mod are as follows:

- Under the "Servers/Clients" tab, click "HTTP Server" and check Start Server (I'd also recommend checking "Auto Start" so the server starts automatically with Streamer.bot)

Take note of the IP and port listed in that menu, if those are changed from the defaults you'll need to update the mod's config file.

To connect to a Twitch or YouTube chat, sign in to your account in the Platforms tab.

To connect to OBS, follow [these steps](https://docs.streamer.bot/get-started/setup#obs-studio) to enable OBS's websocket server and add it to Streamer.bot

### Step 3: Make sure it is all working

Once you have Streamer.bot set up, start the program and make sure the HTTP server is running. Then, start Minecraft with the mod installed.

You'll also want to [create at least one Action in Streamer.bot](https://docs.streamer.bot/guide/actions).

Load a world and type `/streamercraft list` in the chat. If everything works, you should see a list of Streamer.bot Actions in the chat.
Try pressing the "Click to manually trigger" to make sure you can trigger actions as well.

## All Commands

- `/streamercraft list` - List all available actions
- `/streamercraft list <filter>` - List all available actions that contain the filter
- `/streamercraft do <action-id>` - Trigger a specific action
- `/streamercraft refresh` - Refresh the list of actions
