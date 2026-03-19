package me.sunmc.particlelib.internal.particle;

import me.sunmc.particlelib.api.particle.ParticleOptions;
import me.sunmc.particlelib.config.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Concrete particle spawner for Paper 1.21+.
 *
 * <h3>Optimization: tick-scoped player cache</h3>
 * <p>When an effect has many particles per tick (e.g. 50 sphere points),
 * naively calling {@code world.getPlayers()} and doing a distance check
 * for every single particle wastes CPU. Instead, callers should resolve
 * the visible player list <em>once per tick</em> via
 * {@link #resolveRecipients(Location, double, List)} and pass the result
 * to every {@link #spawn} call within that tick.</p>
 *
 * <p>This is exactly what {@link me.sunmc.particlelib.effects.AbstractEffect}
 * does — it calls {@code resolveRecipients} at the top of each tick and
 * passes the cached list down.</p>
 *
 * <h3>forceShow</h3>
 * <p>Uses the Paper 1.21.4+ {@code spawnParticle(..., boolean forceShow)}
 * overload, which bypasses the default 32-block client-side visibility cap.
 * The flag is set when either the per-effect {@link ParticleOptions#forceShow()}
 * is {@code true} <em>or</em> the global {@link ConfigManager#forceShowParticles()}
 * override is enabled.</p>
 *
 * <h3>Client render-distance filter</h3>
 * <p>When {@link ConfigManager#respectClientRender()} is {@code true}, players
 * whose client view distance is below {@link ConfigManager#minRenderDistanceChunks()}
 * are excluded from recipients, avoiding wasted packet sends to players who
 * would not render the particles anyway.</p>
 */
public final class PaperParticleSpawner {

    private static final PaperParticleSpawner INSTANCE = new PaperParticleSpawner();

    public static PaperParticleSpawner getInstance() {
        return INSTANCE;
    }

    private PaperParticleSpawner() {
    }

    /**
     * Resolves the list of players that should receive particles originating
     * near {@code origin}.
     *
     * <p>Returns {@code targetPlayers} unchanged if it is non-null (explicit
     * target list takes priority). Otherwise filters {@code world.getPlayers()}
     * by squared distance, optionally also by client render distance.</p>
     *
     * <p>Call this <strong>once at the top of each tick</strong> and pass the
     * result to every {@link #spawn} call within that tick to avoid redundant
     * distance checks.</p>
     *
     * @param origin        the central location of the effect this tick
     * @param visibleRange  radius in blocks; players beyond this won't receive packets
     * @param targetPlayers explicit list override, or {@code null} for broadcast
     * @return non-null list of recipients (may be empty)
     */
    public @NotNull List<Player> resolveRecipients(@NotNull Location origin,
                                                   double visibleRange,
                                                   @Nullable List<Player> targetPlayers) {
        if (targetPlayers != null) return targetPlayers;

        World world = Objects.requireNonNull(origin.getWorld(), "origin must have a World");
        double rangeSq = visibleRange * visibleRange;
        ConfigManager cfg = ConfigManager.get();
        int minChunks = cfg.minRenderDistanceChunks();

        List<Player> result = new ArrayList<>();
        for (Player p : world.getPlayers()) {
            if (cfg.respectClientRender()
                    && p.getClientViewDistance() < minChunks) continue;
            if (p.getLocation().distanceSquared(origin) <= rangeSq) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Spawns a particle for each player in {@code recipients}.
     *
     * <p>Prefer calling {@link #resolveRecipients} once per tick and passing
     * the result here, rather than letting this method re-resolve every call.</p>
     *
     * @param opts       particle options (particle type, color, count, offsets…)
     * @param location   where to spawn the particle
     * @param recipients pre-resolved list from {@link #resolveRecipients}
     */
    public void spawn(@NotNull ParticleOptions opts,
                      @NotNull Location location,
                      @NotNull List<Player> recipients) {
        if (recipients.isEmpty()) return;
        Object data = ParticleDataResolver.resolve(opts);
        // Global forceShow override from config takes precedence over per-effect flag.
        boolean force = opts.forceShow() || ConfigManager.get().forceShowParticles();
        Particle particle = opts.particle();
        for (Player p : recipients) {
            spawnForPlayer(p, particle, location, opts, data, force);
        }
    }

    /**
     * Convenience overload that resolves recipients internally.
     * Use this only when you have a single particle to spawn per call.
     * For multi-particle effects, use {@link #resolveRecipients} + {@link #spawn}.
     */
    public void spawn(@NotNull ParticleOptions opts,
                      @NotNull Location location,
                      @Nullable List<Player> targetPlayers,
                      double visibleRange) {
        spawn(opts, location, resolveRecipients(location, visibleRange, targetPlayers));
    }

    private void spawnForPlayer(@NotNull Player p,
                                @NotNull Particle particle,
                                @NotNull Location loc,
                                @NotNull ParticleOptions opts,
                                @Nullable Object data,
                                boolean force) {
        try {
            p.spawnParticle(particle, loc,
                    opts.count(),
                    opts.offsetX(), opts.offsetY(), opts.offsetZ(),
                    opts.speed(), data, force);
        } catch (Exception ignored) {
            // Bad data for this particle type — skip silently.
        }
    }
}