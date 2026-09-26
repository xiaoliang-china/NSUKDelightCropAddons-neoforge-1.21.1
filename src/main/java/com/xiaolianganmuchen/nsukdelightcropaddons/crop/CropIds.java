package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

/**
 * 农田盒网络包把作物 id 限制在 32 字符，因此目录 id 使用短前缀而不是完整注册名。
 */
public final class CropIds {
    private CropIds() {
    }

    public static String compose(String prefix, String key) {
        String safePrefix = sanitize(prefix);
        String safeKey = sanitize(key);
        String id = safePrefix + "_" + safeKey;
        if (id.length() <= CropSpec.MAX_ID_LENGTH) {
            return id;
        }
        return id.substring(0, CropSpec.MAX_ID_LENGTH);
    }

    public static String keyFromSeedPath(String path) {
        if (path == null || path.isBlank()) {
            return "crop";
        }
        String key = path.toLowerCase(Locale.ROOT);
        if (key.endsWith("_seeds")) {
            key = key.substring(0, key.length() - "_seeds".length());
        } else if (key.endsWith("_seed")) {
            key = key.substring(0, key.length() - "_seed".length());
        }
        return sanitize(key);
    }

    public static ResourceLocation parse(String value, String fallbackNamespace) {
        if (value == null || value.isBlank()) {
            return null;
        }
        ResourceLocation parsed = ResourceLocation.tryParse(value);
        if (parsed != null) {
            return parsed;
        }
        return ResourceLocation.tryBuild(fallbackNamespace, value);
    }

    private static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "x";
        }
        StringBuilder builder = new StringBuilder(value.length());
        for (char character : value.toLowerCase(Locale.ROOT).toCharArray()) {
            if (character >= 'a' && character <= 'z' || character >= '0' && character <= '9') {
                builder.append(character);
            } else {
                builder.append('_');
            }
        }
        String sanitized = builder.toString().replaceAll("_+", "_");
        if (sanitized.startsWith("_")) {
            sanitized = sanitized.substring(1);
        }
        if (sanitized.endsWith("_")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }
        return sanitized.isEmpty() ? "x" : sanitized;
    }
}
