
package com.skibidimaksim.cursedrandomevents;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(CursedRandomEvents.MOD_ID)
public class CursedRandomEvents {
    public static final String MOD_ID = "cursedrandomevents";

    public CursedRandomEvents(
            net.neoforged.bus.api.IEventBus modEventBus) {
        NeoForge.EVENT_BUS.register(new CursedEventManager());
        CursedItems.ITEMS.register(modEventBus);
    }
}
