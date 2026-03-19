package me.sunmc.particlelib.math.equation;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe cache of parsed {@link EquationTransform}s.
 *
 * <p>Parsing equations is expensive; this store ensures each unique
 * (equation, variables) combination is only parsed once.</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * EquationTransform t = EquationStore.get().getOrCreate("4*sin(t)", "t");
 * double y = t.get(step);
 * }</pre>
 */
public final class EquationStore {

    private static final EquationStore INSTANCE = new EquationStore();

    public static @NotNull EquationStore get() {
        return INSTANCE;
    }

    private final ConcurrentHashMap<String, EquationTransform> cache = new ConcurrentHashMap<>();

    private EquationStore() {
    }

    /**
     * Returns a cached {@link EquationTransform} for the given equation and variable names,
     * creating one if it doesn't exist yet.
     */
    public @NotNull EquationTransform getOrCreate(@NotNull String equation,
                                                  @NotNull String... variables) {
        String key = equation + ":" + String.join(",", variables);
        return cache.computeIfAbsent(key, k -> new EquationTransform(equation, variables));
    }

    /**
     * Clears all cached equations. Useful for plugin reloads.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Returns the number of cached equation transforms.
     */
    public int size() {
        return cache.size();
    }
}