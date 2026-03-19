package me.sunmc.particlelib.internal.lifecycle;

import me.sunmc.particlelib.api.effect.Effect;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectHandle;
import me.sunmc.particlelib.config.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Per-plugin manager that tracks active {@link EffectHandle}s and provides
 * bulk cancel operations.
 *
 * <h3>No leaked cleanup tasks</h3>
 * <p>Each handle is wrapped in a self-removing {@link ManagedHandle} that calls
 * {@link #onHandleDone} when the underlying effect finishes. That callback
 * removes the handle from the tracking map without any polling task.</p>
 *
 * <h3>Player cleanup</h3>
 * <p>Implements {@link Listener} to automatically remove players from the
 * ignored-players set when they disconnect, preventing stale UUID accumulation.</p>
 *
 * <h3>Max-effects cap</h3>
 * <p>Reads {@link ConfigManager#maxActiveEffects()} on each {@link #play} call.
 * Effects beyond the cap are silently dropped and return
 * {@link EffectHandle#terminated()}.</p>
 */
public final class EffectManager implements Listener {

    private final Plugin plugin;

    /**
     * UUID → managed handle for every currently active effect.
     */
    private final Map<UUID, ManagedHandle> active = new ConcurrentHashMap<>();

    /**
     * Players whose particles are suppressed.
     */
    private final Set<UUID> ignoredPlayers = ConcurrentHashMap.newKeySet();

    public EffectManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Plays an effect, tracks its handle, and returns a cancellable reference.
     *
     * <p>If {@link ConfigManager#maxActiveEffects()} is exceeded the effect is
     * dropped and {@link EffectHandle#terminated()} is returned.</p>
     *
     * <p>The returned handle automatically unregisters itself from this manager
     * when the effect finishes — no polling or cleanup tasks required.</p>
     */
    public @NotNull EffectHandle play(@NotNull Effect effect,
                                      @NotNull EffectContext context) {
        int max = ConfigManager.get().maxActiveEffects();
        if (max > 0 && active.size() >= max) {
            plugin.getLogger().fine("[ParticleLib] Max active effects reached ("
                    + max + "). Dropping: " + effect.id());
            return EffectHandle.terminated();
        }
        UUID id = UUID.randomUUID();
        EffectHandle raw = effect.play(context);
        ManagedHandle managed = new ManagedHandle(id, raw, this);
        active.put(id, managed);
        return managed;
    }

    void onHandleDone(@NotNull UUID id) {
        active.remove(id);
    }

    /**
     * Cancels all tracked effects immediately.
     */
    public void cancelAll() {
        // Snapshot to avoid CME — ManagedHandle.cancel() calls onHandleDone()
        new ArrayList<>(active.values()).forEach(ManagedHandle::cancel);
        active.clear();
    }

    /**
     * Cancels all effects and releases resources. Call on plugin disable.
     */
    public void dispose() {
        cancelAll();
        ignoredPlayers.clear();
        HandlerList.unregisterAll(this);
    }

    public void ignorePlayer(@NotNull Player player) {
        ignoredPlayers.add(player.getUniqueId());
    }

    public void unignorePlayer(@NotNull Player player) {
        ignoredPlayers.remove(player.getUniqueId());
    }

    public boolean isIgnored(@NotNull Player player) {
        return ignoredPlayers.contains(player.getUniqueId());
    }

    public int activeCount() {
        return active.size();
    }

    /**
     * Automatically clears the ignored-players entry when a player leaves.
     */
    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        ignoredPlayers.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Thin wrapper around an {@link EffectHandle} that notifies the owning
     * {@link EffectManager} when the effect is done, so the manager's tracking
     * map stays clean without any polling.
     *
     * <p>Uses an {@link AtomicBoolean} one-shot flag so {@link #onHandleDone}
     * is called at most once even if {@link #isDone()} is polled repeatedly
     * after the effect completes.</p>
     */
    private static final class ManagedHandle implements EffectHandle {

        private final UUID id;
        private final EffectHandle delegate;
        private final EffectManager manager;
        private final AtomicBoolean notified = new AtomicBoolean(false);

        ManagedHandle(@NotNull UUID id,
                      @NotNull EffectHandle delegate,
                      @NotNull EffectManager manager) {
            this.id = id;
            this.delegate = delegate;
            this.manager = manager;
        }

        private void notifyDoneOnce() {
            if (notified.compareAndSet(false, true)) {
                manager.onHandleDone(id);
            }
        }

        @Override
        public void cancel() {
            delegate.cancel();
            notifyDoneOnce();
        }

        @Override
        public void pause() {
            delegate.pause();
        }

        @Override
        public void resume() {
            delegate.resume();
        }

        @Override
        public boolean isActive() {
            return delegate.isActive();
        }

        @Override
        public boolean isPaused() {
            return delegate.isPaused();
        }

        @Override
        public boolean isDone() {
            boolean done = delegate.isDone();
            if (done) notifyDoneOnce();
            return done;
        }
    }
}