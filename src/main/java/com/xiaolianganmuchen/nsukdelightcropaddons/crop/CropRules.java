package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

/**
 * 不依赖 Minecraft 类型的作物规则，方便单测。
 */
public final class CropRules {
    private CropRules() {
    }

    public static boolean isAgePropertyName(String name) {
        return name != null && ("age".equals(name) || name.endsWith("_age"));
    }

    public static boolean columnIsComplete(int requiredHeight, int ownPlantCount, boolean allSegmentsMature) {
        if (requiredHeight > 1) {
            return ownPlantCount >= requiredHeight && allSegmentsMature;
        }
        return ownPlantCount >= 1 && allSegmentsMature;
    }

    /**
     * 额外收获方块（巨型凤梨、番茄藤）可收。
     * 带年龄的植株默认最大年龄也可收（凤梨芽）；番茄这类要把 harvestMaxAgePlant 关掉，只收藤。
     */
    public static boolean isHarvestableStage(
            int harvestHeight,
            boolean hasExtraHarvestBlocks,
            boolean isExtraHarvestBlock,
            boolean isUpperSegment,
            boolean hasAge,
            boolean maxAge,
            boolean harvestMaxAgePlant
    ) {
        if (harvestHeight > 1) {
            if (hasAge) {
                return maxAge;
            }
            return isUpperSegment || isExtraHarvestBlock;
        }
        if (isExtraHarvestBlock) {
            return !hasAge || maxAge;
        }
        if (hasExtraHarvestBlocks && !harvestMaxAgePlant) {
            return false;
        }
        return hasAge && maxAge;
    }

    public enum BoxCropAction {
        KEEP,
        CLEAR
    }

    /**
     * 读档后每个农田盒单独决定：对得上就保留；田里是别的作物才清空。
     * 盒子没有选定作物时保持「无」，不从田里自动填回。
     */
    public static BoxCropAction decideBoxCrop(boolean hasSelection, boolean sawForeignCrop) {
        if (hasSelection && sawForeignCrop) {
            return BoxCropAction.CLEAR;
        }
        return BoxCropAction.KEEP;
    }
}
