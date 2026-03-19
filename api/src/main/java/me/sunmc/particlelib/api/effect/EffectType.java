package me.sunmc.particlelib.api.effect;

/**
 * Execution model for an {@link Effect}.
 */
public enum EffectType {
    /**
     * Executes once immediately.
     */
    INSTANT,
    /**
     * Executes once after a configurable delay.
     */
    DELAYED,
    /**
     * Repeats at a fixed period until cancelled or iteration limit is reached.
     */
    REPEATING
}