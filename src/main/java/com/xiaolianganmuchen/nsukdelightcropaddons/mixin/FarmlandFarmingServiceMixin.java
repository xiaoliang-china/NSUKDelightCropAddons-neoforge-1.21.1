package com.xiaolianganmuchen.nsukdelightcropaddons.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropContext;
import com.xiaolianganmuchen.nsukdelightcropaddons.nsuk.FarmlandBoxCleanup;
import com.xiaolianganmuchen.nsukdelightcropaddons.nsuk.FarmlandBoxReconcile;
import common.cn.kafei.simukraft.farmland.FarmlandBoxData;
import common.cn.kafei.simukraft.farmland.FarmlandBoxManager;
import common.cn.kafei.simukraft.farmland.FarmlandFarmingService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Coerce;

/**
 * 只挂钩农民作业循环的上下文。沃土翻地等额外注入会在签名不匹配时直接让整个 Mixin 失败，
 * 因此不放在这个类里。
 * 用 WrapMethod + finally，避免 tickBox 中途抛错时上下文没弹出。
 */
@Mixin(value = FarmlandFarmingService.class, remap = false)
public abstract class FarmlandFarmingServiceMixin {
    @WrapMethod(method = "tickBox")
    private static void nsukdelight$withCrop(
            ServerLevel level,
            FarmlandBoxManager manager,
            FarmlandBoxData data,
            @Coerce Object boxRuntime,
            long gameTime,
            Operation<Void> original
    ) {
        if (level != null && data != null && level.isLoaded(data.boxPos())) {
            Block boxBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.parse("simukraft:nsuk_farmland_box"));
            if (boxBlock != Blocks.AIR && !level.getBlockState(data.boxPos()).is(boxBlock)) {
                FarmlandBoxCleanup.purge(level, data.boxPos());
                return;
            }
        }
        CropContext.push(data);
        try {
            original.call(level, manager, data, boxRuntime, gameTime);
        } finally {
            CropContext.pop();
        }
    }

    @WrapMethod(method = "tick")
    private static void nsukdelight$clearContext(ServerLevel level, Operation<Void> original) {
        try {
            FarmlandBoxReconcile.reconcileAll(level);
            original.call(level);
        } finally {
            CropContext.clear();
        }
    }
}
