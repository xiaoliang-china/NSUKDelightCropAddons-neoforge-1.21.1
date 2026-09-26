package com.xiaolianganmuchen.nsukdelightcropaddons.nsuk;

import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropCatalog;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropRules;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropSpec;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.ResolvedCrop;
import common.cn.kafei.simukraft.farmland.FarmCrop;
import common.cn.kafei.simukraft.farmland.FarmlandBoxData;
import common.cn.kafei.simukraft.farmland.FarmlandBoxManager;
import common.cn.kafei.simukraft.farmland.FarmlandPlot;
import common.cn.kafei.simukraft.util.SaveScopedCacheKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 每个农田盒各自核对关档作物和田里植株。
 * 对得上就保留；对不上才清空。拆掉的盒子会删掉该坐标全部存档。
 */
public final class FarmlandBoxReconcile {
    private static final ResourceLocation BOX_ID = ResourceLocation.parse("simukraft:nsuk_farmland_box");
    private static final Set<String> SETTLED = ConcurrentHashMap.newKeySet();

    private FarmlandBoxReconcile() {
    }

    public static void clear() {
        SETTLED.clear();
    }

    public static void markSettled(ServerLevel level, BlockPos boxPos) {
        if (level == null || boxPos == null) {
            return;
        }
        SETTLED.add(key(level, boxPos));
    }

    public static void forget(ServerLevel level, BlockPos boxPos) {
        if (level == null || boxPos == null) {
            return;
        }
        SETTLED.remove(key(level, boxPos));
    }

    public static void reconcileAll(ServerLevel level) {
        if (level == null || level.isClientSide()) {
            return;
        }
        FarmlandBoxManager manager = FarmlandBoxManager.get(level);
        for (FarmlandBoxData data : manager.all()) {
            reconcileIfNeeded(level, manager, data);
        }
    }

    public static void reconcileIfNeeded(ServerLevel level, FarmlandBoxManager manager, FarmlandBoxData data) {
        if (level == null || manager == null || data == null) {
            return;
        }
        String key = key(level, data.boxPos());
        if (SETTLED.contains(key)) {
            return;
        }
        if (!level.isLoaded(data.boxPos())) {
            return;
        }
        Block boxBlock = BuiltInRegistries.BLOCK.get(BOX_ID);
        if (boxBlock != Blocks.AIR && !level.getBlockState(data.boxPos()).is(boxBlock)) {
            FarmlandBoxCleanup.purge(level, data.boxPos());
            return;
        }
        if (!CropCatalog.isPopulated()) {
            return;
        }
        String selectedId = FarmlandBoxDataAccess.selectedCropId(data);
        if (!selectedId.isBlank() && FarmCrop.fromId(selectedId) == null && !catalogReadyFor(selectedId)) {
            return;
        }
        FarmlandPlot plot = data.plot();
        if (plot == null) {
            SETTLED.add(key);
            return;
        }
        List<ResolvedCrop> knownCrops = CropCatalog.selectable();
        Scan scan = scanPlot(level, data, plot, selectedId, knownCrops);
        if (!scan.ready) {
            return;
        }
        CropRules.BoxCropAction action = CropRules.decideBoxCrop(
                !selectedId.isBlank(),
                !scan.fieldCropIds.isEmpty()
        );
        SETTLED.add(key);
        if (action == CropRules.BoxCropAction.CLEAR) {
            FarmlandBoxDataAccess.clearSelection(data);
            FarmlandBoxCleanup.clearFarmerFarmStatus(level, data.boxPos());
            manager.persist(data);
            return;
        }
        if (!selectedId.isBlank()) {
            manager.persist(data);
        }
    }

    private static boolean catalogReadyFor(String cropId) {
        Optional<CropSpec> spec = CropCatalog.spec(cropId);
        if (spec.isEmpty()) {
            return true;
        }
        return ResolvedCrop.resolve(spec.get()).isPresent();
    }

    private static Scan scanPlot(
            ServerLevel level,
            FarmlandBoxData data,
            FarmlandPlot plot,
            String selectedId,
            List<ResolvedCrop> knownCrops
    ) {
        int unloaded = 0;
        Set<String> otherIds = new LinkedHashSet<>();
        int cells = Math.max(1, plot.cellCount());
        BlockPos boxPos = data.boxPos();
        for (int index = 0; index < cells; index++) {
            BlockPos cropPos = plot.cellAt(index);
            if (cropPos.getX() == boxPos.getX() && cropPos.getZ() == boxPos.getZ()) {
                continue;
            }
            if (!level.isLoaded(cropPos)) {
                unloaded++;
                continue;
            }
            BlockState state = level.getBlockState(cropPos);
            if (state.isAir() || state.canBeReplaced() || state.is(Blocks.WATER) || state.is(Blocks.FARMLAND)) {
                continue;
            }
            String foundId = identifyCropId(state, knownCrops);
            if (foundId.isBlank() || foundId.equals(selectedId)) {
                continue;
            }
            otherIds.add(foundId);
        }
        return new Scan(unloaded == 0 || !otherIds.isEmpty(), otherIds);
    }

    private static String identifyCropId(BlockState state, List<ResolvedCrop> knownCrops) {
        for (ResolvedCrop crop : knownCrops) {
            if (crop.isOwnPlant(state)) {
                return crop.id();
            }
        }
        for (FarmCrop vanilla : FarmCrop.values()) {
            if (vanilla.isOwnPlant(state) || vanilla.isProduce(state)) {
                return vanilla.id();
            }
        }
        return "";
    }

    private static String key(ServerLevel level, BlockPos boxPos) {
        return SaveScopedCacheKey.levelKey(level).toLowerCase(Locale.ROOT) + "|" + boxPos.asLong();
    }

    private record Scan(boolean ready, Set<String> fieldCropIds) {
    }
}
