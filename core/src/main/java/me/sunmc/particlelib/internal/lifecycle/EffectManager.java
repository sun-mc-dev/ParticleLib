package me.sunmc.particlelib.internal.lifecycle;

import me.sunmc.particlelib.api.effect.Effect;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectHandle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-plugin manager that tracks active {@link EffectHandle}s and provides
 * bulk cancel operations.
 *
 * <h3>Fix: no leaked cleanup tasks</h3>
 * <p>The previous implementation spawned one permanent repeating async task
 * per effect to poll {@code handle.isDone()} every second. This leaked tasks
 * indefinitely when effects were short-lived or frequently cancelled.</p>
 *
 * <p>The new approach wraps each handle in a self-removing
 * {@link ManagedHandle} that calls {@link #onHandleDone} when the underlying
 * effect finishes. That callback removes the handle from the tracking map
 * without any polling task.</p>
 */
public final class EffectManager {

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
    }

    /**
     * Plays an effect, tracks its handle, and returns a cancellable reference.
     *
     * <p>The returned handle automatically unregisters itself from this manager
     * when the effect finishes — no polling or cleanup tasks required.</p>
     */
    public @NotNull EffectHandle play(@NotNull Effect effect,
                                      @NotNull EffectContext context) {
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

    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    /**
     * Thin wrapper around an {@link EffectHandle} that notifies the owning
     * {@link EffectManager} when the effect is done, so the manager's tracking
     * map stays clean without any polling.
     */
    private record ManagedHandle(UUID id, EffectHandle delegate, EffectManager manager) implements EffectHandle {

        private ManagedHandle(@NotNull UUID id,
                              @NotNull EffectHandle delegate,
                              @NotNull EffectManager manager) {
            this.id = id;
            this.delegate = delegate;
            this.manager = manager;
        }

        @Override
        public void cancel() {
            delegate.cancel();
            manager.onHandleDone(id);
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
            if (done) manager.onHandleDone(id);
            return done;
        }
    }
}