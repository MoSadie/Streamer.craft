package com.mosadie.streamercraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mosadie.streamercraft.request.DoAction;
import com.mosadie.streamercraft.response.GetActions;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

public final class StreamerCraft extends JavaPlugin implements Listener {
    public static final String TRANSLATION_TRIGGER = "com.mosadie.streamercraft.trigger";

    public static Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();

    public static Gson GSON_COMPRESSED = new GsonBuilder().create();

    private static HttpClient httpClient = HttpClient.newHttpClient();

    private FileConfiguration config;

    public final List<Action> actions = new ArrayList<>();

    @Override
    public void onEnable() {
        config = getConfig();

        config.addDefault("streamerBotAddress", "http://127.0.0.1:7474");

        config.options().copyDefaults(true);
        saveConfig();

        // Register the command
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(getCommand().build());
        });

        getServer().getPluginManager().registerEvents(this, this);

        refreshActionList();

        getLogger().info("StreamerCraft enabled.");
    }

    private  void refreshActionList() {
        List<Action> newActions = getActionList();
        if (newActions != null) {
            actions.clear();
            actions.addAll(newActions);
        }
        getLogger().info("Refreshed actions.");
    }

    public List<Action> getActionList() {
        // Make an HTTP get request to Streamer.bot to get the actions
        // Parse the JSON response and return a list of Action objects

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getString("streamerBotAddress") + "/GetActions"))
                    .header("ngrok-skip-browser-warning", "true")
                    .GET()
                    .build();

            // Send the request and get the response
            // Parse the response and return a GetActions object

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            GetActions getActions = GSON_PRETTY.fromJson(response.body(), GetActions.class);

            return List.of(getActions.actions);

        } catch (Exception e) {
            getLogger().warning("Failed to get actions from Streamer.bot.");
            getLogger().warning(e.toString());
            return null;
        }
    }

    public boolean doAction(DoAction doAction) {
        return doAction(doAction.getAction(), doAction.getArgs());
    }

    public boolean doAction(Action action, Map<String, String> args) {
        // Make an HTTP post request to Streamer.bot to do the action
        // Parse the JSON response and return a boolean indicating success

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getString("streamerBotAddress") + "/DoAction"))
                    .header("Content-Type", "application/json")
                    .header("ngrok-skip-browser-warning", "true")
                    .POST(HttpRequest.BodyPublishers.ofString(GSON_PRETTY.toJson(new DoAction(action, args))))
                    .build();

            // Send the request and get the response
            // Parse the response and return a boolean indicating success

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 204;

        } catch (Exception e) {
            getLogger().warning("Failed to do action with Streamer.bot.");
            getLogger().warning(e.toString());
            return false;
        }
    }

    public LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        // Create a Brigadier command that can be used in Minecraft
        // The command should list all available actions and allow the player to do an action

        return Commands.literal("streamercraft")
                .then(Commands.literal("refresh").requires(sender -> sender.getSender().hasPermission("streamercraft.refresh")).executes(context -> {
                    // Refresh the list of available actions
                    List<Action> newActions = getActionList();
                    if (newActions != null) {
                        actions.clear();
                        actions.addAll(newActions);
                        context.getSource().getSender().sendMessage(text().content("[SC] Refreshed actions.").color(TextColor.color(0x55FF55)).build());
                        return 1;
                    }
                    context.getSource().getSender().sendMessage(text().content("[SC] Failed to refresh actions.").color(TextColor.color(0xFF5555)).build());
                    return 1;
                }))
                .then(Commands.literal("list")
                        .requires(sender -> sender.getSender().hasPermission("streamercraft.list"))
                        .then(Commands.argument("filter", StringArgumentType.word()).executes(context -> {
                            // List all available actions that match the filter
                            List<Action> filteredActions = new ArrayList<>();
                            for (Action action : actions) {
                                if (action.name.contains(StringArgumentType.getString(context, "filter"))) {
                                    filteredActions.add(action);
                                }
                            }

                            if (!filteredActions.isEmpty()) {
                                SendActionList(filteredActions, context.getSource());
                            } else {
                                context.getSource().getSender().sendMessage(text().content("[SC] No actions found.").color(TextColor.color(0xFF5555)).build());
                            }
                            return 1;
                        }))
                        .executes(context -> {
                            // List all available actions
                            if (!actions.isEmpty()) {
                                SendActionList(actions, context.getSource());
                            } else {
                                context.getSource().getSender().sendMessage(text().content("[SC] No actions found.").color(TextColor.color(0xFF5555)).build());
                            }
                            return 1;
                        })
                ).then(Commands.literal("do")
                        .requires(sender -> sender.getSender().hasPermission("streamercraft.do"))
                        .then(Commands.argument("id", StringArgumentType.word()).executes(context -> {
                            // Do the action with the given id
                            String id = StringArgumentType.getString(context, "id");
                            Action action = actions.stream().filter(a -> a.id.equals(id)).findFirst().orElse(null);

                            if (action != null) {
                                if (doAction(action, null)) {
                                    context.getSource().getSender().sendMessage(text().content("[SC] Action done.").color(TextColor.color(0x55FF55)).build());
                                } else {
                                    context.getSource().getSender().sendMessage(text().content("[SC] Failed to do action.").color(TextColor.color(0xFF5555)).build());
                                }
                            } else {
                                context.getSource().getSender().sendMessage(text().content("[SC] Action not found.").color(TextColor.color(0xFF5555)).build());
                            }
                            return 1;
                        }))
                        .executes(context -> {
                            // Show help
                            context.getSource().getSender().sendMessage(text().content("[SC] Usage: /streamercraft do <id>"));
                            return 1;
                        })
                ).executes(context -> {
                    // Show help
                    context.getSource().getSender().sendMessage(text().content("[SC] Usage: /streamercraft <refresh|list|do>"));
                    return 1;
                });
    }

    final static Component ACTION_LIST_HEADER = text().content("[SC] Available Actions:").color(TextColor.color(0x55FF55)).build();

    public static void SendActionList(List<Action> actions, CommandSourceStack sender) {
        sender.getSender().sendMessage(ACTION_LIST_HEADER);

        for (Action action : actions) {
            Component message = text()
                    .append(text("--- " + action.name + " ---").color(TextColor.color(0x0000FF)).decoration(TextDecoration.BOLD, true))
                    .append(newline())
                    .append(text("ID: ").color(TextColor.color(0xAAAAAA)))
                    .append(text(action.id).color(TextColor.color(0xAAAAAA)).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, action.id)).hoverEvent(HoverEvent.showText(text("Click to copy the action ID"))))
                    .append(newline()).append(newline())
                    .append(text("[Click to copy /tellraw command]").color(TextColor.color(0x00FF00)).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "/tellraw @p " + new DoAction(action, new HashMap<>()).getTellRawComponent())).hoverEvent(HoverEvent.showText(text("Click to copy /tellraw command, for triggering CLIENTS running the mod locally"))))
                    .append(newline())
                    .append(text("[Click to copy Paper Command Block command]").color(TextColor.color(0xFF55FF)).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "/streamercraft do " + action.id)).hoverEvent(HoverEvent.showText(text("Click to copy the command for Command Blocks when using the Paper plugin"))))
                    .append(newline())
                    .append(text("[Click to copy Translation Component]").color(TextColor.color(0xFFAA00)).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, new DoAction(action, new HashMap<>()).getTellRawComponent())).hoverEvent(HoverEvent.showText(text("Click to copy the raw Translation Component JSON"))))
                    .append(newline())
                    .append(text("[Click to manually trigger]").color(TextColor.color(0xFF5555)).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/streamercraft do " + action.id)).hoverEvent(HoverEvent.showText(text("Click to manually trigger the action"))))
                    .build();

            sender.getSender().sendMessage(message);
        }
    }
}
