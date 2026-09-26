package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import com.xiaolianganmuchen.nsukdelightcropaddons.NSUKDelightCropAddons;
import com.xiaolianganmuchen.nsukdelightcropaddons.config.DelightCropConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 乐事作物目录。按 id 去重：先注册的条目优先，自动发现不会覆盖 JSON / API。
 */
public final class CropCatalog {
    private static final Map<String, CropSpec> SPECS = new ConcurrentHashMap<>();
    private static final Map<String, String> SOURCE_LABELS = new ConcurrentHashMap<>();

    private CropCatalog() {
    }

    public static void registerSourceLabel(String modId, String translationKey) {
        if (modId == null || modId.isBlank() || translationKey == null || translationKey.isBlank()) {
            return;
        }
        SOURCE_LABELS.put(modId, translationKey);
    }

    public static boolean register(CropSpec spec) {
        if (spec == null) {
            return false;
        }
        CropSpec existing = SPECS.get(spec.id());
        if (existing != null && !(existing.origin() == CropOrigin.DISCOVERY && spec.origin() != CropOrigin.DISCOVERY)) {
            return false;
        }
        SPECS.put(spec.id(), spec);
        NSUKDelightCropAddons.LOGGER.debug("Registered delight crop {} -> {}", spec.id(), spec.seedId());
        return true;
    }

    public static void replaceOrigin(CropOrigin origin, Collection<CropSpec> next) {
        SPECS.entrySet().removeIf(entry -> entry.getValue().origin() == origin);
        if (next == null) {
            return;
        }
        for (CropSpec spec : next) {
            register(spec);
        }
    }

    public static void clear() {
        SPECS.clear();
        SOURCE_LABELS.clear();
    }

    public static boolean contains(String id) {
        return id != null && SPECS.containsKey(id);
    }

    public static boolean isPopulated() {
        return !SPECS.isEmpty();
    }

    public static Optional<CropSpec> spec(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(SPECS.get(id));
    }

    public static Optional<ResolvedCrop> resolve(String id) {
        return spec(id).flatMap(ResolvedCrop::resolve).filter(CropCatalog::isEnabled);
    }

    public static boolean isSelectable(String id) {
        return resolve(id).isPresent();
    }

    public static List<ResolvedCrop> selectable() {
        List<ResolvedCrop> crops = new ArrayList<>();
        for (CropSpec spec : SPECS.values()) {
            ResolvedCrop.resolve(spec).filter(CropCatalog::isEnabled).ifPresent(crops::add);
        }
        crops.sort(Comparator
                .comparingInt((ResolvedCrop crop) -> "custom".equals(crop.sourceModId()) ? 1 : 0)
                .thenComparing(ResolvedCrop::sourceModId)
                .thenComparing(ResolvedCrop::id));
        return List.copyOf(crops);
    }

    public static Map<String, List<ResolvedCrop>> selectableBySource() {
        Map<String, List<ResolvedCrop>> grouped = new LinkedHashMap<>();
        for (ResolvedCrop crop : selectable()) {
            grouped.computeIfAbsent(crop.sourceModId(), ignored -> new ArrayList<>()).add(crop);
        }
        return grouped;
    }

    public static String sourceLabelKey(String modId) {
        if (modId == null || modId.isBlank()) {
            return "source.nsukdelightcropaddons.unknown";
        }
        return SOURCE_LABELS.getOrDefault(modId, "source.nsukdelightcropaddons." + modId);
    }

    public static boolean isSeedRegistered(ResourceLocation seedId) {
        if (seedId == null) {
            return false;
        }
        for (CropSpec spec : SPECS.values()) {
            if (seedId.equals(spec.seedId())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isModLoaded(String modId) {
        return modId != null && !modId.isBlank() && ModList.get().isLoaded(modId);
    }

    public static Item seedItemOrAir(ResourceLocation seedId) {
        return BuiltInRegistries.ITEM.get(seedId);
    }

    private static boolean isEnabled(ResolvedCrop crop) {
        return DelightCropConfig.isSourceEnabled(crop.sourceModId())
                && DelightCropConfig.isCropEnabled(crop.id());
    }
}
