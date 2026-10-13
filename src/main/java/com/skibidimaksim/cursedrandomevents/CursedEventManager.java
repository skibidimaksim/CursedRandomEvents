
package com.skibidimaksim.cursedrandomevents;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Random;

public class CursedEventManager {
    private static final Random RANDOM = new Random();
    private static int ticks = 0;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        if (++ticks < 600) return;
        ticks = 0;

        for (ServerPlayer player :
                event.getServer().getPlayerList().getPlayers()) {
            ServerLevel level = player.serverLevel();
            BlockPos pos = player.blockPosition();

            switch (RANDOM.nextInt(6)) {
                case 0 -> {
                    LightningBolt bolt =
                        EntityType.LIGHTNING_BOLT.create(level);
                    if (bolt != null) {
                        bolt.moveTo(
                            pos.getX() + 2,
                            pos.getY(),
                            pos.getZ() + 2
                        );
                        level.addFreshEntity(bolt);
                    }
                }
                case 1 -> {
                    for (int i = 0; i < 5; i++) {
                        var zombie = EntityType.ZOMBIE.create(level);
                        if (zombie != null) {
                            zombie.moveTo(
                                pos.getX() + RANDOM.nextInt(9) - 4,
                                pos.getY(),
                                pos.getZ() + RANDOM.nextInt(9) - 4,
                                0, 0
                            );
                            level.addFreshEntity(zombie);
                        }
                    }
                }
                case 2 ->
                    player.addEffect(new MobEffectInstance(
                        MobEffects.BLINDNESS, 200, 0
                    ));
                case 3 ->
                    player.addEffect(new MobEffectInstance(
                        MobEffects.LEVITATION, 100, 1
                    ));
                case 4 -> {
                    for (int i = 0; i < 10; i++) {
                        var chicken = EntityType.CHICKEN.create(level);
                        if (chicken != null) {
                            chicken.moveTo(
                                pos.getX() + RANDOM.nextInt(7) - 3,
                                pos.getY() + 1,
                                pos.getZ() + RANDOM.nextInt(7) - 3,
                                0, 0
                            );
                            level.addFreshEntity(chicken);
                        }
                    }
                }
                case 5 ->
                    player.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, 200, 2
                    ));
            }
        }
    }
}
