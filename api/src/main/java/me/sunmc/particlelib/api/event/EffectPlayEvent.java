package me.sunmc.particlelib.api.event;

import me.sunmc.particlelib.api.effect.Effect;
import me.sunmc.particlelib.api.effect.EffectContext;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired on the global region thread immediately before an {@link Effect} is
 * scheduled for the first time.
 *
 * <p>Cancelling this event prevents the effect from being played at all.
 * The returned {@link me.sunmc.particlelib.api.effect.EffectHandle} will be
 * a {@link me.sunmc.particlelib.api.effect.EffectHandle#terminated()} instance.</p>
 *
 * <p><strong>Thread:</strong> always fired synchronously on the global region /
 * main thread, regardless of whether the effect itself runs asynchronously.
 * Listeners may safely call Bukkit APIs.</p>
 *
 * <h3>Example</h3>
 * <pre>{@code
 * @EventHandler
 * public void onEffectPlay(EffectPlayEvent e) {
 *     if (e.getEffect().id().equals("big_bang")) {
 *         e.setCancelled(true); // disable firework effect on this server
 *     }
 * }
 * }</pre>
 */
public final class EffectPlayEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Effect effect;
    private final EffectContext context;
    private boolean cancelled = false;

    public EffectPlayEvent(@NotNull Effect effect, @NotNull EffectContext context) {
        super(false); // not async — always fired on region/main thread
        this.effect = effect;
        this.context = context;
    }

    /**
     * The effect that is about to be played.
     */
    public @NotNull Effect getEffect() {
        return effect;
    }

    /**
     * The context (location, target, players) for this play invocation.
     */
    public @NotNull EffectContext getContext() {
        return context;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean c) {
        cancelled = c;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
