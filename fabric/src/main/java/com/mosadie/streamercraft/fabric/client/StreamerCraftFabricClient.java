package com.mosadie.streamercraft.fabric.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mosadie.streamercraft.Action;
import com.mosadie.streamercraft.StreamerCraft;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.ArrayList;
import java.util.List;

import static com.mosadie.streamercraft.StreamerCraft.*;

public final class StreamerCraftFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        StreamerCraft.init();

        ClientCommandRegistrationCallback.EVENT.register(this::registerClientCommand);
        ClientReceiveMessageEvents.ALLOW_GAME.register(this::handleGameMessage);
    }

    private boolean handleGameMessage(Component component, boolean b) {
        if (component.getContents() instanceof TranslatableContents translatableContents) {
            return !handleTranslatableContent(translatableContents);
        }

        return true;
    }

    public void registerClientCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext commandBuildContext) {
        // Create a Brigadier command that can be used in Minecraft
        // The command should list all available actions and allow the player to do an action

        dispatcher.register(ClientCommandManager.literal("streamercraft")
                .then(ClientCommandManager.literal("refresh").executes(context -> {
                            // Refresh the list of available actions
                            List<Action> newActions = StreamerCraft.getActionList();
                            if (newActions != null) {
                                actions.clear();
                                actions.addAll(newActions);
                                context.getSource().sendFeedback(Component.literal("[SC] Refreshed actions.").withStyle(ChatFormatting.GREEN));
                                return 1;
                            }
                            context.getSource().sendFeedback(Component.literal("[SC] Failed to refresh actions.").withStyle(ChatFormatting.RED));
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("list")
                                .then(ClientCommandManager.argument("filter", StringArgumentType.word()).executes(context -> {
                                    // List all available actions that match the filter
                                    List<Action> filteredActions = new ArrayList<>();
                                    for (Action action : actions) {
                                        if (action.name.contains(StringArgumentType.getString(context, "filter"))) {
                                            filteredActions.add(action);
                                        }
                                    }

                                    if (!filteredActions.isEmpty()) {
                                        // print the filtered actions
                                        LOGGER.info("Filtered actions: " + filteredActions.size());
                                        SendActionList(filteredActions);
                                    } else {
                                        context.getSource().sendFeedback(Component.literal("[SC] No actions found.").withStyle(ChatFormatting.RED));
                                    }
                                    return 1;
                                }))
                                .executes(context -> {
                                    // List all available actions
                                    if (!actions.isEmpty()) {
                                        LOGGER.info("All actions: " + actions.size());
                                        SendActionList(actions);
                                    } else {
                                        context.getSource().sendFeedback(Component.literal("[SC] No actions found.").withStyle(ChatFormatting.RED));
                                    }
                                    return 1;
                                })
                        ).then(ClientCommandManager.literal("do")
                                .then(ClientCommandManager.argument("id", StringArgumentType.word()).executes(context -> {
                                    // Do the action with the given id
                                    String id = StringArgumentType.getString(context, "id");
                                    Action action = actions.stream().filter(a -> a.id.equals(id)).findFirst().orElse(null);

                                    if (action != null) {
                                        if (doAction(action, null)) {
                                            context.getSource().sendFeedback(Component.literal("[SC] Action done.").withStyle(ChatFormatting.GREEN));
                                        } else {
                                            context.getSource().sendFeedback(Component.literal("[SC] Failed to do action.").withStyle(ChatFormatting.RED));
                                        }
                                    } else {
                                        context.getSource().sendFeedback(Component.literal("[SC] Action not found.").withStyle(ChatFormatting.RED));
                                    }
                                    return 1;
                                }))
                                .executes(context -> {
                                    // Show help
                                    context.getSource().sendFeedback(Component.literal("[SC] Usage: /streamercraft do <id>"));
                                    return 1;
                                })
                        ).executes(context -> {
                            // Show help
                            context.getSource().sendFeedback(Component.literal("[SC] Usage: /streamercraft <refresh|list|do>"));
                            return 1;
                        }));
    }
}
