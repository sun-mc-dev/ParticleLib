package me.sunmc.particlelib.internal.lifecycle;

import me.sunmc.particlelib.api.effect.EffectHandle;
import org.jetbrains.annotations.NotNull;

/**
 * Live handle for a currently-running effect.
 * Delegates all state queries and commands to the owning {@link EffectRunner}.
 */
public final class ActiveEffectHandle implements EffectHandle {

    private final EffectRunner runner;

    public ActiveEffectHandle(@NotNull EffectRunner runner) {
        this.runner = runner;
    }

    @Override
    public void cancel() {
        runner.cancel();
    }

    @Override
    public void pause() {
        runner.pause();
    }

    @Override
    public void resume() {
        runner.resume();
    }

    @Override
    public boolean isActive() {
        return !runner.isDone();
    }

    @Override
    public boolean isPaused() {
        return runner.isPaused();
    }

    @Override
    public boolean isDone() {
        return runner.isDone();
    }

    @Override
    public @NotNull String toString() {
        return "EffectHandle[active=" + isActive()
                + ", paused=" + isPaused()
                + ", done=" + isDone() + "]";
    }
}