package com.skibidimaksim.cursedrandomevents;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class CursedEventManager {

private static final Random RANDOM = new Random();
private static final int EVENT_INTERVAL = 600;
private static int ticks = 0;

private static final Deque<Integer> RECENT_EVENTS = new ArrayDeque<>();
private static final int MAX_RECENT_EVENTS = 5;

private enum Rarity {
    COMMON, RARE, EPIC, LEGENDARY
}

@FunctionalInterface
private interface EventAction {
    void run(ServerPlayer player);
}

private record EventDefinition(
    int id,
    String name,
    Rarity rarity,
    EventAction action
) {}

private record ReturnPoint(
    ResourceKey<Level> dimension,
    double x, double y, double z,
    float yaw, float pitch
) {}

private static final Map<UUID, ReturnPoint> RETURN_POINTS =
    new HashMap<>();

private static final List<EventDefinition> EVENTS = List.of(

    // COMMON: 1-20
    new EventDefinition(1, "Speed Boost", Rarity.COMMON,
        p -> effect(p, MobEffects.SPEED, 300, 1)),
    new EventDefinition(2, "Slowness", Rarity.COMMON,
        p -> effect(p, MobEffects.SLOWNESS, 200, 1)),
    new EventDefinition(3, "Blindness", Rarity.COMMON,
        p -> effect(p, MobEffects.BLINDNESS, 160, 0)),
    new EventDefinition(4, "Levitation", Rarity.COMMON,
        p -> effect(p, MobEffects.LEVITATION, 60, 1)),
    new EventDefinition(5, "Super Jump", Rarity.COMMON,
        p -> effect(p, MobEffects.JUMP_BOOST, 200, 2)),
    new EventDefinition(6, "Strength", Rarity.COMMON,
        p -> effect(p, MobEffects.STRENGTH, 240, 1)),
    new EventDefinition(7, "Zombie Attack", Rarity.COMMON,
        p -> spawn(p, EntityType.ZOMBIE, 5)),
    new EventDefinition(8, "Chicken Rain", Rarity.COMMON,
        p -> spawn(p, EntityType.CHICKEN, 10)),
    new EventDefinition(9, "Pig Party", Rarity.COMMON,
        p -> spawn(p, EntityType.PIG, 8)),
    new EventDefinition(10, "Spider Attack", Rarity.COMMON,
        p -> spawn(p, EntityType.SPIDER, 5)),
    new EventDefinition(11, "Lightning Strike", Rarity.COMMON,
        CursedEventManager::lightning),
    new EventDefinition(12, "Creeper Surprise", Rarity.COMMON,
        p -> spawn(p, EntityType.CREEPER, 1)),
    new EventDefinition(13, "Cow Invasion", Rarity.COMMON,
        p -> spawn(p, EntityType.COW, 6)),
    new EventDefinition(14, "Sheep Invasion", Rarity.COMMON,
        p -> spawn(p, EntityType.SHEEP, 6)),
    new EventDefinition(15, "Skeleton Attack", Rarity.COMMON,
        p -> spawn(p, EntityType.SKELETON, 3)),
    new EventDefinition(16, "Witch Surprise", Rarity.COMMON,
        p -> spawn(p, EntityType.WITCH, 1)),
    new EventDefinition(17, "Night Vision", Rarity.COMMON,
        p -> effect(p, MobEffects.NIGHT_VISION, 400, 0)),
    new EventDefinition(18, "Weakness", Rarity.COMMON,
        p -> effect(p, MobEffects.WEAKNESS, 240, 1)),
    new EventDefinition(19, "Hunger", Rarity.COMMON,
        p -> effect(p, MobEffects.HUNGER, 240, 1)),
    new EventDefinition(20, "Regeneration", Rarity.COMMON,
        p -> effect(p, MobEffects.REGENERATION, 160, 1)),

    // RARE: 21-35
    new EventDefinition(21, "Drowned Attack", Rarity.RARE,
        p -> spawn(p, EntityType.DROWNED, 4)),
    new EventDefinition(22, "Stray Attack", Rarity.RARE,
        p -> spawn(p, EntityType.STRAY, 4)),
    new EventDefinition(23, "Husk Attack", Rarity.RARE,
        p -> spawn(p, EntityType.HUSK, 4)),
    new EventDefinition(24, "Cave Spider Swarm", Rarity.RARE,
        p -> spawn(p, EntityType.CAVE_SPIDER, 5)),
    new EventDefinition(25, "Slime Party", Rarity.RARE,
        p -> spawn(p, EntityType.SLIME, 5)),
    new EventDefinition(26, "Phantom Attack", Rarity.RARE,
        p -> spawn(p, EntityType.PHANTOM, 3)),
    new EventDefinition(27, "Enderman Surprise", Rarity.RARE,
        p -> spawn(p, EntityType.ENDERMAN, 2)),
    new EventDefinition(28, "Blaze Attack", Rarity.RARE,
        p -> spawn(p, EntityType.BLAZE, 3)),
    new EventDefinition(29, "Fire Resistance", Rarity.RARE,
        p -> effect(p, MobEffects.FIRE_RESISTANCE, 400, 0)),
    new EventDefinition(30, "Invisibility", Rarity.RARE,
        p -> effect(p, MobEffects.INVISIBILITY, 300, 0)),
    new EventDefinition(31, "Water Breathing", Rarity.RARE,
        p -> effect(p, MobEffects.WATER_BREATHING, 400, 0)),
    new EventDefinition(32, "Slow Falling", Rarity.RARE,
        p -> effect(p, MobEffects.SLOW_FALLING, 300, 0)),
    new EventDefinition(33, "Glowing Player", Rarity.RARE,
        p -> effect(p, MobEffects.GLOWING, 240, 0)),
    new EventDefinition(34, "Poison", Rarity.RARE,
        p -> effect(p, MobEffects.POISON, 160, 0)),
    new EventDefinition(35, "Mixed Mob Attack", Rarity.RARE,
        p -> {
            spawn(p, EntityType.ZOMBIE, 3);
            spawn(p, EntityType.SKELETON, 2);
            spawn(p, EntityType.SPIDER, 2);
        }),

    // EPIC: 36-45
    new EventDefinition(36, "Anvil Storm", Rarity.EPIC,
        p -> {
            effect(p, MobEffects.SLOWNESS, 100, 1);
            spawn(p, EntityType.IRON_GOLEM, 2);
            message(p, "§5Anvil Storm! Iron golems appeared!");
        }),
    new EventDefinition(37, "Slime King", Rarity.EPIC,
        p -> {
            spawn(p, EntityType.SLIME, 10);
            effect(p, MobEffects.JUMP_BOOST, 200, 2);
        }),
    new EventDefinition(38, "Thunderstorm", Rarity.EPIC,
        p -> {
            lightning(p);
            spawn(p, EntityType.CREEPER, 3);
            lightning(p);
        }),
    new EventDefinition(39, "Zombie Apocalypse", Rarity.EPIC,
        p -> spawn(p, EntityType.ZOMBIE, 12)),
    new EventDefinition(40, "Skeleton Army", Rarity.EPIC,
        p -> spawn(p, EntityType.SKELETON, 10)),
    new EventDefinition(41, "Darkness", Rarity.EPIC,
        p -> effect(p, MobEffects.DARKNESS, 240, 0)),
    new EventDefinition(42, "Random Prison", Rarity.EPIC,
        CursedEventManager::prison),
    new EventDefinition(43, "Fire Circle", Rarity.EPIC,
        CursedEventManager::fireCircle),
    new EventDefinition(44, "Spider Swarm", Rarity.EPIC,
        p -> spawn(p, EntityType.SPIDER, 12)),
    new EventDefinition(45, "Mob Arena", Rarity.EPIC,
        p -> {
            spawn(p, EntityType.ZOMBIE, 5);
            spawn(p, EntityType.SKELETON, 5);
            spawn(p, EntityType.SPIDER, 5);
        }),

    // LEGENDARY: 46-50
    new EventDefinition(46, "Creeper Apocalypse", Rarity.LEGENDARY,
        p -> spawn(p, EntityType.CREEPER, 10)),
    new EventDefinition(47, "Island of Chaos", Rarity.LEGENDARY,
        p -> {
            effect(p, MobEffects.LEVITATION, 60, 1);
            spawn(p, EntityType.PHANTOM, 5);
        }),
    new EventDefinition(48, "Boss Arena", Rarity.LEGENDARY,
        p -> {
            spawn(p, EntityType.RAVAGER, 2);
            spawn(p, EntityType.EVOKER, 1);
            spawn(p, EntityType.VINDICATOR, 4);
        }),
    new EventDefinition(49, "Lava Labyrinth", Rarity.LEGENDARY,
        p -> {
            effect(p, MobEffects.SLOWNESS, 160, 1);
            fireCircle(p);
            spawn(p, EntityType.BLAZE, 5);
        }),
    new EventDefinition(50, "SUPER CHAOS", Rarity.LEGENDARY,
        p -> {
            effect(p, MobEffects.NAUSEA, 200, 0);
            effect(p, MobEffects.SLOWNESS, 200, 1);
            spawn(p, EntityType.ZOMBIE, 5);
            spawn(p, EntityType.CREEPER, 4);
            spawn(p, EntityType.SKELETON, 5);
            spawn(p, EntityType.SPIDER, 5);
            lightning(p);
        })
);

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
    ServerLevel level = player.level();

    // Nether: 0.67%; End: 0.067%.
    if (level.dimension().equals(Level.OVERWORLD)) {
        double roll = RANDOM.nextDouble();

        if (roll < 0.0067) {
            teleportToDimension(player, Level.NETHER);
            return;
        }

        if (roll < 0.00737) {
            teleportToDimension(player, Level.END);
            return;
        }
    }

    EventDefinition selected = chooseEvent();
    if (selected == null) {
        return;
    }

    rememberEvent(selected.id());

    player.displayClientMessage(
        Component.literal(
            rarityColor(selected.rarity()) + "[" +
            selected.rarity().name() + "] " + selected.name()
        ),
        false
    );

    selected.action().run(player);
}

private EventDefinition chooseEvent() {
    List<EventDefinition> available = new ArrayList<>();

    for (EventDefinition def : EVENTS) {
        if (RECENT_EVENTS.contains(def.id())) {
            continue;
        }

        // Speed and Super Jump cannot occur in consecutive cycles.
        if ((def.id() == 1 || def.id() == 5)
                && !RECENT_EVENTS.isEmpty()
                && (RECENT_EVENTS.peekLast() == 1
                    || RECENT_EVENTS.peekLast() == 5)) {
            continue;
        }

        available.add(def);
    }

    if (available.isEmpty()) {
        RECENT_EVENTS.clear();
        available.addAll(EVENTS);
    }

    // Common 60%, Rare 25%, Epic 14.33%, Legendary 0.67%.
    double roll = RANDOM.nextDouble();
    Rarity chosenRarity;

    if (roll < 0.60) {
        chosenRarity = Rarity.COMMON;
    } else if (roll < 0.85) {
        chosenRarity = Rarity.RARE;
    } else if (roll < 0.9933) {
        chosenRarity = Rarity.EPIC;
    } else {
        chosenRarity = Rarity.LEGENDARY;
    }

    List<EventDefinition> category = new ArrayList<>();

    for (EventDefinition def : available) {
        if (def.rarity() == chosenRarity) {
            category.add(def);
        }
    }

    if (category.isEmpty()) {
        category = available;
    }

    return category.get(RANDOM.nextInt(category.size()));
}

private void rememberEvent(int id) {
    RECENT_EVENTS.addLast(id);

    while (RECENT_EVENTS.size() > MAX_RECENT_EVENTS) {
        RECENT_EVENTS.removeFirst();
    }
}

private static String rarityColor(Rarity rarity) {
    return switch (rarity) {
        case COMMON -> "§a";
        case RARE -> "§9";
        case EPIC -> "§5";
        case LEGENDARY -> "§c§l";
    };
}

private static void message(ServerPlayer player, String text) {
    player.displayClientMessage(Component.literal(text), false);
}

private static void effect(
        ServerPlayer player,
        Holder<MobEffect> effect,
        int duration,
        int amplifier) {
    player.addEffect(new MobEffectInstance(effect, duration, amplifier));
}

private static void spawn(
        ServerPlayer player,
        EntityType<?> type,
        int count) {
    spawnMobs(
        (ServerLevel) player.level(),
        type,
        player.blockPosition(),
        count
    );
}

private static void lightning(ServerPlayer player) {
    ServerLevel level = (ServerLevel) player.level();
    BlockPos pos = player.blockPosition().offset(
        RANDOM.nextInt(9) - 4,
        0,
        RANDOM.nextInt(9) - 4
    );

    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(
        level, EntitySpawnReason.EVENT
    );

    if (bolt != null) {
        bolt.setPos(
            pos.getX() + 0.5,
            pos.getY(),
            pos.getZ() + 0.5
        );
        level.addFreshEntity(bolt);
    }
}

private static void prison(ServerPlayer player) {
    ServerLevel level = (ServerLevel) player.level();
    BlockPos center = player.blockPosition();

    for (int dx = -1; dx <= 1; dx++) {
        for (int dz = -1; dz <= 1; dz++) {
            level.setBlockAndUpdate(
                center.offset(dx, 0, dz),
                Blocks.IRON_BARS.defaultBlockState()
            );
            level.setBlockAndUpdate(
                center.offset(dx, 1, dz),
                Blocks.IRON_BARS.defaultBlockState()
            );
        }
    }

    level.setBlockAndUpdate(
        center.below(),
        Blocks.IRON_BLOCK.defaultBlockState()
    );

    message(player, "§5You have been imprisoned!");
}

private static void fireCircle(ServerPlayer player) {
    ServerLevel level = (ServerLevel) player.level();
    BlockPos center = player.blockPosition();

    for (int i = 0; i < 12; i++) {
        double angle = (Math.PI * 2 * i) / 12.0;

        int x = center.getX()
            + (int) Math.round(Math.cos(angle) * 4);
        int z = center.getZ()
            + (int) Math.round(Math.sin(angle) * 4);

        BlockPos firePos = new BlockPos(x, center.getY(), z);

        if (level.getBlockState(firePos).isAir()
                && level.getBlockState(firePos.below()).isSolid()) {
            level.setBlockAndUpdate(
                firePos,
                Blocks.FIRE.defaultBlockState()
            );
        }
    }
}

private static void spawnMobs(
        ServerLevel level,
        EntityType<?> type,
        BlockPos center,
        int count) {

    for (int i = 0; i < count; i++) {
        var mob = type.create(level, EntitySpawnReason.EVENT);

        if (mob != null) {
            mob.setPos(
                center.getX() + RANDOM.nextInt(9) - 4 + 0.5,
                center.getY() + 1,
                center.getZ() + RANDOM.nextInt(9) - 4 + 0.5
            );
            level.addFreshEntity(mob);
        }
    }
}

private void teleportToDimension(
        ServerPlayer player,
        ResourceKey<Level> targetDimension) {

    MinecraftServer server = player.level().getServer();
    if (server == null) {
        return;
    }

    ServerLevel destination = server.getLevel(targetDimension);
    if (destination == null) {
        return;
    }

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
            safe = findSafePosition(destination, 0, 0);
        }

        if (safe == null) {
            RETURN_POINTS.remove(player.getUUID());
            message(player, "Could not find a safe Nether location!");
            return;
        }
    } else {
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
        player.getXRot(),
        true
    );

    message(
        player,
        targetDimension.equals(Level.NETHER)
            ? "§c§lRARE EVENT: WELCOME TO THE NETHER!"
            : "§5§lULTRA RARE EVENT: WELCOME TO THE END!"
    );
}

private BlockPos findSafePosition(
        ServerLevel level,
        int x,
        int z) {

    for (int radius = 0; radius <= 8; radius++) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                    continue;
                }

                for (int y = 100; y >= 32; y--) {
                    BlockPos pos = new BlockPos(x + dx, y, z + dz);

                    if (level.getBlockState(pos).isAir()
                            && level.getBlockState(pos.above()).isAir()
                            && level.getBlockState(pos.below()).isSolid()
                            && level.getBlockState(pos.below())
                                .getFluidState().isEmpty()) {
                        return pos;
                    }
                }
            }
        }
    }

    return null;
}

@SubscribeEvent
public void onPlayerRespawn(PlayerRespawnEvent event) {
    if (!(event.getEntity() instanceof ServerPlayer player)) {
        return;
    }

    ReturnPoint point = RETURN_POINTS.remove(player.getUUID());
    if (point == null) {
        return;
    }

    MinecraftServer server = player.level().getServer();
    if (server == null) {
        return;
    }

    ServerLevel destination = server.getLevel(point.dimension());
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
        point.pitch(),
        true
    );
}

                 }
