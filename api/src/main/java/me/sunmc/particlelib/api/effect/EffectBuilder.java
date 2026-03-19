package me.sunmc.particlelib.api.effect;

import me.sunmc.particlelib.api.particle.ParticleOptions;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract fluent builder for all effects.
 *
 * <p>Each concrete effect exposes its own {@code Builder} extending this class.
 * Call {@link #build()} to get the immutable {@link Effect}, or
 * {@link #playAt(Location)} as a one-shot shortcut.</p>
 *
 * @param <B> concrete builder type (self-referential for chaining)
 */
public abstract class EffectBuilder<B extends EffectBuilder<B>> {

    public EffectType type = EffectType.REPEATING;
    public int delay = 0;
    public int period = 1;
    public int iterations = 20;   // -1 = infinite
    @Nullable
    public Integer duration = null;  // ms; overrides iterations if set

    /**
     * If {@code true} the tick loop runs on an async worker thread.
     * Set to {@code false} for effects that access world/entity state.
     */
    public boolean asynchronous = true;

    protected @NotNull Particle particle = Particle.FLAME;
    protected @Nullable Color color = null;
    protected @Nullable Color toColor = null;
    protected float particleSize = 1.0f;
    protected float speed = 0.0f;
    protected int particleCount = 1;
    protected float offsetX = 0, offsetY = 0, offsetZ = 0;
    protected boolean forceShow = false;

    public double visibleRange = 32.0;
    public double probability = 1.0;
    protected @Nullable List<Player> targetPlayers = null;
    @NotNull
    public String id = "unnamed";

    protected abstract @NotNull B self();

    /**
     * Builds and returns the configured {@link Effect}.
     */
    public abstract @NotNull Effect build();

    public @NotNull B type(@NotNull EffectType t) {
        type = Objects.requireNonNull(t);
        return self();
    }

    public @NotNull B instant() {
        return type(EffectType.INSTANT);
    }

    public @NotNull B delayed(int delayTicks) {
        type = EffectType.DELAYED;
        delay = delayTicks;
        return self();
    }

    public @NotNull B repeating(int periodTicks) {
        type = EffectType.REPEATING;
        period = periodTicks;
        return self();
    }

    public @NotNull B delay(int ticks) {
        delay = ticks;
        return self();
    }

    public @NotNull B period(int ticks) {
        period = Math.max(1, ticks);
        return self();
    }

    public @NotNull B iterations(int n) {
        iterations = n;
        return self();
    }

    public @NotNull B infinite() {
        iterations = -1;
        return self();
    }

    public @NotNull B duration(int millis) {
        duration = millis;
        return self();
    }

    /**
     * Controls whether the tick loop runs asynchronously.
     *
     * <p>Default is {@code true} for all particle effects — math is cheap
     * and thread-safe. Set to {@code false} only for effects that must
     * access world/entity state on the region thread (e.g. BleedEffect,
     * ExplodeEffect, SoundEffect).</p>
     */
    public @NotNull B async(boolean a) {
        asynchronous = a;
        return self();
    }

    public @NotNull B sync() {
        return async(false);
    }

    public @NotNull B particle(@NotNull Particle p) {
        particle = Objects.requireNonNull(p);
        return self();
    }

    public @NotNull B color(@Nullable Color c) {
        color = c;
        return self();
    }

    public @NotNull B toColor(@Nullable Color c) {
        toColor = c;
        return self();
    }

    public @NotNull B particleSize(float s) {
        particleSize = s;
        return self();
    }

    public @NotNull B speed(float s) {
        speed = s;
        return self();
    }

    public @NotNull B particleCount(int n) {
        particleCount = n;
        return self();
    }

    public @NotNull B offset(float x, float y, float z) {
        offsetX = x;
        offsetY = y;
        offsetZ = z;
        return self();
    }

    public @NotNull B offset(float uniform) {
        return offset(uniform, uniform, uniform);
    }

    public @NotNull B forceShow(boolean force) {
        forceShow = force;
        return self();
    }

    public @NotNull B visibleRange(double r) {
        visibleRange = r;
        return self();
    }

    public @NotNull B probability(double p) {
        probability = Math.max(0, Math.min(1, p));
        return self();
    }

    public @NotNull B targetPlayers(@NotNull List<Player> players) {
        targetPlayers = new ArrayList<>(players);
        return self();
    }

    public @NotNull B id(@NotNull String effectId) {
        id = Objects.requireNonNull(effectId);
        return self();
    }

    /**
     * Builds the {@link ParticleOptions} record from current state.
     */
    public @NotNull ParticleOptions buildParticleOptions() {
        return new ParticleOptions(
                particle, color, toColor, particleSize,
                speed, particleCount,
                offsetX, offsetY, offsetZ,
                forceShow, null, null
        );
    }

    /**
     * Shortcut: builds and plays the effect immediately at {@code location}.
     *
     * @return the effect handle
     */
    public @NotNull EffectHandle playAt(@NotNull Location location) {
        return build().play(EffectContext.at(location));
    }

    /**
     * Shortcut: builds and plays the effect for a specific player only.
     */
    public @NotNull EffectHandle playFor(@NotNull Location location, @NotNull Player player) {
        return build().play(EffectContext.forPlayer(location, player));
    }
}