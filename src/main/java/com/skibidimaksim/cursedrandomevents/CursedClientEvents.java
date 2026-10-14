
package com.skibidimaksim.cursedrandomevents;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(
        modid = CursedRandomEvents.MOD_ID,
        value = Dist.CLIENT
)
public final class CursedClientEvents {
    private CursedClientEvents() {}

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof TitleScreen)) {
            return;
        }

        int x = event.getScreen().width / 2 + 104;
        int y = event.getScreen().height / 4 + 48;

        event.addListener(Button.builder(
                net.minecraft.network.chat.Component.literal("❔"),
                button -> net.minecraft.client.Minecraft.getInstance()
                        .setScreen(new CursedSettingsScreen(event.getScreen()))
        ).bounds(x, y, 20, 20).build());
    }
}
