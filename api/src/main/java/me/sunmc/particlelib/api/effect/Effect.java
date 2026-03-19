package me.sunmc.particlelib.api.effect;

import org.jetbrains.annotations.NotNull;

/**
 * Core effect interface. All built-in and custom effects implement this.
 *
 * <p>Implement {@link me.sunmc.particlelib.effects.AbstractEffect} instead of
 * this interface directly — it handles the scheduling and lifecycle boilerplate.</p>
 */
public interface Effect {

    /**
     * Unique string identifier, e.g. {@code "sphere"}, {@code "my_plugin:custom"}.
     */
    @NotNull String id();

    /**
     * Execution model.
     */
    @NotNull EffectType type();

    /**
     * Plays the effect at the given context.
     *
     * @param context where and to whom to play
     * @return a handle that can be used to cancel, pause, or query the effect
     */
    @NotNull EffectHandle play(@NotNull EffectContext context);
}