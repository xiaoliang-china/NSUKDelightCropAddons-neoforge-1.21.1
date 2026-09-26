package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CropHarvestLogicTest {
    @Test
    void tallCropNeedsEverySegmentMature() {
        assertFalse(CropRules.columnIsComplete(2, 1, true));
        assertFalse(CropRules.columnIsComplete(2, 2, false));
        assertTrue(CropRules.columnIsComplete(2, 2, true));
        assertTrue(CropRules.columnIsComplete(2, 3, true));
    }

    @Test
    void singleBlockCropOnlyNeedsOneMatureSegment() {
        assertFalse(CropRules.columnIsComplete(1, 0, true));
        assertFalse(CropRules.columnIsComplete(1, 1, false));
        assertTrue(CropRules.columnIsComplete(1, 1, true));
    }

    @Test
    void tomatoBuddingAtMaxAgeIsNotHarvestable() {
        assertFalse(CropRules.isHarvestableStage(1, true, false, false, true, true, false));
    }

    @Test
    void oneBlockRipeTomatoVineIsHarvestable() {
        assertTrue(CropRules.isHarvestableStage(1, true, true, false, true, true, false));
        assertFalse(CropRules.isHarvestableStage(1, true, true, false, true, false, false));
    }

    @Test
    void pineappleMaxAgeCropIsHarvestable() {
        assertTrue(CropRules.isHarvestableStage(1, true, false, false, true, true, true));
        assertTrue(CropRules.isHarvestableStage(1, true, true, false, false, false, true));
        assertFalse(CropRules.isHarvestableStage(1, true, false, false, true, false, true));
    }

    @Test
    void tallCornUsesEachSegmentAge() {
        assertTrue(CropRules.isHarvestableStage(2, true, false, false, true, true, true));
        assertFalse(CropRules.isHarvestableStage(2, true, false, false, true, false, true));
        assertTrue(CropRules.isHarvestableStage(2, true, true, true, true, true, true));
    }

    @Test
    void matchingFieldKeepsCropAndEmptyBoxStaysEmpty() {
        assertEquals(CropRules.BoxCropAction.KEEP, CropRules.decideBoxCrop(true, false));
        assertEquals(CropRules.BoxCropAction.CLEAR, CropRules.decideBoxCrop(true, true));
        assertEquals(CropRules.BoxCropAction.KEEP, CropRules.decideBoxCrop(false, false));
        assertEquals(CropRules.BoxCropAction.KEEP, CropRules.decideBoxCrop(false, true));
    }
}
