package com.mosadie.streamercraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mosadie.streamercraft.request.DoAction;
import com.mosadie.streamercraft.response.GetActions;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.events.client.ClientChatEvent;
import dev.architectury.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StreamerCraft {
    public static final String MOD_ID = "streamercraft";

    public static final String TRANSLATION_TRIGGER = "com.mosadie.streamercraft.trigger";

    public static Gson GSON_PRETTY;

    public static Gson GSON_COMPRESSED;

    private static HttpClient httpClient;

    private static Config config;

    public static Logger LOGGER = LogManager.getLogger();

    public static final List<Action> actions = new ArrayList<>();

    public static void init() {
        GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();
        GSON_COMPRESSED = new GsonBuilder().create();
        httpClient = HttpClient.newHttpClient();

        File configFile = Platform.getConfigFolder().resolve("streamercraft.json").toFile();

        if (configFile.exists()) {
            try {
                FileReader reader = new FileReader(configFile);
                config = GSON_PRETTY.fromJson(reader, Config.class);
            } catch (FileNotFoundException e) {
                LOGGER.error("Failed to find config file. Using default config.");
                config = Config.defaultConfig();
            } catch (Exception e) {
                LOGGER.error("Failed to read config file. Using default config.");
                config = Config.defaultConfig();
            }
        } else {
            config = Config.defaultConfig();
            try {
                if (configFile.createNewFile()) {
                    LOGGER.info("Created config file.");
                    // Write default config to file
                    FileWriter writer = new FileWriter(configFile);
                    GSON_PRETTY.toJson(config, writer);
                    writer.close();
                } else {
                    LOGGER.error("Failed to create default config file.");
                }
            } catch (Exception e) {
                LOGGER.error("Failed to create config file.");
                LOGGER.error(e);
            }
        }

        refreshActionList();

        LOGGER.info("StreamerCraft initialized.");
    }

    private static void refreshActionList() {
        List<Action> newActions = getActionList();
        if (newActions != null) {
            actions.clear();
            actions.addAll(newActions);
        }
        LOGGER.info("Refreshed actions.");
    }

    public static List<Action> getActionList() {
        // Make an HTTP get request to Streamer.bot to get the actions
        // Parse the JSON response and return a list of Action objects

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + config.streamerBotIp + ":" + config.streamerBotPort + "/GetActions"))
                    .GET()
                    .build();

            // Send the request and get the response
            // Parse the response and return a GetActions object

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            GetActions getActions = GSON_PRETTY.fromJson(response.body(), GetActions.class);

            return List.of(getActions.actions);

        } catch (Exception e) {
            LOGGER.error("Failed to get actions from Streamer.bot.");
            LOGGER.error(e);
            return null;
        }
    }

    public static boolean doAction(DoAction doAction) {
        return doAction(doAction.getAction(), doAction.getArgs());
    }

    public static boolean doAction(Action action, Map<String, String> args) {
        // Make an HTTP post request to Streamer.bot to do the action
        // Parse the JSON response and return a boolean indicating success

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + config.streamerBotIp + ":" + config.streamerBotPort + "/DoAction"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(GSON_PRETTY.toJson(new DoAction(action, args))))
                    .build();

            // Send the request and get the response
            // Parse the response and return a boolean indicating success

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 204;

        } catch (Exception e) {
            LOGGER.error("Failed to do action with Streamer.bot.");
            LOGGER.error(e);
            return false;
        }
    }

    public static boolean handleTranslatableContent(TranslatableContents translatableContents) {
        // Handle a translatable content
        // If the translation key is the trigger, parse the action and do it

        if (translatableContents.getKey().equals(TRANSLATION_TRIGGER)) {
            try {
                DoAction doAction = GSON_COMPRESSED.fromJson(translatableContents.getArgs()[0].toString(), DoAction.class);
                boolean result = doAction(doAction);
                LOGGER.info("Triggered action: " + doAction.getAction().name + " Result: " + result);
                return result;
            } catch (Exception e) {
                LOGGER.error("Failed to parse translatable content.");
                LOGGER.error(e);

            }
        }
        return false;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> getNeoForgeCommand() {
        // Create a Brigadier command that can be used in Minecraft
        // The command should list all available actions and allow the player to do an action

        return Commands.literal("streamercraft")
                .then(Commands.literal("refresh").executes(context -> {
                    // Refresh the list of available actions
                    List<Action> newActions = getActionList();
                    if (newActions != null) {
                        actions.clear();
                        actions.addAll(newActions);
                        context.getSource().sendSystemMessage(Component.literal("[SC] Refreshed actions.").withStyle(ChatFormatting.GREEN));
                        return 1;
                    }
                    context.getSource().sendSystemMessage(Component.literal("[SC] Failed to refresh actions.").withStyle(ChatFormatting.RED));
                    return 1;
                }))
                .then(Commands.literal("list")
                        .then(Commands.argument("filter", StringArgumentType.word()).executes(context -> {
                            // List all available actions that match the filter
                            List<Action> filteredActions = new ArrayList<>();
                            for (Action action : actions) {
                                if (action.name.contains(StringArgumentType.getString(context, "filter"))) {
                                    filteredActions.add(action);
                                }
                            }

                            if (!filteredActions.isEmpty()) {
                                SendActionList(filteredActions);
                            } else {
                                context.getSource().sendSystemMessage(Component.literal("[SC] No actions found.").withStyle(ChatFormatting.RED));
                            }
                            return 1;
                        }))
                        .executes(context -> {
                            // List all available actions
                            if (!actions.isEmpty()) {
                                SendActionList(actions);
                            } else {
                                context.getSource().sendSystemMessage(Component.literal("[SC] No actions found.").withStyle(ChatFormatting.RED));
                            }
                            return 1;
                        })
                ).then(Commands.literal("do")
                        .then(Commands.argument("id", StringArgumentType.word()).executes(context -> {
                            // Do the action with the given id
                            String id = StringArgumentType.getString(context, "id");
                            Action action = actions.stream().filter(a -> a.id.equals(id)).findFirst().orElse(null);

                            if (action != null) {
                                if (doAction(action, null)) {
                                    context.getSource().sendSystemMessage(Component.literal("[SC] Action done.").withStyle(ChatFormatting.GREEN));
                                } else {
                                    context.getSource().sendSystemMessage(Component.literal("[SC] Failed to do action.").withStyle(ChatFormatting.RED));
                                }
                            } else {
                                context.getSource().sendSystemMessage(Component.literal("[SC] Action not found.").withStyle(ChatFormatting.RED));
                            }
                            return 1;
                        }))
                        .executes(context -> {
                            // Show help
                            context.getSource().sendSystemMessage(Component.literal("[SC] Usage: /streamercraft do <id>"));
                            return 1;
                        })
                ).executes(context -> {
                    // Show help
                    context.getSource().sendSystemMessage(Component.literal("[SC] Usage: /streamercraft <refresh|list|do>"));
                    return 1;
                });
    }

    public static void SendActionList(List<Action> actions) {
        // Open a Book GUI that shows all available actions
        // The player can select an action to do

        Minecraft.getInstance().player.sendSystemMessage(Component.literal("[SC] Available Actions:").withStyle(ChatFormatting.GREEN));

        for (Action action : actions) {
            MutableComponent header = Component.literal("--- " + action.name + " ---").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD);
            MutableComponent id = Component.literal(action.id).withStyle(ChatFormatting.GRAY).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, action.id))).withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy the action ID"))));

            MutableComponent blankLine = Component.literal("\n");

            DoAction doAction = new DoAction(action, new HashMap<>());
            MutableComponent tellrawButton = Component.literal("[Click to copy /tellraw command]").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x00FF00)).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "/tellraw @p " + doAction.getTellRawComponent())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy /tellraw command"))));
            MutableComponent componentButton = Component.literal("[Click to copy Translation Component]").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, doAction.getTellRawComponent())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy the raw Translation Component JSON"))));
            MutableComponent triggerButton = Component.literal("[Click to manually trigger]").withStyle(Style.EMPTY.withColor(ChatFormatting.RED).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/streamercraft do " + action.id)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to manually trigger the action"))));

            Component message = header.append(blankLine).append(id).append(blankLine).append(blankLine).append(tellrawButton).append(blankLine).append(componentButton).append(blankLine).append(triggerButton);

            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().execute(() -> {
                    Minecraft.getInstance().player.sendSystemMessage(blankLine);
                    Minecraft.getInstance().player.sendSystemMessage(message);
                });
            }
        }
    }
}
