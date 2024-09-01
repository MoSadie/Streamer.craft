package com.mosadie.streamercraft.request;

import com.mosadie.streamercraft.Action;
import com.mosadie.streamercraft.StreamerCraft;

import java.util.Map;

public class DoAction {
    private final Action action;
    private final Map<String, String> args;

    public DoAction(Action action, Map<String, String> args) {
        this.action = action;
        this.args = args;
    }

    public Action getAction() {
        return action;
    }

    public Map<String, String> getArgs() {
        return args;
    }

    public String getTellRawComponent() {
        try {
            return StreamerCraft.GSON_COMPRESSED.toJson(new ActionTranslatableComponent(StreamerCraft.GSON_COMPRESSED.toJson(this)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class ActionTranslatableComponent {
        public final String type = "translatable";
        public final String translate = StreamerCraft.TRANSLATION_TRIGGER;
        public final String fallback = "";

        public final String[] with;

        public ActionTranslatableComponent(String actionJson) {
            this.with = new String[]{actionJson};
        }
    }
}
