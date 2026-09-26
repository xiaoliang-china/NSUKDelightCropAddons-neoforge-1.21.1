package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 同时缓存 en_us / zh_cn 语言文件，搜索框可以跨当前游戏语言匹配中英文。
 */
public final class CropLangIndex {
    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static final Map<String, String> EN = new HashMap<>();
    private static final Map<String, String> ZH = new HashMap<>();

    private CropLangIndex() {
    }

    public static void refresh() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }
        refresh(minecraft.getResourceManager());
    }

    public static void refresh(ResourceManager resourceManager) {
        EN.clear();
        ZH.clear();
        if (resourceManager == null) {
            return;
        }
        try {
            Map<ResourceLocation, Resource> resources = resourceManager.listResources("lang", location -> {
                String path = location.getPath();
                return path.endsWith("en_us.json") || path.endsWith("zh_cn.json");
            });
            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                boolean english = entry.getKey().getPath().endsWith("en_us.json");
                try (var reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                    JsonElement element = GSON.fromJson(reader, JsonElement.class);
                    if (element == null || !element.isJsonObject()) {
                        continue;
                    }
                    JsonObject object = element.getAsJsonObject();
                    Map<String, String> target = english ? EN : ZH;
                    for (Map.Entry<String, JsonElement> line : object.entrySet()) {
                        if (line.getValue() != null && line.getValue().isJsonPrimitive()) {
                            target.put(line.getKey(), line.getValue().getAsString());
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static String en(String key) {
        return EN.getOrDefault(key, "");
    }

    public static String zh(String key) {
        return ZH.getOrDefault(key, "");
    }

    public static String current(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        String value = Language.getInstance().getOrDefault(key);
        return value == null || value.equals(key) ? "" : value;
    }

    public static boolean hasTranslation(String key) {
        return !current(key).isBlank();
    }

    public static String haystack(String... parts) {
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(part.toLowerCase(Locale.ROOT));
        }
        return builder.toString();
    }
}
