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

    private double defaultVisibleRange = 32.0;
    private boolean forceShowParticles = false;
    private int maxActiveEffects = 500;
    private boolean respectClientRender = true;
    private int minRenderDistanceChunks = 4;
    private int defaultPeriod = 1;
    private int defaultIterations = 100;
    private double defaultProbability = 1.0;

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

        defaultVisibleRange = c.getDouble("default-visible-range", 32.0);
        forceShowParticles = c.getBoolean("force-show-particles", false);
        maxActiveEffects = c.getInt("performance.max-active-effects", 500);
        respectClientRender = c.getBoolean("performance.respect-client-render-distance", true);
        minRenderDistanceChunks = c.getInt("performance.min-render-distance-chunks", 4);
        defaultPeriod = c.getInt("effect-defaults.period", 1);
        defaultIterations = c.getInt("effect-defaults.iterations", 100);
        defaultProbability = c.getDouble("effect-defaults.probability", 1.0);
    }

    /**
     * Default visible range in blocks applied to all new {@link me.sunmc.particlelib.api.effect.EffectBuilder} instances.
     */
    public double defaultVisibleRange() {
        return defaultVisibleRange;
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

    /**
     * Default period (ticks) used when a config-driven effect omits the key.
     */
    public int defaultPeriod() {
        return defaultPeriod;
    }

    /**
     * Default iteration count used when a config-driven effect omits the key.
     */
    public int defaultIterations() {
        return defaultIterations;
    }

    /**
     * Default probability (0–1) used when a config-driven effect omits the key.
     */
    public double defaultProbability() {
        return defaultProbability;
    }
}