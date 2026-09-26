package com.xiaolianganmuchen.nsukdelightcropaddons.nsuk;

import common.cn.kafei.simukraft.citizen.CitizenData;
import common.cn.kafei.simukraft.citizen.CitizenService;
import common.cn.kafei.simukraft.citizen.CitizenWorkStatus;
import common.cn.kafei.simukraft.farmland.FarmlandBoxData;
import common.cn.kafei.simukraft.farmland.FarmlandBoxManager;
import common.cn.kafei.simukraft.farmland.FarmlandBoxService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * 农田盒被拆掉后，该坐标上的作物、范围、耕作状态一律删除，避免下次再放盒子读到旧任务。
 */
public final class FarmlandBoxCleanup {
    private FarmlandBoxCleanup() {
    }

    public static void purge(ServerLevel level, BlockPos boxPos) {
        if (level == null || boxPos == null) {
            return;
        }
        FarmlandBoxManager manager = FarmlandBoxManager.get(level);
        FarmlandBoxData data = manager.get(boxPos);
        if (data != null) {
            FarmlandBoxDataAccess.clearSelection(data);
            data.setPlot(null);
            data.setRunning(false);
        }
        clearFarmerFarmStatus(level, boxPos);
        manager.remove(boxPos);
        FarmlandBoxReconcile.forget(level, boxPos);
    }

    public static void clearFarmerFarmStatus(ServerLevel level, BlockPos boxPos) {
        CitizenData farmer = FarmlandBoxService.findAssignedWorker(level, boxPos);
        if (farmer == null) {
            return;
        }
        farmer.setWorkStatus(CitizenWorkStatus.IDLE);
        farmer.setStatusLabel("");
        farmer.setWorkNeedDetail("");
        CitizenService.save(level, farmer.uuid());
    }
}
