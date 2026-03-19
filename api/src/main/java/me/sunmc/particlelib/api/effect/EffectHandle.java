package me.sunmc.particlelib.api.effect;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A handle to a running effect returned by {@link Effect#play(EffectContext)}.
 *
 * <h3>Why not sealed?</h3>
 * <p>The original design used a {@code sealed} interface permitting
 * {@code ActiveEffectHandle} and {@code TerminatedEffectHandle} from the
 * <em>core</em> module. Java's sealed type system requires all permitted
 * subtypes to be in the same module (or the same compilation unit for
 * unnamed modules). Since {@code api} and {@code core} are separate Maven
 * modules / separate jars, the {@code permits} clause would fail at compile
 * time with {@code "class is not accessible"}.
 *
 * <p>The interface is therefore non-sealed. The two concrete implementations
 * ({@code ActiveEffectHandle}, {@code TerminatedEffectHandle}) live in
 * {@code core} and are never exposed directly to API consumers — they always
 * receive an {@code EffectHandle} reference.</p>
 */
public interface EffectHandle {

    /**
     * Cancels the effect immediately. Idempotent — safe to call multiple times.
     */
    void cancel();

    /**
     * Pauses particle emission without destroying the scheduler task.
     */
    void pause();

    /**
     * Resumes a paused effect. No-op if not paused.
     */
    void resume();

    /**
     * {@code true} while the effect is running and not yet done or cancelled.
     */
    boolean isActive();

    /**
     * {@code true} if the effect is running but temporarily paused.
     */
    boolean isPaused();

    /**
     * {@code true} if the effect has finished or been cancelled.
     */
    boolean isDone();

    /**
     * Returns a no-op handle representing an already-terminated effect.
     * All mutation methods are no-ops; {@link #isDone()} always returns {@code true}.
     *
     * <p>Implementation note: the concrete class is loaded reflectively so this
     * module has no compile-time dependency on {@code core}.</p>
     */
    static EffectHandle terminated() {
        return TerminatedHandleSingleton.INSTANCE;
    }

    /**
     * Package-private singleton carrier — avoids a hard reference to the
     * core implementation class from the api module.
     */
    final class TerminatedHandleSingleton implements EffectHandle {

        static final TerminatedHandleSingleton INSTANCE = new TerminatedHandleSingleton();

        private TerminatedHandleSingleton() {
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
}