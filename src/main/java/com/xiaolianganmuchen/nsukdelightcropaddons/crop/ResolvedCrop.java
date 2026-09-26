package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 已从注册表解析的作物。农民耕种时通过 {@link CropContext} 把这些行为接到 NSUK 原版 {@code FarmCrop} 方法上。
 */
public final class ResolvedCrop {
    private static final ResolvedCrop MISSING = new ResolvedCrop(null, Items.AIR, Blocks.AIR, List.of(), List.of(), null);

    private final CropSpec spec;
    private final Item seed;
    private final Block plantBlock;
    private final List<Block> extraHarvestBlocks;
    private final List<Block> growthBlocks;
    private final Block produceBlock;

    private ResolvedCrop(
            CropSpec spec,
            Item seed,
            Block plantBlock,
            List<Block> extraHarvestBlocks,
            List<Block> growthBlocks,
            Block produceBlock
    ) {
        this.spec = spec;
        this.seed = seed;
        this.plantBlock = plantBlock;
        this.extraHarvestBlocks = extraHarvestBlocks;
        this.growthBlocks = growthBlocks;
        this.produceBlock = produceBlock;
    }

    public static ResolvedCrop missing() {
        return MISSING;
    }

    public boolean isMissing() {
        return this == MISSING || spec == null;
    }

    public static Optional<ResolvedCrop> resolve(CropSpec spec) {
        if (spec == null) {
            return Optional.empty();
        }
        Item seed = BuiltInRegistries.ITEM.get(spec.seedId());
        if (seed == null || seed == Items.AIR) {
            return Optional.empty();
        }
        Block plantBlock = lookupBlock(spec.plantBlockId());
        if (plantBlock == Blocks.AIR && seed instanceof BlockItem blockItem) {
            plantBlock = blockItem.getBlock();
        }
        if (plantBlock == Blocks.AIR) {
            return Optional.empty();
        }
        if (!(plantBlock instanceof CropBlock) && !spec.allowNonCropBlock()) {
            return Optional.empty();
        }
        List<Block> extraHarvestBlocks = lookupDistinctBlocks(spec.extraHarvestBlockIds(), plantBlock);
        List<Block> growthBlocks = lookupDistinctBlocks(spec.growthBlockIds(), plantBlock);
        growthBlocks.removeAll(extraHarvestBlocks);
        Block produceBlock = lookupBlock(spec.produceBlockId());
        if (produceBlock == Blocks.AIR) {
            produceBlock = null;
        }
        return Optional.of(new ResolvedCrop(
                spec,
                seed,
                plantBlock,
                List.copyOf(extraHarvestBlocks),
                List.copyOf(growthBlocks),
                produceBlock
        ));
    }

    public CropSpec spec() {
        return spec;
    }

    public String id() {
        return spec == null ? "" : spec.id();
    }

    public String sourceModId() {
        return spec == null ? "" : spec.sourceModId();
    }

    public Item seed() {
        return seed;
    }

    public Block plantBlock() {
        return plantBlock;
    }

    public Block produceBlock() {
        return produceBlock;
    }

    public BlockState plantState() {
        if (isMissing()) {
            return Blocks.AIR.defaultBlockState();
        }
        BlockState state = plantBlock.defaultBlockState();
        if (spec.waterlogOnPlant() && state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, true);
        }
        return state;
    }

    public boolean isStem() {
        return spec != null && spec.layout() == CropLayout.STEM;
    }

    public boolean shouldPlantAt(int x, int z) {
        if (isMissing() || spec == null) {
            return false;
        }
        return spec.layout() == CropLayout.FULL || ((Math.floorMod(x, 2) + Math.floorMod(z, 2)) % 2 == 0);
    }

    public boolean isOwnPlant(BlockState state) {
        if (state == null) {
            return false;
        }
        if (state.is(plantBlock)) {
            return true;
        }
        for (Block extra : extraHarvestBlocks) {
            if (state.is(extra)) {
                return true;
            }
        }
        for (Block growth : growthBlocks) {
            if (state.is(growth)) {
                return true;
            }
        }
        return false;
    }

    public int harvestHeight() {
        if (spec != null && spec.harvestHeight() > 1) {
            return spec.harvestHeight();
        }
        if (plantBlock == null || plantBlock == Blocks.AIR) {
            return 1;
        }
        return looksTall(plantBlock.defaultBlockState()) ? 2 : 1;
    }

    public boolean isUpperSegment(BlockState state) {
        if (state == null) {
            return false;
        }
        if (booleanProperty(state, "upper")) {
            return true;
        }
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
            return true;
        }
        for (Block extra : extraHarvestBlocks) {
            if (state.is(extra)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasExtraHarvestBlocks() {
        return !extraHarvestBlocks.isEmpty();
    }

    public boolean isExtraHarvestBlock(BlockState state) {
        if (state == null) {
            return false;
        }
        for (Block extra : extraHarvestBlocks) {
            if (state.is(extra)) {
                return true;
            }
        }
        return false;
    }

    public boolean isMatureFull(BlockState state) {
        if (state == null || !isOwnPlant(state) || isStem()) {
            return false;
        }
        if (harvestHeight() > 1 && !isUpperSegment(state)) {
            return false;
        }
        return isSegmentMature(state);
    }

    public boolean isSegmentMature(BlockState state) {
        if (state == null || !isOwnPlant(state)) {
            return false;
        }
        return CropRules.isHarvestableStage(
                harvestHeight(),
                !extraHarvestBlocks.isEmpty(),
                isExtraHarvestBlock(state),
                isUpperSegment(state),
                CropAge.hasAge(state),
                CropAge.isMaxAge(state),
                spec == null || spec.harvestMaxAge()
        );
    }

    public String statusTranslationKey() {
        return "crop.nsukdelightcropaddons." + id();
    }

    public String seedTranslationKey() {
        return seed == null || seed == Items.AIR ? statusTranslationKey() : seed.getDescriptionId();
    }

    public boolean isProduce(BlockState state) {
        return produceBlock != null && state != null && state.is(produceBlock);
    }

    public String translationKey() {
        if (isMissing()) {
            return "gui.simukraft.farmland_box.none";
        }
        if (seed != null && seed != Items.AIR) {
            return seed.getDescriptionId();
        }
        if (plantBlock != null && plantBlock != Blocks.AIR) {
            return plantBlock.getDescriptionId();
        }
        return "gui.simukraft.farmland_box.none";
    }

    private static boolean looksTall(BlockState state) {
        if (state == null) {
            return false;
        }
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return true;
        }
        return hasBooleanProperty(state, "upper");
    }

    private static boolean booleanProperty(BlockState state, String name) {
        Property<?> property = findProperty(state, name);
        if (!(property instanceof net.minecraft.world.level.block.state.properties.BooleanProperty booleanProperty)
                || !state.hasProperty(booleanProperty)) {
            return false;
        }
        return state.getValue(booleanProperty);
    }

    private static boolean hasBooleanProperty(BlockState state, String name) {
        return findProperty(state, name) instanceof net.minecraft.world.level.block.state.properties.BooleanProperty;
    }

    private static Property<?> findProperty(BlockState state, String name) {
        if (state == null || name == null || name.isBlank()) {
            return null;
        }
        for (Property<?> property : state.getProperties()) {
            if (name.equals(property.getName())) {
                return property;
            }
        }
        return null;
    }

    private static List<Block> lookupDistinctBlocks(List<ResourceLocation> ids, Block plantBlock) {
        List<Block> blocks = new ArrayList<>();
        if (ids == null) {
            return blocks;
        }
        for (ResourceLocation id : ids) {
            Block block = lookupBlock(id);
            if (block != Blocks.AIR && block != plantBlock && !blocks.contains(block)) {
                blocks.add(block);
            }
        }
        return blocks;
    }

    private static Block lookupBlock(ResourceLocation id) {
        if (id == null) {
            return Blocks.AIR;
        }
        Block block = BuiltInRegistries.BLOCK.get(id);
        return block == null ? Blocks.AIR : block;
    }
}
