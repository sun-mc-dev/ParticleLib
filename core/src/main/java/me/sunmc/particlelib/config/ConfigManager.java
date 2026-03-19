package me.sunmc.particlelib.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Loads and exposes ParticleLib's {@code config.yml} values as typed fields.
 *
 * <p>Call {@link #reload(Plugin)} on enable and on {@code /plib reload}.
 * All getter values reflect the most recently loaded config — callers do not
 * need to cache results themselves.</p>
 *
 * <p>In shaded mode the owning plugin is the consumer plugin, not ParticleLib
 * itself. {@code saveDefaultConfig()} will write ParticleLib's bundled
 * {@code config.yml} into the consumer's data folder on first run, which is
 * intentional — the consumer can then override values there.</p>
 */
public final class ConfigManager {

    private static final ConfigManager INSTANCE = new ConfigManager();

    public static @NotNull ConfigManager get() {
        return INSTANCE;
    }

    private boolean forceShowParticles = false;
    private int maxActiveEffects = 500;
    private boolean respectClientRender = true;
    private int minRenderDistanceChunks = 4;

    private ConfigManager() {
    }

    /**
     * Saves the default config if absent, then reloads all values from disk.
     *
     * @param plugin the owning plugin whose data folder holds {@code config.yml}
     */
    public void reload(@NotNull Plugin plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration c = plugin.getConfig();

        forceShowParticles = c.getBoolean("force-show-particles", false);
        maxActiveEffects = c.getInt("performance.max-active-effects", 500);
        respectClientRender = c.getBoolean("performance.respect-client-render-distance", true);
        minRenderDistanceChunks = c.getInt("performance.min-render-distance-chunks", 4);
    }

    /**
     * When {@code true}, all spawned particles bypass the default 32-block client visibility cap.
     */
    public boolean forceShowParticles() {
        return forceShowParticles;
    }

    /**
     * Maximum number of concurrently tracked effects. 0 = unlimited.
     */
    public int maxActiveEffects() {
        return maxActiveEffects;
    }

    /**
     * When {@code true}, players below {@link #minRenderDistanceChunks()} are excluded from recipients.
     */
    public boolean respectClientRender() {
        return respectClientRender;
    }

    /**
     * Minimum client render distance (in chunks) required to receive particles.
     */
    public int minRenderDistanceChunks() {
        return minRenderDistanceChunks;
    }
}