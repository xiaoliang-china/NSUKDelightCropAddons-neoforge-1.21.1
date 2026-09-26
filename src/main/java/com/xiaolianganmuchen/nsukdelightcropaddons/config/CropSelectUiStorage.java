package com.xiaolianganmuchen.nsukdelightcropaddons.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xiaolianganmuchen.nsukdelightcropaddons.NSUKDelightCropAddons;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 记住作物选择界面里各乐事分组上次是展开还是收起。
 */
public final class CropSelectUiStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Set<String> EXPANDED = ConcurrentHashMap.newKeySet();
    private static boolean loaded;

    private CropSelectUiStorage() {
    }

    public static boolean isExpanded(String modId) {
        load();
        return modId != null && EXPANDED.contains(modId);
    }

    public static void toggle(String modId) {
        if (modId == null || modId.isBlank()) {
            return;
        }
        load();
        if (!EXPANDED.add(modId)) {
            EXPANDED.remove(modId);
        }
        save();
    }

    private static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        EXPANDED.clear();
        Path path = file();
        if (!Files.isRegularFile(path)) {
            return;
        }
        try {
            JsonObject root = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), JsonObject.class);
            if (root == null || !root.has("expanded") || !root.get("expanded").isJsonArray()) {
                return;
            }
            for (JsonElement element : root.getAsJsonArray("expanded")) {
                String id = element.getAsString();
                if (id != null && !id.isBlank()) {
                    EXPANDED.add(id);
                }
            }
        } catch (Exception exception) {
            NSUKDelightCropAddons.LOGGER.warn("Failed to read crop select UI state", exception);
        }
    }

    private static void save() {
        JsonObject root = new JsonObject();
        JsonArray expanded = new JsonArray();
        EXPANDED.stream().sorted().forEach(expanded::add);
        root.add("expanded", expanded);
        Path path = file();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            NSUKDelightCropAddons.LOGGER.warn("Failed to save crop select UI state", exception);
        }
    }

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("nsukdelightcropaddons-crop-ui.json");
    }
}
