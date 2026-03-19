package me.sunmc.particlelib.effects;

import me.sunmc.particlelib.ParticleLib;
import me.sunmc.particlelib.api.effect.*;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.api.particle.ParticleOptions;
import me.sunmc.particlelib.internal.lifecycle.EffectRunner;
import me.sunmc.particlelib.internal.particle.PaperParticleSpawner;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Base class for all ParticleLib effects.
 *
 * <h3>Implementing a new effect</h3>
 * <ol>
 *   <li>Extend {@code AbstractEffect}.</li>
 *   <li>Declare a private inner {@code Builder} extending {@link EffectBuilder}.</li>
 *   <li>Implement {@link #onTick(EffectContext, int)}.</li>
 *   <li>Use the {@code display*()} helpers — never call the Paper API directly.</li>
 * </ol>
 *
 * <h3>Thread safety</h3>
 * <p>Instances are effectively immutable after construction — all parameters
 * come from the builder and are never mutated after that.  The same
 * {@code AbstractEffect} can therefore be played concurrently multiple times
 * without shared mutable state (each play creates its own {@link EffectRunner}
 * with its own iteration counter).</p>
 *
 * <h3>Per-tick recipient cache</h3>
 * <p>{@link EffectRunner} calls {@link #beginTick(List)} once before
 * {@link #onTick} and {@link #endTick()} once after, bracketing the tick so
 * that all {@code display*()} calls within a single tick reuse the same
 * pre-resolved recipient list instead of rescanning world players per particle.</p>
 */
public abstract class AbstractEffect implements Effect {

    protected final String id;
    protected final EffectType type;
    protected final int delay;
    protected final int period;
    protected final int iterations;      // -1 = infinite
    protected final @Nullable Integer duration;      // ms; overrides iterations
    protected final double probability;
    protected final boolean asynchronous;
    protected final @NotNull ParticleOptions baseParticleOptions;
    public final double visibleRange;

    /**
     * Pre-resolved recipient list for the current tick.
     * Set by {@link #beginTick(List)} / cleared by {@link #endTick()}.
     * {@code null} between ticks.
     */
    private @Nullable List<Player> tickRecipients = null;

    protected AbstractEffect(@NotNull EffectBuilder<?> b) {
        this.id = b.id;
        this.type = b.type;
        this.delay = b.delay;
        this.period = Math.max(1, b.period);
        this.iterations = b.iterations;
        this.duration = b.duration;
        this.probability = b.probability;
        this.asynchronous = b.asynchronous;
        this.baseParticleOptions = b.buildParticleOptions();
        this.visibleRange = b.visibleRange;
    }

    @Override
    public final @NotNull String id() {
        return id;
    }

    @Override
    public final @NotNull EffectType type() {
        return type;
    }

    /**
     * Plays this effect and returns a cancellable handle.
     * Safe to call concurrently — each call creates an independent runner.
     */
    @Override
    public final @NotNull EffectHandle play(@NotNull EffectContext ctx) {
        Plugin plugin = ParticleLib.getInstance();
        return new EffectRunner(this, ctx, plugin).start();
    }

    /**
     * Called by {@link EffectRunner} immediately before {@link #onTick}.
     * Sets the pre-resolved recipient list for this tick.
     */
    public final void beginTick(@NotNull List<Player> recipients) {
        this.tickRecipients = recipients;
    }

    /**
     * Called by {@link EffectRunner} immediately after {@link #onTick}.
     * Clears the recipient reference, so it can be GC'd between ticks.
     */
    public final void endTick() {
        this.tickRecipients = null;
    }

    /**
     * Called on every scheduled tick. Subclasses implement particle math here.
     *
     * <p>May be called from an async worker thread (when {@link #asynchronous}
     * is {@code true}) or from the region/main thread (when {@code false}).
     * Do not block. Do not call Bukkit APIs that are not thread-safe unless
     * {@code asynchronous} is {@code false}.</p>
     *
     * @param ctx       play context: origin, target, player list, etc.
     * @param iteration zero-based tick counter, reset to 0 for each new play
     */
    public abstract void onTick(@NotNull EffectContext ctx, int iteration);

    /**
     * Called when the effect finishes (naturally or via cancel). Default no-op.
     * Override for cleanup (e.g. clearing caches, resetting state).
     */
    public void onDone(@NotNull EffectContext ctx) {
    }

    public int delay() {
        return delay;
    }

    public int period() {
        return period;
    }

    public int iterations() {
        return iterations;
    }

    public @Nullable Integer duration() {
        return duration;
    }

    public double probability() {
        return probability;
    }

    public boolean asynchronous() {
        return asynchronous;
    }

    /**
     * Displays the base particle at {@code location}.
     *
     * <p>Uses the tick-cached recipient list resolved by {@link EffectRunner}
     * before {@link #onTick} was called — no per-call distance scan.</p>
     */
    protected void display(@NotNull EffectContext ctx, @NotNull Location location) {
        display(ctx, location, baseParticleOptions);
    }

    /**
     * Displays a particle at {@code location} with custom {@link ParticleOptions}.
     */
    protected void display(@NotNull EffectContext ctx,
                           @NotNull Location location,
                           @NotNull ParticleOptions opts) {
        List<Player> recipients = getTickRecipients(ctx, location);
        PaperParticleSpawner.getInstance().spawn(opts, location, recipients);
    }

    /**
     * Displays the base particle at an offset from the effect's origin.
     *
     * @param offset world-space offset added to {@code ctx.origin()}
     */
    protected void displayAt(@NotNull EffectContext ctx, @NotNull Vec3 offset) {
        Location loc = ctx.origin().clone().add(offset.x(), offset.y(), offset.z());
        display(ctx, loc);
    }

    /**
     * Displays the base particle at an absolute world position.
     *
     * @param pos absolute position; must be in the same world as {@code ctx.origin()}
     */
    protected void displayAbsolute(@NotNull EffectContext ctx, @NotNull Vec3 pos) {
        World world = ctx.world();
        Location loc = pos.toLocation(world);
        display(ctx, loc);
    }

    /**
     * Displays with an explicit color override (dust / entity_effect particles).
     */
    protected void displayColored(@NotNull EffectContext ctx,
                                  @NotNull Location location,
                                  @NotNull Color color) {
        display(ctx, location, baseParticleOptions.withColor(color));
    }

    /**
     * Rotates {@code localOffset} by the origin's yaw/pitch, then adds it to
     * the origin to produce a spawn {@link Location}.
     *
     * <p>Use this for effects that should face the direction an entity is looking.</p>
     */
    protected @NotNull Location applyRotation(@NotNull EffectContext ctx,
                                              @NotNull Vec3 localOffset) {
        Location origin = ctx.origin();
        Vec3 rotated = localOffset.rotateByYawPitch(origin.getYaw(), origin.getPitch());
        return origin.clone().add(rotated.x(), rotated.y(), rotated.z());
    }

    /**
     * Picks a random color from {@code list}, or returns {@code fallback}
     * if the list is null or empty.
     */
    protected @Nullable Color pickColor(@Nullable List<Color> list,
                                        @Nullable Color fallback) {
        if (list == null || list.isEmpty()) return fallback;
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    /**
     * Returns the tick-cached recipients if available, otherwise resolves them
     * on the spot (fallback for display calls made outside the normal tick loop).
     */
    private @NotNull List<Player> getTickRecipients(@NotNull EffectContext ctx,
                                                    @NotNull Location location) {
        if (tickRecipients != null) return tickRecipients;
        // Fallback — shouldn't normally happen, but safe
        return PaperParticleSpawner.getInstance()
                .resolveRecipients(location, visibleRange, ctx.targetPlayers());
    }
}