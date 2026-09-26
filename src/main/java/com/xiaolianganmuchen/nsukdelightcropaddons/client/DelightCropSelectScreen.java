package com.xiaolianganmuchen.nsukdelightcropaddons.client;

import client.cn.kafei.simukraft.client.ui.SimuKraftUiTheme;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.xiaolianganmuchen.nsukdelightcropaddons.config.CropSelectUiStorage;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropCatalog;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropDisplayNames;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropLangIndex;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.ResolvedCrop;
import common.cn.kafei.simukraft.farmland.FarmCrop;
import common.cn.kafei.simukraft.network.farmland.FarmlandBoxOpenRequestPacket;
import common.cn.kafei.simukraft.network.farmland.FarmlandBoxOpenResponsePacket;
import common.cn.kafei.simukraft.network.farmland.FarmlandBoxSetCropPacket;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 农田盒作物选择界面：原版与农夫乐事始终展开，其余乐事按来源折叠。
 * 只有一种可选作物的来源不折叠，排在农夫乐事后面。顶部搜索框走同一套布局。
 */
@OnlyIn(Dist.CLIENT)
public final class DelightCropSelectScreen {
    private static final int ITEM_HEIGHT = 24;
    private static final int HEADER_HEIGHT = 14;
    private static final int HEADER_COLOR = 0xFFF5F5A0;
    private static final int ICON_SIZE = 18;
    private static final int PANEL_MAX_WIDTH = 320;
    private static final int PANEL_HEIGHT = 236;
    private static final int SEARCH_HEIGHT = 22;

    private DelightCropSelectScreen() {
    }

    public static void open(FarmlandBoxOpenResponsePacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }
        CropLangIndex.refresh();
        minecraft.execute(() -> minecraft.setScreen(new CropScreen(createUi(packet), Component.empty())));
    }

    private static ModularUI createUi(FarmlandBoxOpenResponsePacket packet) {
        int screenWidth = Math.max(320, Minecraft.getInstance().getWindow().getGuiScaledWidth());
        int screenHeight = Math.max(240, Minecraft.getInstance().getWindow().getGuiScaledHeight());
        UIElement root = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
            layout.paddingAll(8);
        });
        root.addChild(SimuKraftUiTheme.createShellPanel(screenWidth, screenHeight));
        root.addChild(topButton("gui.button.back", () -> back(packet.boxPos())));

        UIElement panel = new UIElement().layout(layout -> {
            layout.widthPercent(90);
            layout.maxWidth(PANEL_MAX_WIDTH);
            layout.height(Math.min(PANEL_HEIGHT, screenHeight - 48));
            layout.flexDirection(FlexDirection.COLUMN);
            layout.alignItems(AlignItems.STRETCH);
            layout.paddingAll(10);
            layout.gapAll(6);
        }).addClass("simukraft_panel");

        panel.addChild(label(Component.translatable("gui.simukraft.farmland_box.select_crop_title"), Horizontal.CENTER, 0xFFFFFFFF, 16));
        panel.addChild(label(Component.translatable("gui.nsukdelightcropaddons.select_crop.hint"), Horizontal.CENTER, 0xFFBDBDBD, 12));

        UIElement list = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.flexDirection(FlexDirection.COLUMN);
            layout.alignItems(AlignItems.STRETCH);
            layout.gapAll(4);
        });
        CropListState state = new CropListState(packet, list);
        panel.addChild(searchBox(state));

        ScrollerView scroller = new ScrollerView();
        scroller.scrollerStyle(style -> style.mode(ScrollerMode.VERTICAL));
        scroller.layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.minHeight(80);
        });
        scroller.addScrollViewChild(list);
        panel.addChild(scroller);
        state.rebuild();
        root.addChild(panel);
        return new ModularUI(SimuKraftUiTheme.createUi(root))
                .shouldCloseOnEsc(true)
                .shouldCloseOnKeyInventory(false);
    }

    private static UIElement searchBox(CropListState state) {
        UIElement wrap = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.height(SEARCH_HEIGHT);
            layout.minHeight(SEARCH_HEIGHT);
            layout.maxHeight(SEARCH_HEIGHT);
            layout.flexShrink(0);
        }).style(style -> style.backgroundTexture(new GuiTextureGroup(
                new ColorRectTexture(0xFF141418),
                new ColorBorderTexture(1, 0xFF8A8A70)
        )));
        TextField field = new TextField();
        field.setAnyString();
        field.setTextResponder(state::setQuery);
        field.layout(layout -> {
            layout.widthPercent(100);
            layout.height(SEARCH_HEIGHT);
            layout.paddingLeft(6);
            layout.paddingRight(6);
            layout.paddingTop(5);
            layout.paddingBottom(5);
        });
        field.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        field.textFieldStyle(style -> style
                .fontSize(9.0F)
                .textColor(0xFFFFFFFF)
                .cursorColor(0xFFFFFFFF)
                .textShadow(false)
                .placeholder(Component.translatable("gui.nsukdelightcropaddons.select_crop.search"))
                .focusOverlay(IGuiTexture.EMPTY));
        wrap.addChild(field);
        return wrap;
    }

    private static UIElement vanillaButton(FarmlandBoxOpenResponsePacket packet, FarmCrop crop) {
        boolean selected = crop.id().equals(packet.cropId());
        Component name = Component.translatable(crop.translationKey());
        return cropRow(packet.boxPos(), crop.id(), new ItemStack(crop.seed()), name, selected);
    }

    private static UIElement delightButton(FarmlandBoxOpenResponsePacket packet, ResolvedCrop crop) {
        boolean selected = crop.id().equals(packet.cropId());
        return cropRow(packet.boxPos(), crop.id(), new ItemStack(crop.seed()), CropDisplayNames.of(crop), selected);
    }

    private static UIElement cropRow(BlockPos boxPos, String cropId, ItemStack icon, Component name, boolean selected) {
        UIElement row = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.height(ITEM_HEIGHT);
            layout.flexDirection(FlexDirection.ROW);
            layout.alignItems(AlignItems.CENTER);
            layout.gapAll(6);
        });
        row.addChild(new UIElement().layout(layout -> {
            layout.width(ICON_SIZE);
            layout.height(ICON_SIZE);
            layout.flexShrink(0);
        }).style(style -> style.backgroundTexture(new ItemStackTexture(icon))));

        Button button = new Button();
        Component text = selected
                ? Component.translatable("gui.simukraft.farmland_box.crop_selected", name)
                : name;
        button.setText(text);
        button.setOnClick(event -> select(boxPos, cropId));
        button.layout(layout -> {
            layout.height(ITEM_HEIGHT);
            layout.flex(1);
        });
        row.addChild(button);
        return row;
    }

    private static UIElement sectionTitle(Component text) {
        return label(text, Horizontal.LEFT, HEADER_COLOR, HEADER_HEIGHT);
    }

    private static UIElement foldHeader(String modId, boolean expanded, Runnable toggle) {
        String key = expanded
                ? "gui.nsukdelightcropaddons.select_crop.group_expanded"
                : "gui.nsukdelightcropaddons.select_crop.group_collapsed";
        Button header = new Button().noText();
        header.buttonStyle(style -> style
                .baseTexture(IGuiTexture.EMPTY)
                .hoverTexture(new ColorRectTexture(0x22F5F5A0))
                .pressedTexture(IGuiTexture.EMPTY));
        header.setOnClick(event -> toggle.run());
        header.layout(layout -> {
            layout.widthPercent(100);
            layout.height(HEADER_HEIGHT);
            layout.flexDirection(FlexDirection.ROW);
            layout.alignItems(AlignItems.CENTER);
        });
        header.addChild(label(
                Component.translatable(key, CropDisplayNames.source(modId)),
                Horizontal.LEFT,
                HEADER_COLOR,
                HEADER_HEIGHT
        ));
        return header;
    }

    private static boolean keepOpen(String modId, int totalCount) {
        return "farmersdelight".equals(modId) || totalCount <= 1;
    }

    private static int sourceRank(String modId, int totalCount) {
        if ("farmersdelight".equals(modId)) {
            return 0;
        }
        if (totalCount <= 1) {
            return 1;
        }
        if ("custom".equals(modId)) {
            return 3;
        }
        return 2;
    }

    private static List<Map.Entry<String, List<ResolvedCrop>>> orderedSources(Map<String, List<ResolvedCrop>> grouped) {
        List<Map.Entry<String, List<ResolvedCrop>>> entries = new ArrayList<>(grouped.entrySet());
        entries.sort(Comparator
                .comparingInt((Map.Entry<String, List<ResolvedCrop>> entry) -> sourceRank(entry.getKey(), entry.getValue().size()))
                .thenComparing(entry -> CropDisplayNames.source(entry.getKey()).getString()));
        return entries;
    }

    private static Button topButton(String key, Runnable action) {
        Button button = new Button();
        button.setText(Component.translatable(key));
        button.setOnClick(event -> action.run());
        button.layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(5);
            layout.top(5);
            layout.width(50);
            layout.height(22);
        });
        return button;
    }

    private static Label label(Component text, Horizontal horizontal, int color, int height) {
        Label label = new Label();
        label.setText(text);
        label.layout(layout -> {
            layout.widthPercent(100);
            layout.height(height);
        });
        label.textStyle(style -> style
                .textColor(color)
                .textShadow(true)
                .textAlignHorizontal(horizontal)
                .textAlignVertical(Vertical.CENTER));
        return label;
    }

    private static void select(BlockPos boxPos, String cropId) {
        PacketDistributor.sendToServer(new FarmlandBoxSetCropPacket(boxPos, cropId));
    }

    private static void back(BlockPos boxPos) {
        PacketDistributor.sendToServer(new FarmlandBoxOpenRequestPacket(boxPos));
    }

    private static final class CropListState {
        private final FarmlandBoxOpenResponsePacket packet;
        private final UIElement list;
        private String query = "";

        private CropListState(FarmlandBoxOpenResponsePacket packet, UIElement list) {
            this.packet = packet;
            this.list = list;
        }

        private void setQuery(String text) {
            String next = text == null ? "" : text.trim();
            if (next.equals(query)) {
                return;
            }
            query = next;
            rebuild();
        }

        private void toggle(String modId) {
            CropSelectUiStorage.toggle(modId);
            rebuild();
        }

        private void rebuild() {
            list.clearAllChildren();
            boolean searching = !query.isBlank();
            boolean any = false;
            boolean vanillaHeader = false;
            for (FarmCrop crop : FarmCrop.values()) {
                if (!CropDisplayNames.matches(query, crop)) {
                    continue;
                }
                if (!vanillaHeader) {
                    list.addChild(sectionTitle(Component.translatable("source.nsukdelightcropaddons.vanilla")));
                    vanillaHeader = true;
                }
                list.addChild(vanillaButton(packet, crop));
                any = true;
            }
            Map<String, List<ResolvedCrop>> grouped = CropCatalog.selectableBySource();
            if (grouped.isEmpty() && !searching) {
                list.addChild(label(Component.translatable("gui.nsukdelightcropaddons.select_crop.empty"), Horizontal.LEFT, 0xFFBDBDBD, 14));
                return;
            }
            for (Map.Entry<String, List<ResolvedCrop>> entry : orderedSources(grouped)) {
                String modId = entry.getKey();
                List<ResolvedCrop> all = entry.getValue();
                List<ResolvedCrop> matched = new ArrayList<>();
                for (ResolvedCrop crop : all) {
                    if (CropDisplayNames.matches(query, crop)) {
                        matched.add(crop);
                    }
                }
                if (matched.isEmpty()) {
                    continue;
                }
                boolean open = searching || keepOpen(modId, all.size()) || CropSelectUiStorage.isExpanded(modId);
                if (keepOpen(modId, all.size()) || searching) {
                    list.addChild(sectionTitle(CropDisplayNames.source(modId)));
                } else {
                    list.addChild(foldHeader(modId, open, () -> toggle(modId)));
                }
                if (open) {
                    for (ResolvedCrop crop : matched) {
                        list.addChild(delightButton(packet, crop));
                    }
                }
                any = true;
            }
            if (!any) {
                list.addChild(label(Component.translatable("gui.nsukdelightcropaddons.select_crop.no_match"), Horizontal.LEFT, 0xFFBDBDBD, 14));
            }
        }
    }

    private static final class CropScreen extends ModularUIScreen {
        private CropScreen(ModularUI modularUI, Component title) {
            super(modularUI, title);
        }
    }
}
