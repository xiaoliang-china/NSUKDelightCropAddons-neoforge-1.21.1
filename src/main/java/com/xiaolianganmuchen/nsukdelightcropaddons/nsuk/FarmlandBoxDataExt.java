package com.xiaolianganmuchen.nsukdelightcropaddons.nsuk;

/**
 * Mixin 注入到 NSUK {@code FarmlandBoxData} 上的附加作物 id。
 * 原版枚举存不下乐事作物，所以额外字段保存目录 id，NBT 的 {@code Crop} 字符串仍走同一键。
 */
public interface FarmlandBoxDataExt {
    String nsukdelight$getAddonCropId();

    void nsukdelight$setAddonCropId(String id);
}
