package com.xiaolianganmuchen.nsukdelightcropaddons.mixin;

import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropContext;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropHarvest;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.ResolvedCrop;
import common.cn.kafei.simukraft.farmland.FarmCrop;
import common.cn.kafei.simukraft.farmland.FarmlandBoxData;
import common.cn.kafei.simukraft.farmland.FarmlandFarmingService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * 乐事作物收获与状态：两格玉米整株成熟再收、右键产物进箱、NPC 显示所选作物名。
 * 不引用 NSUK 包私有类型，避免编译失败；单独一个 Mixin，避免拖垮作业上下文。
 */
@Mixin(value = FarmlandFarmingService.class, remap = false)
public abstract class FarmlandHarvestServiceMixin {
    @Inject(method = "needsHarvestWork", at = @At("HEAD"), cancellable = true, require = 0)
    private static void nsukdelight$needsHarvestWork(
            ServerLevel level,
            FarmlandBoxData data,
            BlockPos cropPos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        CropContext.current().ifPresent(crop -> {
            if (crop.isStem()) {
                return;
            }
            cir.setReturnValue(CropHarvest.isReady(level, crop, cropPos));
        });
    }

    @Inject(method = "harvestBlock", at = @At("HEAD"), cancellable = true, require = 0)
    private static void nsukdelight$harvestBlock(
            ServerLevel level,
            List<BlockPos> chestPositions,
            BlockPos pos,
            BlockState state,
            CallbackInfo ci
    ) {
        CropContext.current().ifPresent(crop -> {
            if (crop.isStem() || !crop.isOwnPlant(state)) {
                return;
            }
            CropHarvest.harvest(level, crop, chestPositions, pos);
            ci.cancel();
        });
    }

    @Inject(method = "needsBonemealWork", at = @At("HEAD"), cancellable = true, require = 0)
    private static void nsukdelight$needsBonemealWork(
            ServerLevel level,
            FarmlandBoxData data,
            List<BlockPos> chestPositions,
            BlockPos cropPos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        CropContext.current().ifPresent(crop -> {
            if (!crop.isStem() && CropHarvest.isReady(level, crop, cropPos)) {
                cir.setReturnValue(false);
            }
        });
    }

    @Inject(method = "farmerStatusLabel", at = @At("HEAD"), cancellable = true, require = 0)
    private static void nsukdelight$farmerStatusLabel(
            ServerLevel level,
            String translationKey,
            FarmCrop crop,
            CallbackInfoReturnable<String> cir
    ) {
        CropContext.current().ifPresent(resolved -> cir.setReturnValue(
                Component.Serializer.toJson(Component.translatable(translationKey, statusName(resolved)), level.registryAccess())
        ));
    }

    private static Component statusName(ResolvedCrop crop) {
        return Component.translatableWithFallback(crop.statusTranslationKey(), Component.translatable(crop.seedTranslationKey()).getString());
    }
}
