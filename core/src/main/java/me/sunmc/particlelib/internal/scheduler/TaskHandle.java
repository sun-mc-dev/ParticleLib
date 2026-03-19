package me.sunmc.particlelib.internal.scheduler;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A scheduler-agnostic cancellable task handle.
 *
 * <p>Wraps whatever the underlying scheduler returns (Paper {@code ScheduledTask},
 * classic {@code BukkitTask}, etc.) behind a single {@code cancel()} call
 * so no scheduler-specific types ever leak into the rest of the codebase.</p>
 */
@FunctionalInterface
public interface TaskHandle {

    /**
     * Cancels the underlying task. Implementations must be idempotent.
     */
    void cancel();

    /**
     * A no-op handle (use for already-completed or never-started tasks).
     */
    @Contract(pure = true)
    static @NotNull TaskHandle noop() {
        return () -> {
        };
    }
}