package com.xiaolianganmuchen.nsukdelightcropaddons.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 按乐事来源和作物 id 开关。关闭后农田盒选择列表和农民作业都不会使用该作物。
 */
public final class DelightCropConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLE_FARMERS_DELIGHT = BUILDER
            .comment("Allow Farmer's Delight farmland crops in the NSUK farmland box.")
            .define("enableFarmersDelight", true);

    private static final ModConfigSpec.BooleanValue ENABLE_DUMPLINGS_DELIGHT = BUILDER
            .comment("Allow Dumplings Delight Rewrapped farmland crops in the NSUK farmland box.")
            .define("enableDumplingsDelight", true);

    private static final ModConfigSpec.BooleanValue ENABLE_PINEAPPLE_DELIGHT = BUILDER
            .comment("Allow Pineapple Delight farmland crops in the NSUK farmland box.")
            .define("enablePineappleDelight", true);

    private static final ModConfigSpec.BooleanValue ENABLE_UBES_DELIGHT = BUILDER
            .comment("Allow Ube's Delight farmland crops in the NSUK farmland box.")
            .define("enableUbesDelight", true);

    private static final ModConfigSpec.BooleanValue ENABLE_VEGGIES_DELIGHT = BUILDER
            .comment("Allow Veggies Delight farmland crops in the NSUK farmland box.")
            .define("enableVeggiesDelight", true);

    private static final ModConfigSpec.BooleanValue ENABLE_CORN_DELIGHT = BUILDER
            .comment("Allow Corn Delight farmland crops in the NSUK farmland box.")
            .define("enableCornDelight", true);

    private static final ModConfigSpec.BooleanValue ENABLE_EXPANDED_DELIGHT = BUILDER
            .comment("Allow Expanded Delight farmland crops in the NSUK farmland box.")
            .define("enableExpandedDelight", true);

    private static final ModConfigSpec.BooleanValue ENABLE_CULTURAL_DELIGHTS = BUILDER
            .comment("Allow Cultural Delights farmland crops in the NSUK farmland box.")
            .define("enableCulturalDelights", true);

    private static final ModConfigSpec.BooleanValue ENABLE_CROPTOPIA = BUILDER
            .comment("Allow Croptopia farmland crops in the NSUK farmland box.")
            .define("enableCroptopia", true);

    private static final ModConfigSpec.BooleanValue ENABLE_GAN_DELIGHT = BUILDER
            .comment("Allow Gan Delight Reborn farmland crops in the NSUK farmland box.")
            .define("enableGanDelight", true);

    private static final ModConfigSpec.BooleanValue ENABLE_TRAIL_AND_TALES_DELIGHT = BUILDER
            .comment("Allow Trail & Tales Delight farmland crops in the NSUK farmland box.")
            .define("enableTrailAndTalesDelight", true);

    private static final ModConfigSpec.BooleanValue ENABLE_IMMORTALERS_DELIGHT = BUILDER
            .comment("Allow Immortalers Delight farmland crops in the NSUK farmland box.")
            .define("enableImmortalersDelight", true);

    private static final ModConfigSpec.BooleanValue ENABLE_YOUKAIS_FEASTS = BUILDER
            .comment("Allow Youkais' Feasts farmland crops in the NSUK farmland box.")
            .define("enableYoukaisFeasts", true);

    private static final ModConfigSpec.BooleanValue ENABLE_RUSTIC_DELIGHT = BUILDER
            .comment("Allow Rustic Delight farmland crops in the NSUK farmland box.")
            .define("enableRusticDelight", true);

    private static final ModConfigSpec.BooleanValue AUTO_DISCOVERY = BUILDER
            .comment("Scan loaded delight mods for CropBlock seeds that were not listed in JSON.")
            .define("autoDiscovery", true);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED_CROPS = BUILDER
            .comment("Catalog ids to hide, for example fd_cabbage or dd_garlic.")
            .defineListAllowEmpty("disabledCrops", List.of(), () -> "", DelightCropConfig::isString);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private DelightCropConfig() {
    }

    public static boolean configReady() {
        try {
            return SPEC.isLoaded();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public static boolean autoDiscovery() {
        return flag(AUTO_DISCOVERY, true);
    }

    public static boolean isSourceEnabled(String modId) {
        if (modId == null || modId.isBlank()) {
            return false;
        }
        return switch (modId) {
            case "farmersdelight" -> flag(ENABLE_FARMERS_DELIGHT, true);
            case "mynethersdelight", "ends_delight" -> false;
            case "dumplings_delight" -> flag(ENABLE_DUMPLINGS_DELIGHT, true);
            case "pineapple_delight" -> flag(ENABLE_PINEAPPLE_DELIGHT, true);
            case "ubesdelight" -> flag(ENABLE_UBES_DELIGHT, true);
            case "veggiesdelight" -> flag(ENABLE_VEGGIES_DELIGHT, true);
            case "corn_delight" -> flag(ENABLE_CORN_DELIGHT, true);
            case "expandeddelight" -> flag(ENABLE_EXPANDED_DELIGHT, true);
            case "culturaldelights" -> flag(ENABLE_CULTURAL_DELIGHTS, true);
            case "croptopia" -> flag(ENABLE_CROPTOPIA, true);
            case "gan_delight_reborn" -> flag(ENABLE_GAN_DELIGHT, true);
            case "trailandtales_delight" -> flag(ENABLE_TRAIL_AND_TALES_DELIGHT, true);
            case "immortalers_delight" -> flag(ENABLE_IMMORTALERS_DELIGHT, true);
            case "youkaisfeasts" -> flag(ENABLE_YOUKAIS_FEASTS, true);
            case "rusticdelight" -> flag(ENABLE_RUSTIC_DELIGHT, true);
            case "custom" -> true;
            default -> true;
        };
    }

    public static List<SourceToggle> sourceToggles() {
        return List.of(
                new SourceToggle("farmersdelight", ENABLE_FARMERS_DELIGHT),
                new SourceToggle("dumplings_delight", ENABLE_DUMPLINGS_DELIGHT),
                new SourceToggle("pineapple_delight", ENABLE_PINEAPPLE_DELIGHT),
                new SourceToggle("ubesdelight", ENABLE_UBES_DELIGHT),
                new SourceToggle("veggiesdelight", ENABLE_VEGGIES_DELIGHT),
                new SourceToggle("corn_delight", ENABLE_CORN_DELIGHT),
                new SourceToggle("expandeddelight", ENABLE_EXPANDED_DELIGHT),
                new SourceToggle("culturaldelights", ENABLE_CULTURAL_DELIGHTS),
                new SourceToggle("croptopia", ENABLE_CROPTOPIA),
                new SourceToggle("gan_delight_reborn", ENABLE_GAN_DELIGHT),
                new SourceToggle("trailandtales_delight", ENABLE_TRAIL_AND_TALES_DELIGHT),
                new SourceToggle("immortalers_delight", ENABLE_IMMORTALERS_DELIGHT),
                new SourceToggle("youkaisfeasts", ENABLE_YOUKAIS_FEASTS),
                new SourceToggle("rusticdelight", ENABLE_RUSTIC_DELIGHT)
        );
    }

    public static void setAutoDiscovery(boolean value) {
        AUTO_DISCOVERY.set(value);
    }

    public record SourceToggle(String modId, ModConfigSpec.BooleanValue value) {
        public boolean enabled() {
            return flag(value, true);
        }

        public void setEnabled(boolean enabled) {
            value.set(enabled);
        }

        public String labelKey() {
            return "source.nsukdelightcropaddons." + modId;
        }
    }

    public static boolean isCropEnabled(String cropId) {
        if (cropId == null || cropId.isBlank()) {
            return false;
        }
        try {
            if (!configReady()) {
                return true;
            }
            Set<String> disabled = DISABLED_CROPS.get().stream()
                    .map(value -> value.toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());
            return !disabled.contains(cropId.toLowerCase(Locale.ROOT));
        } catch (RuntimeException ignored) {
            return true;
        }
    }

    private static boolean flag(ModConfigSpec.BooleanValue value, boolean fallback) {
        try {
            if (!configReady()) {
                return fallback;
            }
            return value.get();
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static boolean isString(Object value) {
        return value instanceof String;
    }
}
