package me.sunmc.particlelib.api.particle;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Immutable description of a single particle spawn call.
 *
 * <p>Use the static factory {@link #of(Particle)} to start, then chain
 * {@code with*} methods — each returns a new instance.</p>
 *
 * <pre>{@code
 * ParticleOptions opts = ParticleOptions.of(Particle.DUST)
 *     .withColor(Color.RED)
 *     .withSize(1.5f)
 *     .withCount(3);
 * }</pre>
 */
public record ParticleOptions(
        @NotNull Particle particle,
        @Nullable Color color,
        @Nullable Color toColor,
        float size,
        float speed,
        int count,
        float offsetX,
        float offsetY,
        float offsetZ,
        boolean forceShow,
        @Nullable Material material,
        @Nullable String blockData
) {

    public ParticleOptions {
        Objects.requireNonNull(particle, "particle must not be null");
        if (count < 0) throw new IllegalArgumentException("count must be >= 0");
        if (size <= 0) throw new IllegalArgumentException("size must be > 0");
        if (speed < 0) throw new IllegalArgumentException("speed must be >= 0");
    }

    /**
     * Creates options with sensible defaults: count=1, size=1, speed=0.
     */
    public static @NotNull ParticleOptions of(@NotNull Particle particle) {
        return new ParticleOptions(particle, null, null, 1f, 0f,
                1, 0f, 0f, 0f, false, null, null);
    }

    /**
     * Preset for DUST particles with a color.
     */
    public static @NotNull ParticleOptions dust(@NotNull Color color, float size) {
        return of(Particle.DUST).withColor(color).withSize(size);
    }

    /**
     * Preset for DUST_COLOR_TRANSITION particles.
     */
    public static @NotNull ParticleOptions dustTransition(@NotNull Color from, @NotNull Color to, float size) {
        return of(Particle.DUST_COLOR_TRANSITION).withColor(from).withToColor(to).withSize(size);
    }

    public @NotNull ParticleOptions withColor(@Nullable Color c) {
        return new ParticleOptions(particle, c, toColor, size, speed, count,
                offsetX, offsetY, offsetZ, forceShow, material, blockData);
    }

    public @NotNull ParticleOptions withToColor(@Nullable Color c) {
        return new ParticleOptions(particle, color, c, size, speed, count,
                offsetX, offsetY, offsetZ, forceShow, material, blockData);
    }

    public @NotNull ParticleOptions withSize(float s) {
        return new ParticleOptions(particle, color, toColor, s, speed, count,
                offsetX, offsetY, offsetZ, forceShow, material, blockData);
    }

    public @NotNull ParticleOptions withSpeed(float s) {
        return new ParticleOptions(particle, color, toColor, size, s, count,
                offsetX, offsetY, offsetZ, forceShow, material, blockData);
    }

    public @NotNull ParticleOptions withCount(int c) {
        return new ParticleOptions(particle, color, toColor, size, speed, c,
                offsetX, offsetY, offsetZ, forceShow, material, blockData);
    }

    public @NotNull ParticleOptions withOffset(float x, float y, float z) {
        return new ParticleOptions(particle, color, toColor, size, speed, count,
                x, y, z, forceShow, material, blockData);
    }

    public @NotNull ParticleOptions withOffset(float uniform) {
        return withOffset(uniform, uniform, uniform);
    }

    public @NotNull ParticleOptions withForceShow(boolean force) {
        return new ParticleOptions(particle, color, toColor, size, speed, count,
                offsetX, offsetY, offsetZ, force, material, blockData);
    }

    public @NotNull ParticleOptions withMaterial(@Nullable Material m) {
        return new ParticleOptions(particle, color, toColor, size, speed, count,
                offsetX, offsetY, offsetZ, forceShow, m, blockData);
    }

    public @NotNull ParticleOptions withBlockData(@Nullable String bd) {
        return new ParticleOptions(particle, color, toColor, size, speed, count,
                offsetX, offsetY, offsetZ, forceShow, material, bd);
    }

    public @NotNull ParticleOptions withParticle(@NotNull Particle p) {
        return new ParticleOptions(p, color, toColor, size, speed, count,
                offsetX, offsetY, offsetZ, forceShow, material, blockData);
    }
}