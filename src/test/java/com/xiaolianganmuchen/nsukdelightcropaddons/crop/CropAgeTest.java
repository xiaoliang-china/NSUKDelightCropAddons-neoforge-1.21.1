package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CropAgeTest {
    @Test
    void recognizesVanillaAndModAgePropertyNames() {
        assertTrue(CropRules.isAgePropertyName("age"));
        assertTrue(CropRules.isAgePropertyName("corn_age"));
        assertFalse(CropRules.isAgePropertyName("moisture"));
        assertFalse(CropRules.isAgePropertyName(null));
        assertFalse(CropRules.isAgePropertyName(""));
    }
}
