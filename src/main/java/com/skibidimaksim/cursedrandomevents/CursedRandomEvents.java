
package com.skibidimaksim.cursedrandomevents;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;

@Mod(CursedRandomEvents.MOD_ID)
public class CursedRandomEvents {
    public static final String MOD_ID = "cursedrandomevents";

    public CursedRandomEvents(
            FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        NeoForge.EVENT_BUS.register(new CursedEventManager());
        CursedItems.ITEMS.register(modEventBus);
    }
}
