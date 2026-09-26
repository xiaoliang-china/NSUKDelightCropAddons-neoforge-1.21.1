package com.xiaolianganmuchen.nsukdelightcropaddons.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xiaolianganmuchen.nsukdelightcropaddons.NSUKDelightCropAddons;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropCatalog;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropIds;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropLangIndex;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropOrigin;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 玩家在模组配置界面新增的作物，保存在 config 目录，选择列表里归入「其他」。
 */
public final class CustomCropStorage {
    public static final String SOURCE_MOD = "custom";
    public static final String PREFIX = "cu";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final List<Entry> ENTRIES = new CopyOnWriteArrayList<>();

    private CustomCropStorage() {
    }

    public static List<Entry> entries() {
        return List.copyOf(ENTRIES);
    }

    public static void loadAndApply() {
        ENTRIES.clear();
        ENTRIES.addAll(readFile());
        apply();
    }

    public static void apply() {
        List<CropSpec> specs = new ArrayList<>();
        for (Entry entry : ENTRIES) {
            CropSpec spec = entry.toSpec();
            if (spec != null) {
                specs.add(spec);
            }
        }
        CropCatalog.replaceOrigin(CropOrigin.CUSTOM, specs);
    }

    public static boolean add(Entry entry) {
        if (entry == null || entry.seed == null || entry.seed.isBlank()) {
            return false;
        }
        Entry stored = entry.withId(uniqueId(entry));
        ENTRIES.add(stored);
        save();
        apply();
        return true;
    }

    public static void remove(String id) {
        ENTRIES.removeIf(entry -> entry.id.equals(id));
        save();
        apply();
    }

    public static Component displayName(String cropId) {
        Entry entry = find(cropId);
        if (entry == null) {
            return null;
        }
        String preferred = preferredName(entry);
        return preferred.isBlank() ? null : Component.literal(preferred);
    }

    public static String searchText(String cropId) {
        Entry entry = find(cropId);
        if (entry == null) {
            return "";
        }
        return CropLangIndex.haystack(entry.id, entry.seed, entry.plant, entry.nameZh, entry.nameEn);
    }

    private static Entry find(String id) {
        if (id == null) {
            return null;
        }
        for (Entry entry : ENTRIES) {
            if (id.equals(entry.id)) {
                return entry;
            }
        }
        return null;
    }

    private static String preferredName(Entry entry) {
        String language = "";
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.options != null && minecraft.options.languageCode != null) {
            language = minecraft.options.languageCode;
        }
        boolean chinese = language.toLowerCase(Locale.ROOT).startsWith("zh");
        if (chinese && !entry.nameZh.isBlank()) {
            return entry.nameZh;
        }
        if (!chinese && !entry.nameEn.isBlank()) {
            return entry.nameEn;
        }
        if (!entry.nameZh.isBlank()) {
            return entry.nameZh;
        }
        return entry.nameEn;
    }

    private static String uniqueId(Entry entry) {
        String base = entry.id == null || entry.id.isBlank()
                ? CropIds.compose(PREFIX, CropIds.keyFromSeedPath(ResourceLocation.tryParse(entry.seed) == null
                ? entry.seed
                : ResourceLocation.tryParse(entry.seed).getPath()))
                : CropIds.compose(PREFIX, entry.id);
        String id = base;
        int suffix = 2;
        while (CropCatalog.contains(id) || hasId(id)) {
            String extra = String.valueOf(suffix++);
            id = base.length() + extra.length() > CropSpec.MAX_ID_LENGTH
                    ? (base.substring(0, Math.max(1, CropSpec.MAX_ID_LENGTH - extra.length())) + extra)
                    : base + extra;
        }
        return id;
    }

    private static boolean hasId(String id) {
        for (Entry entry : ENTRIES) {
            if (entry.id.equals(id)) {
                return true;
            }
        }
        return false;
    }

    private static List<Entry> readFile() {
        Path path = file();
        if (!Files.isRegularFile(path)) {
            return List.of();
        }
        try {
            JsonElement element = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), JsonElement.class);
            JsonArray array = element != null && element.isJsonObject() && element.getAsJsonObject().has("crops")
                    ? element.getAsJsonObject().getAsJsonArray("crops")
                    : (element != null && element.isJsonArray() ? element.getAsJsonArray() : new JsonArray());
            List<Entry> loaded = new ArrayList<>();
            for (JsonElement crop : array) {
                if (crop.isJsonObject()) {
                    Entry entry = Entry.fromJson(crop.getAsJsonObject());
                    if (entry != null) {
                        loaded.add(entry);
                    }
                }
            }
            return loaded;
        } catch (Exception exception) {
            NSUKDelightCropAddons.LOGGER.error("Failed to read custom delight crops", exception);
            return List.of();
        }
    }

    private static void save() {
        JsonObject root = new JsonObject();
        JsonArray crops = new JsonArray();
        for (Entry entry : ENTRIES) {
            crops.add(entry.toJson());
        }
        root.add("crops", crops);
        Path path = file();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            NSUKDelightCropAddons.LOGGER.error("Failed to save custom delight crops", exception);
        }
    }

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("nsukdelightcropaddons-custom-crops.json");
    }

    public record Entry(
            String id,
            String seed,
            String plant,
            List<String> harvestBlocks,
            boolean allowNonCropBlock,
            String nameZh,
            String nameEn
    ) {
        public Entry withId(String nextId) {
            return new Entry(nextId, seed, plant, harvestBlocks, allowNonCropBlock, nameZh, nameEn);
        }

        public CropSpec toSpec() {
            ResourceLocation seedId = CropIds.parse(seed, "minecraft");
            if (seedId == null) {
                return null;
            }
            List<ResourceLocation> extra = new ArrayList<>();
            if (harvestBlocks != null) {
                for (String harvest : harvestBlocks) {
                    ResourceLocation extraId = CropIds.parse(harvest, seedId.getNamespace());
                    if (extraId != null) {
                        extra.add(extraId);
                    }
                }
            }
            return CropSpec.builder(id)
                    .sourceModId(SOURCE_MOD)
                    .sourcePrefix(PREFIX)
                    .seedId(seedId)
                    .plantBlockId(CropIds.parse(plant, seedId.getNamespace()))
                    .extraHarvestBlockIds(extra)
                    .allowNonCropBlock(allowNonCropBlock)
                    .origin(CropOrigin.CUSTOM)
                    .build();
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("id", id);
            json.addProperty("seed", seed);
            if (plant != null && !plant.isBlank()) {
                json.addProperty("plant", plant);
            }
            if (harvestBlocks != null && !harvestBlocks.isEmpty()) {
                JsonArray extra = new JsonArray();
                harvestBlocks.forEach(extra::add);
                json.add("harvest_blocks", extra);
            }
            json.addProperty("allow_non_crop_block", allowNonCropBlock);
            if (nameZh != null && !nameZh.isBlank()) {
                json.addProperty("name_zh", nameZh);
            }
            if (nameEn != null && !nameEn.isBlank()) {
                json.addProperty("name_en", nameEn);
            }
            return json;
        }

        public static Entry fromJson(JsonObject json) {
            String seed = string(json, "seed");
            if (seed.isBlank()) {
                return null;
            }
            List<String> extra = new ArrayList<>();
            if (json.has("harvest_blocks") && json.get("harvest_blocks").isJsonArray()) {
                for (JsonElement element : json.getAsJsonArray("harvest_blocks")) {
                    extra.add(element.getAsString());
                }
            }
            String id = string(json, "id");
            return new Entry(
                    id,
                    seed,
                    string(json, "plant"),
                    List.copyOf(extra),
                    json.has("allow_non_crop_block") && json.get("allow_non_crop_block").getAsBoolean(),
                    string(json, "name_zh"),
                    string(json, "name_en")
            );
        }

        private static String string(JsonObject json, String key) {
            if (!json.has(key) || json.get(key).isJsonNull()) {
                return "";
            }
            String value = json.get(key).getAsString();
            return value == null ? "" : value.trim();
        }
    }
}
