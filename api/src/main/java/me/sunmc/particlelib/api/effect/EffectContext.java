package me.sunmc.particlelib.api.effect;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot of where and to whom an effect should be played.
 *
 * <p>Locations are cloned on construction — mutations to the originals
 * do not affect an {@code EffectContext} already in flight.</p>
 */
public record EffectContext(
        @NotNull Location origin,
        @Nullable Location target,
        @Nullable List<Player> targetPlayers,
        double visibleRange
) {
    public EffectContext {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(origin.getWorld(), "origin must have a non-null World");
        origin = origin.clone();
        target = target == null ? null : target.clone();
        targetPlayers = targetPlayers == null ? null : List.copyOf(targetPlayers);
        if (visibleRange < 0) throw new IllegalArgumentException("visibleRange must be >= 0");
    }

    public static @NotNull EffectContext at(@NotNull Location origin) {
        return new EffectContext(origin, null, null, 32.0);
    }

    public static @NotNull EffectContext at(@NotNull Location origin, @NotNull Location target) {
        return new EffectContext(origin, target, null, 32.0);
    }

    public static @NotNull EffectContext forPlayer(@NotNull Location origin, @NotNull Player player) {
        return new EffectContext(origin, null, List.of(player), 32.0);
    }

    public @NotNull World world() {
        return Objects.requireNonNull(origin.getWorld());
    }

    public @NotNull EffectContext withTarget(@Nullable Location newTarget) {
        return new EffectContext(origin, newTarget, targetPlayers, visibleRange);
    }

    public @NotNull EffectContext withVisibleRange(double range) {
        return new EffectContext(origin, target, targetPlayers, range);
    }

    public @NotNull EffectContext withTargetPlayers(@Nullable List<Player> players) {
        return new EffectContext(origin, target, players, visibleRange);
    }
}
