package com.xiaolianganmuchen.nsukdelightcropaddons.client;

import com.xiaolianganmuchen.nsukdelightcropaddons.config.CustomCropStorage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

/**
 * 新增自定义耕地作物。保存后会出现在农田盒选择列表的「其他」分组。
 */
@OnlyIn(Dist.CLIENT)
public final class DelightAddCropScreen extends Screen {
    private static final int PANEL_WIDTH = 320;
    private static final int PANEL_HEIGHT = 196;
    private final Screen parent;
    private EditBox seedBox;
    private EditBox plantBox;
    private EditBox nameZhBox;
    private EditBox nameEnBox;
    private Checkbox allowNonCrop;
    private Component status = Component.empty();
    private int panelX;
    private int panelY;

    public DelightAddCropScreen(Screen parent) {
        super(Component.translatable("gui.nsukdelightcropaddons.config.add.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_WIDTH) / 2;
        panelY = (height - PANEL_HEIGHT) / 2;
        seedBox = field(panelY + 40, "gui.nsukdelightcropaddons.config.field.seed", "modid:crop_seeds");
        plantBox = field(panelY + 68, "gui.nsukdelightcropaddons.config.field.plant", "");
        nameZhBox = field(panelY + 96, "gui.nsukdelightcropaddons.config.field.name_zh", "");
        nameEnBox = field(panelY + 124, "gui.nsukdelightcropaddons.config.field.name_en", "");
        allowNonCrop = addRenderableWidget(Checkbox.builder(
                Component.translatable("gui.nsukdelightcropaddons.config.field.allow_non_crop"),
                font
        ).pos(panelX + 16, panelY + 150).selected(false).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.nsukdelightcropaddons.config.save"), button -> save())
                .bounds(panelX + PANEL_WIDTH - 168, panelY + PANEL_HEIGHT - 26, 72, 18)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(panelX + PANEL_WIDTH - 88, panelY + PANEL_HEIGHT - 26, 72, 18)
                .build());
        setInitialFocus(seedBox);
    }

    private EditBox field(int y, String hintKey, String value) {
        EditBox box = new EditBox(font, panelX + 108, y, 196, 18, Component.translatable(hintKey));
        box.setMaxLength(128);
        box.setValue(value);
        box.setHint(Component.translatable(hintKey));
        return addRenderableWidget(box);
    }

    private void save() {
        String seed = seedBox.getValue().trim();
        if (seed.isBlank() || ResourceLocation.tryParse(seed) == null) {
            status = Component.translatable("gui.nsukdelightcropaddons.config.error.seed");
            return;
        }
        String plant = plantBox.getValue().trim();
        if (!plant.isBlank() && ResourceLocation.tryParse(plant) == null) {
            status = Component.translatable("gui.nsukdelightcropaddons.config.error.plant");
            return;
        }
        boolean added = CustomCropStorage.add(new CustomCropStorage.Entry(
                "",
                seed,
                plant,
                List.of(),
                allowNonCrop.selected(),
                nameZhBox.getValue().trim(),
                nameEnBox.getValue().trim()
        ));
        if (!added) {
            status = Component.translatable("gui.nsukdelightcropaddons.config.error.seed");
            return;
        }
        onClose();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xC0101018);
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xF0222228);
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 1, 0xFFE0C060);
        graphics.fill(panelX, panelY + PANEL_HEIGHT - 1, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFFE0C060);
        graphics.fill(panelX, panelY, panelX + 1, panelY + PANEL_HEIGHT, 0xFFE0C060);
        graphics.fill(panelX + PANEL_WIDTH - 1, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFFE0C060);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, panelY + 10, 0xFFE8D48A);
        graphics.drawString(font, Component.translatable("gui.nsukdelightcropaddons.config.field.seed"), panelX + 16, panelY + 45, 0xFFECECEC, false);
        graphics.drawString(font, Component.translatable("gui.nsukdelightcropaddons.config.field.plant"), panelX + 16, panelY + 73, 0xFFECECEC, false);
        graphics.drawString(font, Component.translatable("gui.nsukdelightcropaddons.config.field.name_zh"), panelX + 16, panelY + 101, 0xFFECECEC, false);
        graphics.drawString(font, Component.translatable("gui.nsukdelightcropaddons.config.field.name_en"), panelX + 16, panelY + 129, 0xFFECECEC, false);
        graphics.drawCenteredString(font, status, width / 2, panelY + PANEL_HEIGHT - 42, 0xFFFF8888);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
