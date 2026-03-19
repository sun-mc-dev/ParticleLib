package me.sunmc.particlelib.effects.special;

import me.sunmc.particlelib.api.effect.Effect;
import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import me.sunmc.particlelib.math.equation.EquationStore;
import me.sunmc.particlelib.math.equation.EquationTransform;
import me.sunmc.particlelib.registry.EffectRegistry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.HashMap;
import java.util.Map;

/**
 * Drives an inner effect while modifying its public fields each tick
 * via equations. Reproduces EffectLib's ModifiedEffect cleanly.
 *
 * <pre>{@code
 * ModifiedEffect.builder()
 *     .innerEffect("sphere")
 *     .parameter("radius", "0.5 + t * 0.01")
 *     .iterations(200)
 *     .playAt(loc);
 * }</pre>
 *
 * <h3>Java 21 reflection fix</h3>
 * <p>Field lookup walks the entire class hierarchy via {@code getDeclaredField}
 * and always calls {@code setAccessible(true)} before use. Unknown or
 * inaccessible fields are silently skipped rather than crashing the effect.</p>
 */
public final class ModifiedEffect extends AbstractEffect {

    public final String innerEffectId;
    public final Map<String, String> parameters;
    public final @Nullable String xEq, yEq, zEq;
    public final boolean orient, orientPitch;

    private AbstractEffect innerEffect;
    private Vec3 previousOffset;
    private EquationTransform xt, yt, zt;
    private final Map<Field, EquationTransform> fieldTransforms = new HashMap<>();

    private ModifiedEffect(Builder b) {
        super(b);
        this.innerEffectId = b.innerEffectId;
        this.parameters = Map.copyOf(b.parameters);
        this.xEq = b.xEq;
        this.yEq = b.yEq;
        this.zEq = b.zEq;
        this.orient = b.orient;
        this.orientPitch = b.orientPitch;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        if (iteration == 0) init(ctx);
        if (innerEffect == null) return;

        // location equations
        if (xt != null || yt != null || zt != null) {
            Vec3 offset = new Vec3(
                    xt != null ? xt.get(iteration, iterations()) : 0,
                    yt != null ? yt.get(iteration, iterations()) : 0,
                    zt != null ? zt.get(iteration, iterations()) : 0);
            if (previousOffset != null) offset = offset.subtract(previousOffset);
            else previousOffset = Vec3.ZERO;
            if (orient && orientPitch) offset = offset.rotateByLocation(ctx.origin());
            else if (orient) offset = offset.rotateByYawPitch(ctx.origin().getYaw(), 0);
            previousOffset = (previousOffset != null ? previousOffset : Vec3.ZERO).add(offset);
        }

        // field equations
        for (var entry : fieldTransforms.entrySet()) {
            double val = entry.getValue().get(iteration, iterations());
            try {
                Field f = entry.getKey();
                if (f.getType() == double.class || f.getType() == Double.class)
                    f.set(innerEffect, val);
                else if (f.getType() == float.class || f.getType() == Float.class)
                    f.set(innerEffect, (float) val);
                else if (f.getType() == int.class || f.getType() == Integer.class)
                    f.set(innerEffect, (int) val);
            } catch (IllegalAccessException ignored) {
            }
        }

        innerEffect.onTick(ctx, iteration);
    }

    private void init(EffectContext ctx) {
        var store = EquationStore.get();
        if (xEq != null) xt = store.getOrCreate(xEq, "t", "i");
        if (yEq != null) yt = store.getOrCreate(yEq, "t", "i");
        if (zEq != null) zt = store.getOrCreate(zEq, "t", "i");

        EffectRegistry.get().lookup(innerEffectId).ifPresent(b -> {
            Effect built = b.build();
            if (!(built instanceof AbstractEffect ae)) return;
            innerEffect = ae;
            for (var entry : parameters.entrySet()) {
                Field f = findField(ae.getClass(), entry.getKey());
                if (f == null) continue;
                try {
                    f.setAccessible(true);
                    fieldTransforms.put(f, store.getOrCreate(entry.getValue(), "t", "i"));
                } catch (InaccessibleObjectException ignored) {
                }
            }
        });
    }

    /**
     * Walks the class hierarchy to find a declared field by name,
     * including fields on superclasses up to (but not including) {@link Object}.
     */
    private @Nullable Field findField(@NotNull Class<?> clazz, @NotNull String name) {
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
            c = c.getSuperclass();
        }
        return null;
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public String innerEffectId = "sphere";
        public final Map<String, String> parameters = new HashMap<>();
        public @Nullable String xEq = null, yEq = null, zEq = null;
        public boolean orient = true, orientPitch = false;

        {
            type = EffectType.REPEATING;
            period = 1;
            iterations = 100;
        }

        @Contract(mutates = "this")
        public @NotNull Builder innerEffect(String id) {
            innerEffectId = id;
            return self();
        }

        public @NotNull Builder parameter(String field, String eq) {
            parameters.put(field, eq);
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder xEquation(String eq) {
            xEq = eq;
            return self();
        }

        public @NotNull Builder yEquation(String eq) {
            yEq = eq;
            return self();
        }

        public @NotNull Builder zEquation(String eq) {
            zEq = eq;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull ModifiedEffect build() {
            return new ModifiedEffect(this);
        }
    }
}