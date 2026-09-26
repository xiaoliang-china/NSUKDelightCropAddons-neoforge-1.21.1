package com.xiaolianganmuchen.nsukdelightcropaddons.client;

import com.xiaolianganmuchen.nsukdelightcropaddons.config.CustomCropStorage;
import com.xiaolianganmuchen.nsukdelightcropaddons.config.DelightCropConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * 模组菜单配置：来源开关 + 自定义作物。自定义作物会出现在农田盒「其他」分组。
 */
@OnlyIn(Dist.CLIENT)
public final class DelightModConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 340;
    private static final int PANEL_HEIGHT = 220;
    private static final int ROW_HEIGHT = 20;
    private final Screen parent;
    private int tab;
    private int scroll;
    private int panelX;
    private int panelY;
    private Button tabSources;
    private Button tabCustom;
    private Button addCrop;
    private Button done;
    private final List<Button> dynamicButtons = new ArrayList<>();

    public DelightModConfigScreen(Screen parent) {
        super(Component.translatable("gui.nsukdelightcropaddons.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_WIDTH) / 2;
        panelY = (height - PANEL_HEIGHT) / 2;
        clearWidgets();
        dynamicButtons.clear();
        tabSources = addRenderableWidget(Button.builder(Component.translatable("gui.nsukdelightcropaddons.config.tab.sources"), button -> {
            tab = 0;
            scroll = 0;
            rebuild();
        }).bounds(panelX + 12, panelY + 28, 150, 18).build());
        tabCustom = addRenderableWidget(Button.builder(Component.translatable("gui.nsukdelightcropaddons.config.tab.custom"), button -> {
            tab = 1;
            scroll = 0;
            rebuild();
        }).bounds(panelX + PANEL_WIDTH - 162, panelY + 28, 150, 18).build());
        done = addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(panelX + PANEL_WIDTH - 88, panelY + PANEL_HEIGHT - 26, 76, 18)
                .build());
        addCrop = addRenderableWidget(Button.builder(Component.translatable("gui.nsukdelightcropaddons.config.add"), button -> {
            if (minecraft != null) {
                minecraft.setScreen(new DelightAddCropScreen(this));
            }
        }).bounds(panelX + 12, panelY + PANEL_HEIGHT - 26, 88, 18).build());
        rebuild();
    }

    private void rebuild() {
        for (Button button : dynamicButtons) {
            removeWidget(button);
        }
        dynamicButtons.clear();
        addCrop.visible = tab == 1;
        tabSources.active = tab != 0;
        tabCustom.active = tab != 1;
        int contentTop = panelY + 52;
        int contentBottom = panelY + PANEL_HEIGHT - 34;
        int visible = Math.max(1, (contentBottom - contentTop) / ROW_HEIGHT);
        if (tab == 0) {
            List<Row> rows = sourceRows();
            scroll = Mth.clamp(scroll, 0, Math.max(0, rows.size() - visible));
            int y = contentTop;
            for (int index = scroll; index < rows.size() && y + ROW_HEIGHT <= contentBottom; index++) {
                Row row = rows.get(index);
                Button toggle = Button.builder(row.right, button -> {
                    row.action.run();
                    rebuild();
                }).bounds(panelX + PANEL_WIDTH - 86, y + 1, 70, 18).build();
                dynamicButtons.add(addRenderableWidget(toggle));
                y += ROW_HEIGHT;
            }
            return;
        }
        List<CustomCropStorage.Entry> entries = CustomCropStorage.entries();
        scroll = Mth.clamp(scroll, 0, Math.max(0, entries.size() - visible));
        int y = contentTop;
        for (int index = scroll; index < entries.size() && y + ROW_HEIGHT <= contentBottom; index++) {
            CustomCropStorage.Entry entry = entries.get(index);
            Button delete = Button.builder(Component.translatable("gui.nsukdelightcropaddons.config.remove"), button -> {
                CustomCropStorage.remove(entry.id());
                rebuild();
            }).bounds(panelX + PANEL_WIDTH - 86, y + 1, 70, 18).build();
            dynamicButtons.add(addRenderableWidget(delete));
            y += ROW_HEIGHT;
        }
    }

    private List<Row> sourceRows() {
        List<Row> rows = new ArrayList<>();
        rows.add(new Row(
                Component.translatable("nsukdelightcropaddons.configuration.autoDiscovery"),
                toggleLabel(DelightCropConfig.autoDiscovery()),
                () -> DelightCropConfig.setAutoDiscovery(!DelightCropConfig.autoDiscovery())
        ));
        for (DelightCropConfig.SourceToggle toggle : DelightCropConfig.sourceToggles()) {
            rows.add(new Row(
                    Component.translatable(toggle.labelKey()),
                    toggleLabel(toggle.enabled()),
                    () -> toggle.setEnabled(!toggle.enabled())
            ));
        }
        return rows;
    }

    private static Component toggleLabel(boolean enabled) {
        return Component.translatable(enabled
                ? "gui.nsukdelightcropaddons.config.on"
                : "gui.nsukdelightcropaddons.config.off");
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xC0101018);
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xF0222228);
        drawBorder(graphics, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 0xFFE0C060);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, panelY + 10, 0xFFE8D48A);
        int contentTop = panelY + 52;
        int contentBottom = panelY + PANEL_HEIGHT - 34;
        graphics.fill(panelX + 10, contentTop - 4, panelX + PANEL_WIDTH - 10, contentBottom, 0xFF16161C);
        drawBorder(graphics, panelX + 10, contentTop - 4, PANEL_WIDTH - 20, contentBottom - (contentTop - 4), 0xFF4A4A52);
        graphics.enableScissor(panelX + 12, contentTop, panelX + PANEL_WIDTH - 12, contentBottom);
        if (tab == 0) {
            List<Row> rows = sourceRows();
            int y = contentTop;
            int visible = Math.max(1, (contentBottom - contentTop) / ROW_HEIGHT);
            scroll = Mth.clamp(scroll, 0, Math.max(0, rows.size() - visible));
            for (int index = scroll; index < rows.size() && y + ROW_HEIGHT <= contentBottom; index++) {
                graphics.drawString(font, rows.get(index).left, panelX + 18, y + 6, 0xFFECECEC, false);
                y += ROW_HEIGHT;
            }
        } else {
            List<CustomCropStorage.Entry> entries = CustomCropStorage.entries();
            if (entries.isEmpty()) {
                graphics.drawWordWrap(font, Component.translatable("gui.nsukdelightcropaddons.config.custom.empty"),
                        panelX + 18, contentTop + 8, PANEL_WIDTH - 40, 0xFFBDBDBD);
            } else {
                int y = contentTop;
                int visible = Math.max(1, (contentBottom - contentTop) / ROW_HEIGHT);
                scroll = Mth.clamp(scroll, 0, Math.max(0, entries.size() - visible));
                for (int index = scroll; index < entries.size() && y + ROW_HEIGHT <= contentBottom; index++) {
                    CustomCropStorage.Entry entry = entries.get(index);
                    String name = !entry.nameZh().isBlank() ? entry.nameZh() : (!entry.nameEn().isBlank() ? entry.nameEn() : entry.seed());
                    graphics.drawString(font, name, panelX + 18, y + 3, 0xFFECECEC, false);
                    graphics.drawString(font, entry.seed(), panelX + 18, y + 12, 0xFF9A9A9A, false);
                    y += ROW_HEIGHT;
                }
            }
        }
        graphics.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH && mouseY >= panelY && mouseY <= panelY + PANEL_HEIGHT) {
            scroll -= (int) Math.signum(scrollY);
            rebuild();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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

    private static void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    private record Row(Component left, Component right, Runnable action) {
    }
}
