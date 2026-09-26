package com.xiaolianganmuchen.nsukdelightcropaddons.nsuk;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AddonCropPersistenceTest {
    @Test
    void keepsAddonIdEvenIfCatalogIsNotLoaded() {
        String restored = AddonCropPersistence.restoreId("gd_chili", "", Set.of("wheat", "carrots")::contains);
        assertEquals("gd_chili", restored);
    }

    @Test
    void prefersBackupAddonCropKey() {
        String restored = AddonCropPersistence.restoreId("wheat", "gd_chili", Set.of("wheat", "carrots")::contains);
        assertEquals("gd_chili", restored);
    }

    @Test
    void ignoresVanillaCropIds() {
        String restored = AddonCropPersistence.restoreId("wheat", "", Set.of("wheat")::contains);
        assertEquals("", restored);
    }

    @Test
    void blankTagsStayBlank() {
        assertEquals("", AddonCropPersistence.restoreId("", "", id -> false));
        assertEquals("", AddonCropPersistence.firstNonBlank("  ", null));
    }
}
