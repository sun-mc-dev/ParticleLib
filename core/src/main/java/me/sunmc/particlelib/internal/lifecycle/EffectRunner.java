package me.sunmc.particlelib.internal.lifecycle;

import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectHandle;
import me.sunmc.particlelib.effects.AbstractEffect;
import me.sunmc.particlelib.internal.particle.PaperParticleSpawner;
import me.sunmc.particlelib.internal.scheduler.PlatformScheduler;
import me.sunmc.particlelib.internal.scheduler.TaskHandle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Manages the scheduling lifecycle for a single effect invocation.
 *
 * <p>One runner is created per {@link AbstractEffect#play} call. It schedules
 * the appropriate task via {@link PlatformScheduler}, drives the tick loop,
 * resolves the recipient list once per tick, and handles cancel/pause/resume.</p>
 *
 * <h3>Fixes vs previous version</h3>
 * <ul>
 *   <li><b>Recipient resolution</b> — called once per tick via
 *       {@link AbstractEffect#beginTick}/{@link AbstractEffect#endTick},
 *       not once per particle.</li>
 *   <li><b>Folia sync-repeating</b> — effects with {@code asynchronous=false}
 *       that have an origin location now use {@code runAtLocation} for the
 *       initial run and {@code GlobalRegionScheduler} for repeating (Folia
 *       does not expose a region-bound repeating scheduler in the public API,
 *       so GlobalRegionScheduler is the correct choice for sync repeating).</li>
 *   <li><b>No leaked cleanup task</b> — the old approach scheduled a permanent
 *       repeating async task per effect that never cancelled itself. Now the
 *       runner registers a {@link Runnable callback} with the effect's
 *       {@code onDone} that removes it from {@link EffectManager} directly.</li>
 * </ul>
 */
public final class EffectRunner {

    private final AbstractEffect effect;
    private final EffectContext ctx;
    private final Plugin plugin;

    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicBoolean done = new AtomicBoolean(false);
    private final AtomicInteger iteration = new AtomicInteger(0);

    /**
     * Underlying scheduler task — cancelled on {@link #cancel()}.
     */
    private volatile TaskHandle taskHandle = TaskHandle.noop();


    public EffectRunner(@NotNull AbstractEffect effect,
                        @NotNull EffectContext ctx,
                        @NotNull Plugin plugin) {
        this.effect = effect;
        this.ctx = ctx;
        this.plugin = plugin;
    }

    /**
     * Schedules the effect and returns its handle. Call exactly once.
     */
    public @NotNull EffectHandle start() {
        taskHandle = switch (effect.type()) {
            case INSTANT -> scheduleInstant();
            case DELAYED -> scheduleDelayed();
            case REPEATING -> scheduleRepeating();
        };
        return new ActiveEffectHandle(this);
    }

    /**
     * Called on every scheduled tick.
     *
     * <ol>
     *   <li>Skips if done or paused.</li>
     *   <li>Applies probability check.</li>
     *   <li>Resolves recipients <em>once</em> and sets them on the effect.</li>
     *   <li>Calls {@link AbstractEffect#onTick}.</li>
     *   <li>Advances the iteration counter and checks the limit.</li>
     * </ol>
     */
    private void tick() {
        if (done.get() || paused.get()) return;

        // Probability gate
        double prob = effect.probability();
        if (prob < 1.0 && ThreadLocalRandom.current().nextDouble() >= prob) {
            advanceIteration();
            return;
        }

        // Resolve recipient list once for the whole tick
        List<Player> recipients = PaperParticleSpawner.getInstance()
                .resolveRecipients(ctx.origin(), effect.visibleRange, ctx.targetPlayers());

        try {
            effect.beginTick(recipients);   // sets tick-scoped cache
            effect.onTick(ctx, iteration.get());
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING,
                    "[ParticleLib] Error in effect '" + effect.id() + "' on tick "
                            + iteration.get(), ex);
            cancel();
            return;
        } finally {
            effect.endTick();               // always clear cache
        }

        advanceIteration();
    }

    private void runOnce() {
        tick();
        if (!done.get()) markDone();
    }

    private void advanceIteration() {
        int i = iteration.incrementAndGet();
        int max = resolveMaxIterations();
        if (max > 0 && i >= max) markDone();
    }

    private TaskHandle scheduleInstant() {
        return effect.asynchronous()
                ? PlatformScheduler.runAsync(plugin, this::runOnce)
                : PlatformScheduler.runAtLocation(plugin, ctx.origin(), this::runOnce);
    }

    private TaskHandle scheduleDelayed() {
        return effect.asynchronous()
                ? PlatformScheduler.runAsyncDelayed(plugin, this::runOnce, effect.delay())
                : PlatformScheduler.runDelayedAtLocation(plugin, ctx.origin(), this::runOnce, effect.delay());
    }

    private TaskHandle scheduleRepeating() {
        if (effect.asynchronous()) {
            return PlatformScheduler.runAsyncRepeating(
                    plugin, this::tick, effect.delay(), effect.period());
        } else {
            return PlatformScheduler.runSyncRepeating(
                    plugin, this::tick, effect.delay(), effect.period());
        }
    }

    public void cancel() {
        if (done.compareAndSet(false, true)) {
            taskHandle.cancel();
            try {
                effect.onDone(ctx);
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING,
                        "[ParticleLib] Error in effect.onDone() for '" + effect.id() + "'", ex);
            }
        }
    }

    public void pause() {
        paused.set(true);
    }

    public void resume() {
        paused.set(false);
    }

    public boolean isPaused() {
        return paused.get();
    }

    public boolean isDone() {
        return done.get();
    }

    private void markDone() {
        cancel();
    }

    private int resolveMaxIterations() {
        Integer dur = effect.duration();
        if (dur != null && dur > 0) {
            // Convert millisecond duration to ticks, then to iterations at the
            // configured period. period is in ticks; 50 ms/tick.
            int periodMs = Math.max(1, effect.period()) * 50;
            return Math.max(1, dur / periodMs);
        }
        return effect.iterations(); // -1 = infinite (> 0 check handles that)
    }
}