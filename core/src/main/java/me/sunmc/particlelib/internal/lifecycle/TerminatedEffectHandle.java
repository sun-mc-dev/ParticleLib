package me.sunmc.particlelib.internal.lifecycle;

import me.sunmc.particlelib.api.effect.EffectHandle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Inert handle representing an effect that has already finished or was
 * never started. All mutation methods are no-ops.
 *
 * <p>Note: {@link EffectHandle#terminated()} returns the api-module singleton
 * ({@link EffectHandle.TerminatedHandleSingleton}). This class exists for
 * internal use where the core module needs a concrete terminated handle
 * without going through the api factory method.</p>
 */
public final class TerminatedEffectHandle implements EffectHandle {

    public static final TerminatedEffectHandle INSTANCE = new TerminatedEffectHandle();

    private TerminatedEffectHandle() {
    }

    @Override
    public void cancel() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "EffectHandle[TERMINATED]";
    }
}