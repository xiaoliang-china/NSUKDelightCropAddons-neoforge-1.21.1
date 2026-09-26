package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * 通用成熟判定：优先 {@link CropBlock#isMaxAge}，否则读取常见 AGE 方块状态。
 */
public final class CropAge {
    private CropAge() {
    }

    public static boolean isMaxAge(BlockState state) {
        if (state == null) {
            return false;
        }
        if (state.getBlock() instanceof CropBlock cropBlock) {
            return cropBlock.isMaxAge(state);
        }
        if (state.hasProperty(BlockStateProperties.AGE_7)) {
            return state.getValue(BlockStateProperties.AGE_7) >= 7;
        }
        if (state.hasProperty(BlockStateProperties.AGE_5)) {
            return state.getValue(BlockStateProperties.AGE_5) >= 5;
        }
        if (state.hasProperty(BlockStateProperties.AGE_4)) {
            return state.getValue(BlockStateProperties.AGE_4) >= 4;
        }
        if (state.hasProperty(BlockStateProperties.AGE_3)) {
            return state.getValue(BlockStateProperties.AGE_3) >= 3;
        }
        if (state.hasProperty(BlockStateProperties.AGE_2)) {
            return state.getValue(BlockStateProperties.AGE_2) >= 2;
        }
        if (state.hasProperty(BlockStateProperties.AGE_1)) {
            return state.getValue(BlockStateProperties.AGE_1) >= 1;
        }
        IntegerProperty age = findAgeProperty(state);
        return age != null && state.getValue(age) >= max(age);
    }

    public static boolean hasAge(BlockState state) {
        if (state == null) {
            return false;
        }
        return state.getBlock() instanceof CropBlock
                || state.hasProperty(BlockStateProperties.AGE_7)
                || state.hasProperty(BlockStateProperties.AGE_5)
                || state.hasProperty(BlockStateProperties.AGE_4)
                || state.hasProperty(BlockStateProperties.AGE_3)
                || state.hasProperty(BlockStateProperties.AGE_2)
                || state.hasProperty(BlockStateProperties.AGE_1)
                || findAgeProperty(state) != null;
    }

    private static IntegerProperty findAgeProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty integerProperty && CropRules.isAgePropertyName(property.getName())) {
                return integerProperty;
            }
        }
        return null;
    }

    private static int max(IntegerProperty property) {
        int highest = 0;
        for (int value : property.getPossibleValues()) {
            if (value > highest) {
                highest = value;
            }
        }
        return highest;
    }
}
