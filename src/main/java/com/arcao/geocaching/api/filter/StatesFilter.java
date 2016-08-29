package com.arcao.geocaching.api.filter;

import com.google.gson.stream.JsonWriter;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class StatesFilter implements Filter {
    private static final String NAME = "States";

    private final int[] states;

    public StatesFilter(int... states) {
        this.states = states;
    }

    @NotNull
    @Override
    public String name() {
        return NAME;
    }

    @Override
    public boolean valid() {
        return states != null && states.length > 0;
    }

    @Override
    public void writeJson(@NotNull JsonWriter w) throws IOException {
        w.name(NAME);
        w.beginObject();
        w.name("StateIds");
        w.beginArray();
        for (int state : states) {
            w.value(state);
        }
        w.endArray();
        w.endObject();
    }
}
