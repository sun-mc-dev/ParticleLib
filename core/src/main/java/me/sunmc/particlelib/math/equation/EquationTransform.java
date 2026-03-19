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
 * <h3>Thread safety</h3>
 * <p>exp4j expressions are stateful (mutable variable slots) and cannot be
 * shared across threads. This class uses a {@link ThreadLocal} to give each
 * thread its own {@link Expression} instance, parsed lazily on first use per
 * thread. This eliminates contention between concurrent async-effect ticks
 * that share the same cached {@code EquationTransform} without the coarse
 * {@code synchronized} lock of the previous version.</p>
 *
 * <h3>Caching</h3>
 * <p>Instances are managed by {@link EquationStore}, which ensures each unique
 * (equation, variables) combination is only constructed once. Each thread then
 * parses its own copy lazily on first access.</p>
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
    private final @Nullable Exception parseException;

    /**
     * One independently-mutable {@link Expression} per thread, parsed lazily.
     * Each thread gets its own instance so variable slots are never shared.
     */
    private final ThreadLocal<Expression> threadLocalExpr;

    EquationTransform(@NotNull String equation, @NotNull String... variables) {
        this.equation = equation;
        this.variables = variables;

        // Validate the equation once on the constructing thread so we can
        // surface parse errors immediately rather than on first evaluation.
        Exception error = null;
        try {
            buildExpression();
        } catch (Exception ex) {
            error = ex;
        }
        this.parseException = error;

        // Each thread builds its own Expression on first use.
        this.threadLocalExpr = ThreadLocal.withInitial(this::buildExpression);
    }

    private @NotNull Expression buildExpression() {
        return new ExpressionBuilder(equation)
                .functions(RAND, PROB, MIN, MAX, SELECT)
                .variables(new HashSet<>(Arrays.asList(variables)))
                .build();
    }

    /**
     * Evaluates with a single variable value.
     */
    public double get(double t) {
        return evaluate(new double[]{t});
    }

    /**
     * Evaluates with two variable values (e.g. {@code t} + step).
     */
    public double get(double t, double step) {
        return evaluate(new double[]{t, step});
    }

    /**
     * Evaluates with up to four variable values ({@code t, i, a, b}).
     */
    public double get(double t, double i, double a, double b) {
        return evaluate(new double[]{t, i, a, b});
    }

    /**
     * Retrieves this thread's {@link Expression}, sets its variables, and
     * evaluates it. No locking required — each thread owns its instance.
     */
    private double evaluate(double[] values) {
        if (parseException != null) return 0.0;
        Expression expr = threadLocalExpr.get();
        for (int idx = 0; idx < variables.length && idx < values.length; idx++) {
            expr.setVariable(variables[idx], values[idx]);
        }
        try {
            return expr.evaluate();
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