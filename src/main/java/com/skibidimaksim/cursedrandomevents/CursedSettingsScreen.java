
package com.skibidimaksim.cursedrandomevents;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CursedSettingsScreen extends Screen {
    private final Screen parent;

    public CursedSettingsScreen(Screen parent) {
        super(Component.literal("Cursed Random Events"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        CursedConfig.load();

        int center = this.width / 2;
        int y = this.height / 4;

        addRenderableWidget(Button.builder(
                Component.literal("Интервал: "
                        + CursedConfig.intervalSeconds + " сек."),
                button -> {
                    CursedConfig.intervalSeconds =
                            CursedConfig.intervalSeconds >= 120
                                    ? 5
                                    : CursedConfig.intervalSeconds + 5;
                    CursedConfig.save();
                    button.setMessage(Component.literal(
                            "Интервал: "
                                    + CursedConfig.intervalSeconds + " сек."));
                }).bounds(center - 100, y, 200, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("Шанс Nether: "
                        + CursedConfig.netherChance + "%"),
                button -> {
                    CursedConfig.netherChance =
                            CursedConfig.netherChance >= 5
                                    ? 0
                                    : Math.round(
                                            (CursedConfig.netherChance + 0.1)
                                                    * 1000.0) / 1000.0;
                    CursedConfig.save();
                    button.setMessage(Component.literal(
                            "Шанс Nether: "
                                    + CursedConfig.netherChance + "%"));
                }).bounds(center - 100, y + 26, 200, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("Шанс End: "
                        + CursedConfig.endChance + "%"),
                button -> {
                    CursedConfig.endChance =
                            CursedConfig.endChance >= 1
                                    ? 0
                                    : Math.round(
                                            (CursedConfig.endChance + 0.01)
                                                    * 1000.0) / 1000.0;
                    CursedConfig.save();
                    button.setMessage(Component.literal(
                            "Шанс End: "
                                    + CursedConfig.endChance + "%"));
                }).bounds(center - 100, y + 52, 200, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("События: "
                        + (CursedConfig.eventsEnabled
                        ? "ВКЛ" : "ВЫКЛ")),
                button -> {
                    CursedConfig.eventsEnabled =
                            !CursedConfig.eventsEnabled;
                    CursedConfig.save();
                    button.setMessage(Component.literal(
                            "События: "
                                    + (CursedConfig.eventsEnabled
                                    ? "ВКЛ" : "ВЫКЛ")));
                }).bounds(center - 100, y + 78, 200, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("Назад"),
                button -> onClose()
        ).bounds(center - 100, y + 110, 200, 20).build());
    }

    @Override
    public void render(
            GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(
                font, title, width / 2, height / 4 - 25, 0xFFFFFF);
        graphics.drawCenteredString(
                font, "Cursed Studios", width / 2, height - 25, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
