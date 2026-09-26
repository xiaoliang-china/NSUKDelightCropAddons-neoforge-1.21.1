package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import java.util.Locale;

/**
 * 种植布局。与 NSUK {@code FarmCrop.Layout} 对齐：满铺或隔格给藤蔓留结果空间。
 */
public enum CropLayout {
    FULL,
    STEM;

    public static CropLayout fromName(String name) {
        if (name == null || name.isBlank()) {
            return FULL;
        }
        try {
            return valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return FULL;
        }
    }
}
