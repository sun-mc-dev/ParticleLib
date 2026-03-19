package me.sunmc.particlelib.api.event;

import me.sunmc.particlelib.api.effect.Effect;
import me.sunmc.particlelib.api.effect.EffectContext;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired on the global region thread when an {@link Effect} finishes or is
 * cancelled (via {@link me.sunmc.particlelib.api.effect.EffectHandle#cancel()}).
 *
 * <p>This event is not cancellable — it is informational only. Use it to trigger
 * follow-up logic when an effect ends (e.g. apply a final burst, update a
 * database, remove a scoreboard entry).</p>
 *
 * <p><strong>Thread:</strong> always fired on the global region / main thread.</p>
 *
 * <h3>Example</h3>
 * <pre>{@code
 * @EventHandler
 * public void onEffectCancel(EffectCancelEvent e) {
 *     if (e.getEffect().id().startsWith("death_")) {
 *         Bukkit.broadcastMessage("A death effect just finished!");
 *     }
 * }
 * }</pre>
 */
public final class EffectCancelEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Effect effect;
    private final EffectContext context;
    private final CancelReason reason;

    /**
     * Reason the effect stopped playing.
     */
    public enum CancelReason {
        /**
         * Effect completed all its iterations naturally.
         */
        COMPLETED,
        /**
         * Effect was cancelled early via {@link me.sunmc.particlelib.api.effect.EffectHandle#cancel()}.
         */
        CANCELLED,
        /**
         * Effect encountered an unrecoverable error during a tick.
         */
        ERROR
    }

    public EffectCancelEvent(@NotNull Effect effect,
                             @NotNull EffectContext context,
                             @NotNull CancelReason reason) {
        super(false); // not async
        this.effect = effect;
        this.context = context;
        this.reason = reason;
    }

    /**
     * The effect that ended.
     */
    public @NotNull Effect getEffect() {
        return effect;
    }

    /**
     * The context this effect was playing in.
     */
    public @NotNull EffectContext getContext() {
        return context;
    }

    /**
     * Why the effect stopped.
     */
    public @NotNull CancelReason getReason() {
        return reason;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}