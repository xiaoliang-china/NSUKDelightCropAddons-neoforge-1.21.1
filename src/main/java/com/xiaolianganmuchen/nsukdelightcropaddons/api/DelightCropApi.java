package com.xiaolianganmuchen.nsukdelightcropaddons.api;

import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropCatalog;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropSpec;

/**
 * 给其它附属模组用的注册入口。在 {@code FMLCommonSetupEvent} 或更早调用即可。
 *
 * <pre>{@code
 * DelightCropApi.register(CropSpec.builder("cd_corn")
 *     .sourceModId("corn_delight")
 *     .sourcePrefix("cd")
 *     .seedId(ResourceLocation.parse("corn_delight:corn_seeds"))
 *     .build());
 * }</pre>
 */
public final class DelightCropApi {
    private DelightCropApi() {
    }

    public static boolean register(CropSpec spec) {
        return CropCatalog.register(spec);
    }

    public static void registerSourceLabel(String modId, String translationKey) {
        CropCatalog.registerSourceLabel(modId, translationKey);
    }
}
