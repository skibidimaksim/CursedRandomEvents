
package com.skibidimaksim.cursedrandomevents;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class CursedConfig {
    private static final Path FILE =
            FMLPaths.CONFIGDIR.get().resolve("cursedrandomevents.properties");

    private static final Properties DATA = new Properties();

    public static int intervalSeconds = 30;
    public static double netherChance = 0.67;
    public static double endChance = 0.067;
    public static boolean eventsEnabled = true;

    private CursedConfig() {}

    public static void load() {
        if (Files.exists(FILE)) {
            try (InputStream in = Files.newInputStream(FILE)) {
                DATA.load(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        intervalSeconds = readInt("intervalSeconds", 30, 5, 3600);
        netherChance = readDouble("netherChance", 0.67, 0, 100);
        endChance = readDouble("endChance", 0.067, 0, 100);
        eventsEnabled = Boolean.parseBoolean(
                DATA.getProperty("eventsEnabled", "true")
        );
    }

    public static void save() {
        DATA.setProperty("intervalSeconds", String.valueOf(intervalSeconds));
        DATA.setProperty("netherChance", String.valueOf(netherChance));
        DATA.setProperty("endChance", String.valueOf(endChance));
        DATA.setProperty("eventsEnabled", String.valueOf(eventsEnabled));

        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                DATA.store(out, "Cursed Random Events settings");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int readInt(
            String key, int fallback, int min, int max) {
        try {
            return Math.max(min, Math.min(max,
                    Integer.parseInt(DATA.getProperty(
                            key, String.valueOf(fallback)))));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double readDouble(
            String key, double fallback, double min, double max) {
        try {
            return Math.max(min, Math.min(max,
                    Double.parseDouble(DATA.getProperty(
                            key, String.valueOf(fallback)))));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
