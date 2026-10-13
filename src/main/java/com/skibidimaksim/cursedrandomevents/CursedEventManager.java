
package com.skibidimaksim.cursedrandomevents;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class CursedEventManager {
    private static final Random RANDOM = new Random();
    private static final int EVENT_INTERVAL = 600;

    private static int ticks = 0;

    private record ReturnPoint(
        ResourceKey<Level> dimension,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
    ) {}

    private static final Map<UUID, ReturnPoint> RETURN_POINTS =
        new HashMap<>();

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        ticks++;

        if (ticks >= EVENT_INTERVAL) {
            ticks = 0;

            for (ServerPlayer player :
                    event.getServer().getPlayerList().getPlayers()) {
                triggerEvent(player);
            }
        }

        if (ticks % 20 == 0) {
            int seconds = (EVENT_INTERVAL - ticks + 19) / 20;

            for (ServerPlayer player :
                    event.getServer().getPlayerList().getPlayers()) {
                player.displayClientMessage(
                    Component.literal("Next event in: " + seconds + "s"),
                    true
                );
            }
        }
    }

    private void triggerEvent(ServerPlayer player) {
        ServerLevel level = player.serverLevel();

        // Rare dimension events are rolled only in the Overworld.
        if (level.dimension().equals(Level.OVERWORLD)) {
            double roll = RANDOM.nextDouble();

            // 0.67%: teleport straight to the Nether.
            if (roll < 0.0067) {
                teleportToDimension(player, Level.NETHER);
                return;
            }

            // 0.067%: teleport straight to the End.
            if (roll < 0.00737) {
                teleportToDimension(player, Level.END);
                return;
            }
        }

        BlockPos pos = player.blockPosition();

        switch (RANDOM.nextInt(12)) {
            case 0 -> {
                var bolt = EntityType.LIGHTNING_BOLT.create(
                    level, EntitySpawnReason.EVENT
                );

                if (bolt != null) {
                    bolt.setPos(
                        pos.getX() + 2.5,
                        pos.getY(),
                        pos.getZ() + 2.5
                    );
                    level.addFreshEntity(bolt);
                }
            }

            case 1 -> spawnMobs(
                level, EntityType.ZOMBIE, pos, 5
            );

            case 2 -> player.addEffect(
                new MobEffectInstance(
                    MobEffects.BLINDNESS, 200, 0
                )
            );

            case 3 -> player.addEffect(
                new MobEffectInstance(
                    MobEffects.LEVITATION, 100, 1
                )
            );

            case 4 -> spawnMobs(
                level, EntityType.CHICKEN, pos, 10
            );

            case 5 -> player.addEffect(
                new MobEffectInstance(
                    MobEffects.SLOWNESS, 200, 2
                )
            );

            case 6 -> spawnMobs(
                level, EntityType.PIG, pos, 8
            );

            case 7 -> spawnMobs(
                level, EntityType.SPIDER, pos, 5
            );

            case 8 -> player.addEffect(
                new MobEffectInstance(
                    MobEffects.SPEED, 300, 2
                )
            );

            case 9 -> player.addEffect(
                new MobEffectInstance(
                    MobEffects.STRENGTH, 300, 1
                )
            );

            case 10 -> player.addEffect(
                new MobEffectInstance(
                    MobEffects.JUMP_BOOST, 200, 3
                )
            );

            case 11 -> spawnMobs(
                level, EntityType.CREEPER, pos, 3
            );
        }
    }

    private void teleportToDimension(
            ServerPlayer player,
            ResourceKey<Level> targetDimension) {

        if (player.getServer() == null) {
            return;
        }

        ServerLevel destination =
            player.getServer().getLevel(targetDimension);

        if (destination == null) {
            return;
        }

        // Remember the original position only once.
        RETURN_POINTS.putIfAbsent(
            player.getUUID(),
            new ReturnPoint(
                player.level().dimension(),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot()
            )
        );

        BlockPos safe;

        if (targetDimension.equals(Level.NETHER)) {
            int x = (int) Math.floor(player.getX());
            int z = (int) Math.floor(player.getZ());

            safe = findSafePosition(destination, x, z);

            if (safe == null) {
                BlockPos spawn = destination.getSharedSpawnPos();
                safe = findSafePosition(
                    destination, spawn.getX(), spawn.getZ()
                );
            }

            if (safe == null) {
                RETURN_POINTS.remove(player.getUUID());
                return;
            }
        } else {
            // Build a small safe obsidian platform in the End.
            safe = new BlockPos(100, 50, 0);

            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    destination.setBlockAndUpdate(
                        safe.offset(dx, -1, dz),
                        Blocks.OBSIDIAN.defaultBlockState()
                    );

                    destination.setBlockAndUpdate(
                        safe.offset(dx, 0, dz),
                        Blocks.AIR.defaultBlockState()
                    );

                    destination.setBlockAndUpdate(
                        safe.offset(dx, 1, dz),
                        Blocks.AIR.defaultBlockState()
                    );
                }
            }
        }

        player.teleportTo(
            destination,
            safe.getX() + 0.5,
            safe.getY(),
            safe.getZ() + 0.5,
            Set.of(),
            player.getYRot(),
            player.getXRot()
        );

        String message = targetDimension.equals(Level.NETHER)
            ? "§c§lRARE EVENT: WELCOME TO THE NETHER!"
            : "§5§lULTRA RARE EVENT: WELCOME TO THE END!";

        player.displayClientMessage(
            Component.literal(message),
            false
        );
    }

    private BlockPos findSafePosition(
            ServerLevel level, int x, int z) {

        for (int radius = 0; radius <= 8; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz))
                            != radius) {
                        continue;
                    }

                    for (int y = 100; y >= 32; y--) {
                        BlockPos p = new BlockPos(
                            x + dx, y, z + dz
                        );

                        boolean feetAir =
                            level.getBlockState(p).isAir();

                        boolean headAir =
                            level.getBlockState(p.above()).isAir();

                        boolean solidFloor =
                            level.getBlockState(p.below())
                                .isSolid();

                        boolean dryFloor =
                            level.getBlockState(p.below())
                                .getFluidState().isEmpty();

                        if (feetAir && headAir
                                && solidFloor && dryFloor) {
                            return p;
                        }
                    }
                }
            }
        }

        return null;
    }

    private void spawnMobs(
            ServerLevel level,
            EntityType<?> type,
            BlockPos center,
            int count) {

        for (int i = 0; i < count; i++) {
            var mob = type.create(
                level, EntitySpawnReason.EVENT
            );

            if (mob != null) {
                mob.setPos(
                    center.getX() + RANDOM.nextInt(9) - 4,
                    center.getY() + 1,
                    center.getZ() + RANDOM.nextInt(9) - 4
                );

                level.addFreshEntity(mob);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ReturnPoint point =
            RETURN_POINTS.remove(player.getUUID());

        if (point == null || player.getServer() == null) {
            return;
        }

        ServerLevel destination =
            player.getServer().getLevel(point.dimension());

        if (destination == null) {
            return;
        }

        player.teleportTo(
            destination,
            point.x(),
            point.y(),
            point.z(),
            Set.of(),
            point.yaw(),
            point.pitch()
        );
    }
        }
