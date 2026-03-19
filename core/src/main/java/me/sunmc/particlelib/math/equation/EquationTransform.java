package me.sunmc.particlelib.math.equation;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

/**
 * Wraps an exp4j {@link Expression} for a single equation string.
 *
 * <p>Thread safety: {@code get(double...)} is {@code synchronized} because
 * exp4j expressions are stateful (mutable variable slots).  If you need
 * higher throughput, create separate instances per thread via
 * {@link EquationStore#getOrCreate}.</p>
 *
 * <p>Supported custom functions beyond standard math:</p>
 * <ul>
 *   <li>{@code rand(min, max)} — uniform random in [min, max)</li>
 *   <li>{@code prob(p, a, b)} — returns {@code a} if random &lt; p, else {@code b}</li>
 *   <li>{@code min(a, b)}, {@code max(a, b)}</li>
 *   <li>{@code select(v, neg, zero, pos)} — ternary-like: picks branch based on sign of v</li>
 * </ul>
 */
public final class EquationTransform {

    private static final Function RAND = new Function("rand", 2) {
        private final Random rng = new Random();

        @Override
        public double apply(double @NotNull ... a) {
            return rng.nextDouble() * (a[1] - a[0]) + a[0];
        }
    };
    private static final Function PROB = new Function("prob", 3) {
        private final Random rng = new Random();

        @Override
        public double apply(double @NotNull ... a) {
            return rng.nextDouble() < a[0] ? a[1] : a[2];
        }
    };
    private static final Function MIN = new Function("min", 2) {
        @Contract(pure = true)
        @Override
        public double apply(double @NotNull ... a) {
            return Math.min(a[0], a[1]);
        }
    };
    private static final Function MAX = new Function("max", 2) {
        @Contract(pure = true)
        @Override
        public double apply(double @NotNull ... a) {
            return Math.max(a[0], a[1]);
        }
    };
    private static final Function SELECT = new Function("select", 4) {
        @Contract(pure = true)
        @Override
        public double apply(double @NotNull ... a) {
            if (a[0] < 0) return a[1];
            if (a[0] == 0) return a[2];
            return a[3];
        }
    };

    private final String equation;
    private final String[] variables;
    private final Expression expression;
    private @Nullable
    final Exception parseException;

    EquationTransform(@NotNull String equation, @NotNull String... variables) {
        this.equation = equation;
        this.variables = variables;

        Expression built = null;
        Exception error = null;
        try {
            built = new ExpressionBuilder(equation)
                    .functions(RAND, PROB, MIN, MAX, SELECT)
                    .variables(new HashSet<>(Arrays.asList(variables)))
                    .build();
        } catch (Exception ex) {
            error = ex;
        }
        this.expression = built;
        this.parseException = error;
    }

    /**
     * Evaluates with a single variable value.
     */
    public synchronized double get(double t) {
        return get(new double[]{t});
    }

    /**
     * Evaluates with two variable values (e.g. t + step).
     */
    public synchronized double get(double t, double step) {
        return get(new double[]{t, step});
    }

    /**
     * Evaluates with up to four variable values (t, i, a, b).
     */
    public synchronized double get(double t, double i, double a, double b) {
        return get(new double[]{t, i, a, b});
    }

    private double get(double[] values) {
        if (expression == null) return 0.0;
        for (int idx = 0; idx < variables.length && idx < values.length; idx++) {
            expression.setVariable(variables[idx], values[idx]);
        }
        try {
            return expression.evaluate();
        } catch (Exception ex) {
            return Double.NaN;
        }
    }

    public boolean isValid() {
        return parseException == null;
    }

    public @Nullable Exception exception() {
        return parseException;
    }

    public @NotNull String equation() {
        return equation;
    }
}
