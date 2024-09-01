package com.mosadie.streamercraft.response;

import com.mosadie.streamercraft.Action;

public class GetActions {
    public Action[] actions;

    public Action getActionById(String id) {
        for (Action action : actions) {
            if (action.id.equals(id)) {
                return action;
            }
        }
        return null;
    }

    public Action getActionByName(String name) {
        for (Action action : actions) {
            if (action.name.equals(name)) {
                return action;
            }
        }
        return null;
    }
}
