# Streamer.craft - Paper Edition

Trigger Streamer.bot Actions from inside Minecraft!

## Setup guide

Video setup guide: (coming soon)

### Step 1: Install the plugin

You can download the latest version of the plugin from [Modrinth](https://modrinth.com/mod/streamer.craft/versions?l=paper) or the [releases page](https://github.com/MoSadie/Streamer.craft/releases/latest).

### Step 2: Set up Streamer.bot

This plugin is designed to work with the stream automation tool [Streamer.bot](https://streamer.bot). Download the program and unzip it.

The settings needed to connect Streamer.bot to the plugin are as follows:

- Under the "Servers/Clients" tab, click "HTTP Server" and check Start Server (I'd also recommend checking "Auto Start" so the server starts automatically with Streamer.bot)

Take note of the IP and port listed in that menu, if those are changed from the defaults you may need to update the plugin's config file.

To connect to a Twitch or YouTube chat, sign in to your account in the Platforms tab.

To connect to OBS, follow [these steps](https://docs.streamer.bot/get-started/setup#obs-studio) to enable OBS's websocket server and add it to Streamer.bot

### Step 3: Update Plugin Configuration

Depending on your setup, you may need to do one of the following sections:

<details>
<summary>If Streamer.bot and Paper are running on the same computer.</summary>

If you are using the default settings in Streamer.bot, no changes should be needed in the config file, and you should be able to run `/streamercraft list` and see the list of actions.

If you changed the port, change the numbers in the config file to match.
</details>

<details>
<summary>If Streamer.bot and Paper are on different computers on the SAME NETWORK.</summary>

You will need to update the plugin's config file.

Get the local IP of the computer running Streamer.bot and replace the `http://localhost:7474` with `http://<IP>:7474` (if you changed the port used, update that as well)
</details>

<details>
<summary>If Streamer.bot and Paper are on different computers on DIFFERENT NETWORKS. (Ex Paper hosted by a hosting company)</summary>

You will need to update the plugin's config file.

If you are comfortable port forwarding, you can forward the port used by Streamer.bot (7474 by default) to the computer running Streamer.bot. Then, replace `http://localhost:7474` with `http://<Your Public IP>:7474` (if you changed the port used, update that as well)

Otherwise, I would recommend using [ngrok](https://ngrok.com/) on the computer running Streamer.bot to create a unique address you can put in the config file. They have a quickstart guide [here](https://ngrok.com/docs/getting-started/) to walk you through setup. (Just make sure to use the port you have configured in Streamer.bot, 7474 by default)

</details>

### Step 4: Make sure it is all working

Once you have Streamer.bot set up, start the program and make sure the HTTP server is running. Then, start Minecraft with the plugin installed.

You'll also want to [create at least one Action in Streamer.bot](https://docs.streamer.bot/guide/actions).

Load a world and type `/streamercraft list` in the chat. If everything works, you should see a list of Streamer.bot Actions in the chat.
Try pressing the "Click to manually trigger" to make sure you can trigger actions as well.

## All Commands

- `/streamercraft list` - List all available actions
- `/streamercraft list <filter>` - List all available actions that contain the filter
- `/streamercraft do <action-id>` - Trigger a specific action **(This can also be a command block)**
- `/streamercraft refresh` - Refresh the list of actions
