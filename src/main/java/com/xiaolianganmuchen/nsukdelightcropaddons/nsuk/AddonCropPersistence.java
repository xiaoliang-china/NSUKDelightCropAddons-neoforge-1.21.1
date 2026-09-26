package com.xiaolianganmuchen.nsukdelightcropaddons.nsuk;

import java.util.function.Predicate;

/**
 * 农田盒作物 id 存档：乐事作物写在 {@code Crop} 和备份键 {@code AddonCrop} 里。
 * 读档时只要不是原版枚举 id，就原样保留，不依赖此时作物目录是否已经加载。
 */
public final class AddonCropPersistence {
    public static final String ADDON_CROP_TAG = "AddonCrop";

    private AddonCropPersistence() {
    }

    public static String restoreId(String crop, String addonCrop, Predicate<String> vanillaCrop) {
        String id = firstNonBlank(addonCrop, crop);
        if (id == null || id.isBlank() || (vanillaCrop != null && vanillaCrop.test(id))) {
            return "";
        }
        return id;
    }

    public static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return "";
    }
}
