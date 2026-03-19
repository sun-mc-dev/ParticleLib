package me.sunmc.particlelib.internal.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

/**
 * Unified scheduler for Paper 1.21+ and Folia.
 *
 * <h3>Platform detection</h3>
 * <ul>
 *   <li><b>Folia</b> — detected via
 *       {@code io.papermc.paper.threadedregions.RegionizedServer}.
 *       Region-bound calls use {@code RegionScheduler} for thread-safe
 *       world/block access.</li>
 *   <li><b>Paper 1.21+ (non-Folia)</b> — detected via
 *       {@code Bukkit#getAsyncScheduler()}. All "region" calls delegate to
 *       {@code GlobalRegionScheduler} which runs on the main thread.</li>
 * </ul>
 *
 * <p>This library targets Paper 1.21+ only — no Spigot legacy fallback is
 * provided. If you need Spigot support, use the original EffectLib instead.</p>
 */
public final class PlatformScheduler {

    private enum Platform {FOLIA, PAPER}

    private static final Platform PLATFORM = detect();

    private static Platform detect() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return Platform.FOLIA;
        } catch (ClassNotFoundException ignored) {
            return Platform.PAPER;
        }
    }

    public static boolean isFolia() {
        return PLATFORM == Platform.FOLIA;
    }

    /**
     * Runs {@code task} on an async worker thread immediately.
     * Safe to call from any thread on both Paper and Folia.
     */
    public static @NotNull TaskHandle runAsync(@NotNull Plugin plugin, @NotNull Runnable task) {
        var scheduled = Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run());
        return scheduled::cancel;
    }

    /**
     * Runs {@code task} on an async worker thread after {@code delayTicks} ticks.
     */
    public static @NotNull TaskHandle runAsyncDelayed(@NotNull Plugin plugin,
                                                      @NotNull Runnable task,
                                                      long delayTicks) {
        long ms = Math.max(delayTicks * 50L, 1L);
        var scheduled = Bukkit.getAsyncScheduler()
                .runDelayed(plugin, t -> task.run(), ms, TimeUnit.MILLISECONDS);
        return scheduled::cancel;
    }

    /**
     * Runs {@code task} on an async worker thread at a fixed rate.
     */
    public static @NotNull TaskHandle runAsyncRepeating(@NotNull Plugin plugin,
                                                        @NotNull Runnable task,
                                                        long initialDelayTicks,
                                                        long periodTicks) {
        long initMs = Math.max(initialDelayTicks * 50L, 1L);
        long periodMs = Math.max(periodTicks * 50L, 1L);
        var scheduled = Bukkit.getAsyncScheduler()
                .runAtFixedRate(plugin, t -> task.run(), initMs, periodMs, TimeUnit.MILLISECONDS);
        return scheduled::cancel;
    }

    /**
     * Runs {@code task} on the global region (main thread on Paper, global
     * region on Folia). Safe for non-world-data work (e.g. callbacks, events).
     */
    public static @NotNull TaskHandle runSync(@NotNull Plugin plugin, @NotNull Runnable task) {
        var scheduled = Bukkit.getGlobalRegionScheduler().run(plugin, t -> task.run());
        return scheduled::cancel;
    }

    /**
     * Runs {@code task} on the global region after {@code delayTicks} ticks.
     */
    public static @NotNull TaskHandle runSyncDelayed(@NotNull Plugin plugin,
                                                     @NotNull Runnable task,
                                                     long delayTicks) {
        var scheduled = Bukkit.getGlobalRegionScheduler()
                .runDelayed(plugin, t -> task.run(), delayTicks);
        return scheduled::cancel;
    }

    /**
     * Runs {@code task} on the global region at a fixed rate.
     */
    public static @NotNull TaskHandle runSyncRepeating(@NotNull Plugin plugin,
                                                       @NotNull Runnable task,
                                                       long initialDelayTicks,
                                                       long periodTicks) {
        var scheduled = Bukkit.getGlobalRegionScheduler()
                .runAtFixedRate(plugin, t -> task.run(),
                        Math.max(1, initialDelayTicks),
                        Math.max(1, periodTicks));
        return scheduled::cancel;
    }

    /**
     * Runs {@code task} on the region thread owning {@code location}.
     *
     * <ul>
     *   <li>On <b>Folia</b>: dispatches to the correct region thread — safe
     *       for block/entity access at that location.</li>
     *   <li>On <b>Paper</b>: {@code RegionScheduler.run()} runs on the main
     *       thread, as expected.</li>
     * </ul>
     * <p>
     * Falls back to {@link #runSync} if location or its World is null.
     */
    public static @NotNull TaskHandle runAtLocation(@NotNull Plugin plugin,
                                                    @Nullable Location location,
                                                    @NotNull Runnable task) {
        if (location != null && location.getWorld() != null) {
            var scheduled = Bukkit.getRegionScheduler()
                    .run(plugin, location, t -> task.run());
            return scheduled::cancel;
        }
        return runSync(plugin, task);
    }

    /**
     * Runs {@code task} on the region owning {@code location} after
     * {@code delayTicks} ticks.
     */
    public static @NotNull TaskHandle runDelayedAtLocation(@NotNull Plugin plugin,
                                                           @Nullable Location location,
                                                           @NotNull Runnable task,
                                                           long delayTicks) {
        if (location != null && location.getWorld() != null) {
            var scheduled = Bukkit.getRegionScheduler()
                    .runDelayed(plugin, location, t -> task.run(), delayTicks);
            return scheduled::cancel;
        }
        return runSyncDelayed(plugin, task, delayTicks);
    }

    private PlatformScheduler() {
    }
}