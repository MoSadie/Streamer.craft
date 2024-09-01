package com.mosadie.streamercraft.neoforge;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.fml.common.Mod;

import com.mosadie.streamercraft.StreamerCraft;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(StreamerCraft.MOD_ID)
public final class StreamerCraftNeoForge {
    public StreamerCraftNeoForge() {
        // Run our common setup.
        StreamerCraft.init();

        NeoForge.EVENT_BUS.addListener(this::onChatMessage);
        NeoForge.EVENT_BUS.addListener(this::registerClientCommand);
    }

    public void registerClientCommand(RegisterClientCommandsEvent event) {
        // Register our client command.
        event.getDispatcher().register(StreamerCraft.getNeoForgeCommand());
    }

    public void onChatMessage(ClientChatReceivedEvent event) {
        if (event.getMessage().getContents() instanceof TranslatableContents translatableContents) {
            StreamerCraft.handleTranslatableContent(translatableContents);
            event.setCanceled(true);
        }
    }
}
