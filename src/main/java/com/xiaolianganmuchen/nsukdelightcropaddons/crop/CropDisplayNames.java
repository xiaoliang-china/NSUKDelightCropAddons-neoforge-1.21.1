package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import com.xiaolianganmuchen.nsukdelightcropaddons.config.CustomCropStorage;
import common.cn.kafei.simukraft.farmland.FarmCrop;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Locale;

/**
 * 农田盒作物显示名：优先用已翻译的种子名，避免作物方块只有键名（例如蔬菜乐事）。
 */
public final class CropDisplayNames {
    private CropDisplayNames() {
    }

    public static Component of(String cropId) {
        if (cropId == null || cropId.isBlank()) {
            return Component.translatable("gui.simukraft.farmland_box.none");
        }
        FarmCrop vanilla = FarmCrop.fromId(cropId);
        if (vanilla != null) {
            return Component.translatable(vanilla.translationKey());
        }
        return CropCatalog.resolve(cropId)
                .<Component>map(CropDisplayNames::of)
                .orElseGet(() -> fallback(cropId));
    }

    public static Component of(ResolvedCrop crop) {
        if (crop == null || crop.isMissing()) {
            return Component.translatable("gui.simukraft.farmland_box.none");
        }
        Component custom = CustomCropStorage.displayName(crop.id());
        if (custom != null) {
            return custom;
        }
        String ourKey = "crop.nsukdelightcropaddons." + crop.id();
        if (CropLangIndex.hasTranslation(ourKey)) {
            return Component.translatable(ourKey);
        }
        Item seed = crop.seed();
        if (seed != null && seed != Items.AIR && CropLangIndex.hasTranslation(seed.getDescriptionId())) {
            return seed.getName(new ItemStack(seed));
        }
        Block plant = crop.plantBlock();
        if (plant != null && plant != Blocks.AIR && CropLangIndex.hasTranslation(plant.getDescriptionId())) {
            return plant.getName();
        }
        String indexed = firstNonBlank(
                CropLangIndex.zh(seedKey(crop)),
                CropLangIndex.en(seedKey(crop)),
                CropLangIndex.zh(plantKey(crop)),
                CropLangIndex.en(plantKey(crop)),
                CropLangIndex.zh(ourKey),
                CropLangIndex.en(ourKey)
        );
        if (!indexed.isBlank()) {
            return Component.literal(indexed);
        }
        return Component.literal(humanize(crop));
    }

    public static Component source(String modId) {
        return Component.translatable(CropCatalog.sourceLabelKey(modId));
    }

    public static boolean matches(String query, FarmCrop crop) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String needle = query.toLowerCase(Locale.ROOT).trim();
        String key = crop.translationKey();
        return CropLangIndex.haystack(
                crop.id(),
                Component.translatable(key).getString(),
                CropLangIndex.en(key),
                CropLangIndex.zh(key),
                CropLangIndex.current(key)
        ).contains(needle);
    }

    public static boolean matches(String query, ResolvedCrop crop) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String needle = query.toLowerCase(Locale.ROOT).trim();
        String seedKey = seedKey(crop);
        String plantKey = plantKey(crop);
        String ourKey = "crop.nsukdelightcropaddons." + crop.id();
        String sourceKey = CropCatalog.sourceLabelKey(crop.sourceModId());
        return CropLangIndex.haystack(
                crop.id(),
                crop.sourceModId(),
                of(crop).getString(),
                seedKey,
                plantKey,
                crop.spec() == null ? "" : String.valueOf(crop.spec().seedId()),
                crop.spec() == null || crop.spec().plantBlockId() == null ? "" : crop.spec().plantBlockId().toString(),
                CropLangIndex.en(seedKey),
                CropLangIndex.zh(seedKey),
                CropLangIndex.current(seedKey),
                CropLangIndex.en(plantKey),
                CropLangIndex.zh(plantKey),
                CropLangIndex.current(plantKey),
                CropLangIndex.en(ourKey),
                CropLangIndex.zh(ourKey),
                CropLangIndex.en(sourceKey),
                CropLangIndex.zh(sourceKey),
                CustomCropStorage.searchText(crop.id())
        ).contains(needle);
    }

    private static Component fallback(String cropId) {
        String ourKey = "crop.nsukdelightcropaddons." + cropId;
        if (CropLangIndex.hasTranslation(ourKey)) {
            return Component.translatable(ourKey);
        }
        return CropCatalog.spec(cropId)
                .map(spec -> Component.literal(humanizePath(spec.seedId().getPath())))
                .orElse(Component.literal(cropId));
    }

    private static String seedKey(ResolvedCrop crop) {
        Item seed = crop.seed();
        return seed == null || seed == Items.AIR ? "" : seed.getDescriptionId();
    }

    private static String plantKey(ResolvedCrop crop) {
        Block plant = crop.plantBlock();
        return plant == null || plant == Blocks.AIR ? "" : plant.getDescriptionId();
    }

    private static String humanize(ResolvedCrop crop) {
        if (crop.spec() != null) {
            return humanizePath(crop.spec().seedId().getPath());
        }
        return crop.id();
    }

    private static String humanizePath(String path) {
        String key = CropIds.keyFromSeedPath(path).replace('_', ' ');
        if (key.isBlank()) {
            return path;
        }
        StringBuilder builder = new StringBuilder(key.length());
        boolean cap = true;
        for (char character : key.toCharArray()) {
            if (character == ' ') {
                builder.append(character);
                cap = true;
            } else if (cap) {
                builder.append(Character.toUpperCase(character));
                cap = false;
            } else {
                builder.append(character);
            }
        }
        return builder.toString();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
