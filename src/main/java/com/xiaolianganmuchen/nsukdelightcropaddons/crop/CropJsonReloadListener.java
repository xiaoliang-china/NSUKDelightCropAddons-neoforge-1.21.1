package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xiaolianganmuchen.nsukdelightcropaddons.NSUKDelightCropAddons;
import com.xiaolianganmuchen.nsukdelightcropaddons.config.CustomCropStorage;
import com.xiaolianganmuchen.nsukdelightcropaddons.config.DelightCropConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 读取 {@code data/<namespace>/nsuk_crops/*.json}。
 * 单文件解析失败只记日志，绝不中断原版数据包重载，避免进档/建档卡死。
 */
public final class CropJsonReloadListener implements PreparableReloadListener {
    public static final CropJsonReloadListener INSTANCE = new CropJsonReloadListener();
    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static final String DIRECTORY = "nsuk_crops";
    private static final String[] BUILTIN_FILES = {
            "farmersdelight.json",
            "dumplings_delight.json",
            "pineapple_delight.json",
            "ubesdelight.json",
            "veggiesdelight.json",
            "corn_delight.json",
            "expandeddelight.json",
            "culturaldelights.json",
            "croptopia.json",
            "gan_delight_reborn.json",
            "trailandtales_delight.json",
            "immortalers_delight.json",
            "youkaisfeasts.json",
            "rusticdelight.json"
    };

    private CropJsonReloadListener() {
    }

    public static void loadBuiltInFromJar() {
        List<CropSpec> loaded = new ArrayList<>();
        for (String fileName : BUILTIN_FILES) {
            String path = "/data/" + NSUKDelightCropAddons.MODID + "/" + DIRECTORY + "/" + fileName;
            try (var stream = CropJsonReloadListener.class.getResourceAsStream(path)) {
                if (stream == null) {
                    continue;
                }
                JsonElement element = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonElement.class);
                if (element != null && element.isJsonObject()) {
                    loaded.addAll(parseFile(ResourceLocation.fromNamespaceAndPath(NSUKDelightCropAddons.MODID, fileName), element.getAsJsonObject()));
                }
            } catch (Exception exception) {
                NSUKDelightCropAddons.LOGGER.error("Failed to read built-in delight crop file {}", fileName, exception);
            }
        }
        CropCatalog.replaceOrigin(CropOrigin.JSON, loaded);
        try {
            CropDiscovery.discover(CropSourceIndex.discoverNamespaces());
        } catch (Exception exception) {
            NSUKDelightCropAddons.LOGGER.error("Delight crop auto-discovery failed", exception);
        }
        CustomCropStorage.loadAndApply();
        logUnresolved(loaded);
        NSUKDelightCropAddons.LOGGER.info("Loaded {} built-in delight crop definition(s)", loaded.size());
    }

    @Override
    public CompletableFuture<Void> reload(
            PreparationBarrier barrier,
            ResourceManager resourceManager,
            ProfilerFiller prepareProfiler,
            ProfilerFiller applyProfiler,
            Executor backgroundExecutor,
            Executor gameExecutor
    ) {
        // 扫描失败也必须走进 barrier.wait，否则第一次进档/进游戏会卡在加载画面。
        return CompletableFuture
                .supplyAsync(() -> scanQuietly(resourceManager), backgroundExecutor)
                .exceptionally(throwable -> {
                    NSUKDelightCropAddons.LOGGER.error("Delight crop scan failed", throwable);
                    return Map.of();
                })
                .thenCompose(barrier::wait)
                .thenAcceptAsync(this::applySafe, gameExecutor)
                .exceptionally(throwable -> {
                    NSUKDelightCropAddons.LOGGER.error("Delight crop apply failed", throwable);
                    return null;
                });
    }

    private Map<ResourceLocation, JsonElement> scanQuietly(ResourceManager resourceManager) {
        try {
            return scan(resourceManager);
        } catch (Throwable throwable) {
            NSUKDelightCropAddons.LOGGER.error("Delight crop scan crashed", throwable);
            return Map.of();
        }
    }

    private Map<ResourceLocation, JsonElement> scan(ResourceManager resourceManager) {
        Map<ResourceLocation, JsonElement> entries = new LinkedHashMap<>();
        try {
            Map<ResourceLocation, Resource> resources = resourceManager.listResources(DIRECTORY, location -> location.getPath().endsWith(".json"));
            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                ResourceLocation id = entry.getKey();
                if (id.getPath().contains("/_")) {
                    continue;
                }
                try (var reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                    JsonElement element = GSON.fromJson(reader, JsonElement.class);
                    if (element != null) {
                        entries.put(id, element);
                    }
                } catch (Exception exception) {
                    NSUKDelightCropAddons.LOGGER.error("Skipped invalid delight crop json {}", id, exception);
                }
            }
        } catch (Exception exception) {
            NSUKDelightCropAddons.LOGGER.error("Failed to scan nsuk_crops json", exception);
        }
        return entries;
    }

    private void applySafe(Map<ResourceLocation, JsonElement> entries) {
        try {
            apply(entries == null ? Map.of() : entries);
        } catch (Throwable throwable) {
            NSUKDelightCropAddons.LOGGER.error("Delight crop reload failed; vanilla datapacks continue", throwable);
        }
    }

    private void apply(Map<ResourceLocation, JsonElement> entries) {
        List<CropSpec> loaded = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
            try {
                if (entry.getKey().getPath().contains("/_") || !entry.getValue().isJsonObject()) {
                    continue;
                }
                loaded.addAll(parseFile(entry.getKey(), entry.getValue().getAsJsonObject()));
            } catch (Exception exception) {
                NSUKDelightCropAddons.LOGGER.error("Failed to parse delight crop file {}", entry.getKey(), exception);
            }
        }
        if (loaded.isEmpty()) {
            NSUKDelightCropAddons.LOGGER.warn("Delight crop reload produced 0 JSON crops; keeping existing JSON definitions");
        } else {
            CropCatalog.replaceOrigin(CropOrigin.JSON, loaded);
        }
        if (DelightCropConfig.configReady() && DelightCropConfig.autoDiscovery()) {
            CropDiscovery.discover(CropSourceIndex.discoverNamespaces());
        }
        CustomCropStorage.loadAndApply();
        logUnresolved(loaded);
        NSUKDelightCropAddons.LOGGER.info("Loaded {} JSON delight crop definition(s)", loaded.size());
    }

    private static void logUnresolved(List<CropSpec> loaded) {
        for (CropSpec spec : loaded) {
            if (ResolvedCrop.resolve(spec).isEmpty()) {
                NSUKDelightCropAddons.LOGGER.warn(
                        "Delight crop {} is listed but not selectable (seed={}, plant={})",
                        spec.id(),
                        spec.seedId(),
                        spec.plantBlockId()
                );
            }
        }
    }

    private static List<CropSpec> parseFile(ResourceLocation fileId, JsonObject root) {
        String modId = string(root, "mod", fileId.getPath());
        if ("mynethersdelight".equals(modId) || "ends_delight".equals(modId) || !CropCatalog.isModLoaded(modId)) {
            return List.of();
        }
        String prefix = string(root, "prefix", CropSourceIndex.prefixFor(modId));
        boolean discover = !root.has("discover") || root.get("discover").getAsBoolean();
        CropSourceIndex.register(modId, prefix, discover);
        if (root.has("label")) {
            CropCatalog.registerSourceLabel(modId, root.get("label").getAsString());
        }
        JsonArray crops = root.has("crops") && root.get("crops").isJsonArray() ? root.getAsJsonArray("crops") : new JsonArray();
        List<CropSpec> specs = new ArrayList<>();
        for (JsonElement element : crops) {
            if (!element.isJsonObject()) {
                continue;
            }
            parseCrop(modId, prefix, element.getAsJsonObject()).ifPresent(specs::add);
        }
        return specs;
    }

    private static java.util.Optional<CropSpec> parseCrop(String modId, String prefix, JsonObject json) {
        String key = string(json, "id", "");
        ResourceLocation seedId = CropIds.parse(string(json, "seed", ""), modId);
        if (key.isBlank() || seedId == null) {
            return java.util.Optional.empty();
        }
        String id = CropIds.compose(prefix, key);
        ResourceLocation plantId = CropIds.parse(string(json, "plant", ""), modId);
        List<ResourceLocation> extra = idList(json, "harvest_blocks", modId);
        List<ResourceLocation> growth = idList(json, "growth_blocks", modId);
        ResourceLocation produceId = CropIds.parse(string(json, "produce", ""), modId);
        CropLayout layout = CropLayout.fromName(string(json, "layout", "full"));
        boolean waterlog = json.has("waterlog") && json.get("waterlog").getAsBoolean();
        boolean allowNonCrop = json.has("allow_non_crop_block") && json.get("allow_non_crop_block").getAsBoolean();
        int harvestHeight = json.has("harvest_height") ? json.get("harvest_height").getAsInt() : 0;
        boolean harvestMaxAge = !json.has("harvest_max_age") || json.get("harvest_max_age").getAsBoolean();
        try {
            return java.util.Optional.of(CropSpec.builder(id)
                    .sourceModId(modId)
                    .sourcePrefix(prefix)
                    .seedId(seedId)
                    .plantBlockId(plantId)
                    .extraHarvestBlockIds(extra)
                    .growthBlockIds(growth)
                    .layout(layout)
                    .produceBlockId(produceId)
                    .waterlogOnPlant(waterlog)
                    .allowNonCropBlock(allowNonCrop)
                    .harvestHeight(harvestHeight)
                    .harvestMaxAge(harvestMaxAge)
                    .origin(CropOrigin.JSON)
                    .build());
        } catch (IllegalArgumentException exception) {
            NSUKDelightCropAddons.LOGGER.warn("Skipped crop {}: {}", id, exception.getMessage());
            return java.util.Optional.empty();
        }
    }

    private static List<ResourceLocation> idList(JsonObject json, String key, String modId) {
        List<ResourceLocation> ids = new ArrayList<>();
        if (!json.has(key) || !json.get(key).isJsonArray()) {
            return ids;
        }
        for (JsonElement element : json.getAsJsonArray(key)) {
            ResourceLocation id = CropIds.parse(element.getAsString(), modId);
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static String string(JsonObject json, String key, String fallback) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            return fallback;
        }
        String value = json.get(key).getAsString();
        return value == null || value.isBlank() ? fallback : value;
    }
}
