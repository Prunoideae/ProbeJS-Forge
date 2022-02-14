package com.prunoideae.probejs.dump;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.prunoideae.probejs.plugin.WrappedEventHandler;

public class ProcessEvent {
    public static JsonArray fetchEventData() {
        JsonArray eventJson = new JsonArray();
        WrappedEventHandler.capturedEvents.forEach((k, v) -> {
            JsonObject keyJson = new JsonObject();
            keyJson.addProperty("name", k);
            keyJson.addProperty("classname", v.getName());
            eventJson.add(keyJson);
        });
        return eventJson;
    }
}
