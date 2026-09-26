package com.xiaolianganmuchen.nsukdelightcropaddons.mixin;

import com.xiaolianganmuchen.nsukdelightcropaddons.nsuk.AddonCropPersistence;
import com.xiaolianganmuchen.nsukdelightcropaddons.nsuk.FarmlandBoxDataExt;
import common.cn.kafei.simukraft.farmland.FarmCrop;
import common.cn.kafei.simukraft.farmland.FarmlandBoxData;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FarmlandBoxData.class, remap = false)
public abstract class FarmlandBoxDataMixin implements FarmlandBoxDataExt {
    @Unique
    private String nsukdelight$addonCropId = "";

    @Override
    public String nsukdelight$getAddonCropId() {
        return nsukdelight$addonCropId == null ? "" : nsukdelight$addonCropId;
    }

    @Override
    public void nsukdelight$setAddonCropId(String id) {
        this.nsukdelight$addonCropId = id == null ? "" : id;
    }

    @Inject(method = "toTag", at = @At("RETURN"))
    private void nsukdelight$writeAddonCrop(CallbackInfoReturnable<CompoundTag> cir) {
        String addonId = nsukdelight$getAddonCropId();
        if (addonId.isBlank()) {
            return;
        }
        CompoundTag tag = cir.getReturnValue();
        tag.putString("Crop", addonId);
        tag.putString(AddonCropPersistence.ADDON_CROP_TAG, addonId);
    }

    @Inject(method = "fromTag", at = @At("RETURN"))
    private static void nsukdelight$readAddonCrop(CompoundTag tag, CallbackInfoReturnable<FarmlandBoxData> cir) {
        FarmlandBoxData data = cir.getReturnValue();
        if (data == null || tag == null) {
            return;
        }
        String cropId = AddonCropPersistence.restoreId(
                tag.contains("Crop") ? tag.getString("Crop") : "",
                tag.contains(AddonCropPersistence.ADDON_CROP_TAG) ? tag.getString(AddonCropPersistence.ADDON_CROP_TAG) : "",
                id -> FarmCrop.fromId(id) != null
        );
        if (cropId.isBlank()) {
            return;
        }
        data.setCrop(FarmCrop.WHEAT);
        ((FarmlandBoxDataExt) (Object) data).nsukdelight$setAddonCropId(cropId);
    }
}
