package com.penikq.seyfmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class SeyfConfig {
    public static final int NORMAL = 0;   // обычный предмет, выбрасывается свободно
    public static final int CONFIRM = 1;  // выбрасывается только с подтверждением
    public static final int BLOCKED = 2;  // выбросить нельзя вообще

    public Map<String, Integer> items = new LinkedHashMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static SeyfConfig instance;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("seyfmodbypenikq.json");
    }

    public static SeyfConfig get() {
        if (instance == null) instance = load();
        return instance;
    }

    private static SeyfConfig load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                try (Reader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
                    SeyfConfig c = GSON.fromJson(r, SeyfConfig.class);
                    if (c != null) {
                        if (c.items == null) c.items = new LinkedHashMap<>();
                        return c;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        SeyfConfig c = new SeyfConfig();
        c.save();
        return c;
    }

    public void save() {
        try (Writer w = Files.newBufferedWriter(path(), StandardCharsets.UTF_8)) {
            GSON.toJson(this, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int modeOf(String id) {
        Integer m = items.get(id);
        return m == null ? NORMAL : m;
    }

    public boolean has(String id) {
        return items.containsKey(id);
    }

    public void setMode(String id, int mode) {
        items.put(id, mode);
        save();
    }

    public void remove(String id) {
        items.remove(id);
        save();
    }
}
