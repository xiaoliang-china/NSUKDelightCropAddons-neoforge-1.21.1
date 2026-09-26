package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

/**
 * 未解析的作物定义。只保存注册名，物品/方块在游戏注册表就绪后再解析。
 * 新增乐事模组时优先写 JSON；特殊行为（额外收获方块、水logged、藤蔓）也在这里表达。
 */
public record CropSpec(
        String id,
        String sourceModId,
        String sourcePrefix,
        ResourceLocation seedId,
        ResourceLocation plantBlockId,
        List<ResourceLocation> extraHarvestBlockIds,
        List<ResourceLocation> growthBlockIds,
        CropLayout layout,
        ResourceLocation produceBlockId,
        boolean waterlogOnPlant,
        boolean allowNonCropBlock,
        int harvestHeight,
        boolean harvestMaxAge,
        CropOrigin origin
) {
    public static final int MAX_ID_LENGTH = 32;
    public static final int MAX_HARVEST_HEIGHT = 8;

    public CropSpec {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceModId, "sourceModId");
        Objects.requireNonNull(sourcePrefix, "sourcePrefix");
        Objects.requireNonNull(seedId, "seedId");
        extraHarvestBlockIds = extraHarvestBlockIds == null ? List.of() : List.copyOf(extraHarvestBlockIds);
        growthBlockIds = growthBlockIds == null ? List.of() : List.copyOf(growthBlockIds);
        layout = layout == null ? CropLayout.FULL : layout;
        origin = origin == null ? CropOrigin.API : origin;
        harvestHeight = Math.max(0, Math.min(MAX_HARVEST_HEIGHT, harvestHeight));
        if (id.length() > MAX_ID_LENGTH) {
            throw new IllegalArgumentException("Crop id '" + id + "' exceeds NSUK farmland packet limit of " + MAX_ID_LENGTH);
        }
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final String id;
        private String sourceModId = "";
        private String sourcePrefix = "";
        private ResourceLocation seedId;
        private ResourceLocation plantBlockId;
        private List<ResourceLocation> extraHarvestBlockIds = List.of();
        private List<ResourceLocation> growthBlockIds = List.of();
        private CropLayout layout = CropLayout.FULL;
        private ResourceLocation produceBlockId;
        private boolean waterlogOnPlant;
        private boolean allowNonCropBlock;
        private int harvestHeight;
        private boolean harvestMaxAge = true;
        private CropOrigin origin = CropOrigin.API;

        private Builder(String id) {
            this.id = id;
        }

        public Builder sourceModId(String sourceModId) {
            this.sourceModId = sourceModId;
            return this;
        }

        public Builder sourcePrefix(String sourcePrefix) {
            this.sourcePrefix = sourcePrefix;
            return this;
        }

        public Builder seedId(ResourceLocation seedId) {
            this.seedId = seedId;
            return this;
        }

        public Builder plantBlockId(ResourceLocation plantBlockId) {
            this.plantBlockId = plantBlockId;
            return this;
        }

        public Builder extraHarvestBlockIds(List<ResourceLocation> extraHarvestBlockIds) {
            this.extraHarvestBlockIds = extraHarvestBlockIds;
            return this;
        }

        public Builder growthBlockIds(List<ResourceLocation> growthBlockIds) {
            this.growthBlockIds = growthBlockIds;
            return this;
        }

        public Builder layout(CropLayout layout) {
            this.layout = layout;
            return this;
        }

        public Builder produceBlockId(ResourceLocation produceBlockId) {
            this.produceBlockId = produceBlockId;
            return this;
        }

        public Builder waterlogOnPlant(boolean waterlogOnPlant) {
            this.waterlogOnPlant = waterlogOnPlant;
            return this;
        }

        public Builder allowNonCropBlock(boolean allowNonCropBlock) {
            this.allowNonCropBlock = allowNonCropBlock;
            return this;
        }

        public Builder harvestHeight(int harvestHeight) {
            this.harvestHeight = harvestHeight;
            return this;
        }

        public Builder harvestMaxAge(boolean harvestMaxAge) {
            this.harvestMaxAge = harvestMaxAge;
            return this;
        }

        public Builder origin(CropOrigin origin) {
            this.origin = origin;
            return this;
        }

        public CropSpec build() {
            return new CropSpec(
                    id,
                    sourceModId,
                    sourcePrefix,
                    seedId,
                    plantBlockId,
                    extraHarvestBlockIds,
                    growthBlockIds,
                    layout,
                    produceBlockId,
                    waterlogOnPlant,
                    allowNonCropBlock,
                    harvestHeight,
                    harvestMaxAge,
                    origin
            );
        }
    }
}
