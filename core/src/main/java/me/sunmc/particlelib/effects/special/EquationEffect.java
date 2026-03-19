package me.sunmc.particlelib.effects.special;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import me.sunmc.particlelib.math.equation.EquationStore;
import me.sunmc.particlelib.math.equation.EquationTransform;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Plays particles whose positions are defined by mathematical equations.
 *
 * <pre>{@code
 * EffectHandle h = EquationEffect.builder()
 *     .x("4 * sin(t)")
 *     .y("0")
 *     .z("4 * cos(t)")
 *     .stepsPerTick(2)
 *     .maxSteps(628)   // 2π × 100
 *     .playAt(loc);
 * }</pre>
 */
public final class EquationEffect extends AbstractEffect {

    public final String xEq, yEq, zEq;
    public final String variable;
    public final int stepsPerTick;
    public final int maxSteps;       // 0 = unlimited
    public final boolean orient;
    public final boolean orientPitch;
    // inner equations (optional sub-iteration)
    public final @Nullable String x2Eq, y2Eq, z2Eq;
    public final String variable2;
    public final int stepsPerTick2;
    public final boolean cycleMiniStep;

    private EquationTransform xt, yt, zt;
    private @Nullable EquationTransform x2t, y2t, z2t;
    private int step = 0;
    private int miniStep = 0;

    private EquationEffect(Builder b) {
        super(b);
        this.xEq = b.xEq;
        this.yEq = b.yEq;
        this.zEq = b.zEq;
        this.variable = b.variable;
        this.stepsPerTick = b.stepsPerTick;
        this.maxSteps = b.maxSteps;
        this.orient = b.orient;
        this.orientPitch = b.orientPitch;
        this.x2Eq = b.x2Eq;
        this.y2Eq = b.y2Eq;
        this.z2Eq = b.z2Eq;
        this.variable2 = b.variable2;
        this.stepsPerTick2 = b.stepsPerTick2;
        this.cycleMiniStep = b.cycleMiniStep;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        if (iteration == 0) {
            step = 0;
            miniStep = 0;
            var store = EquationStore.get();
            xt = store.getOrCreate(xEq, variable);
            yt = store.getOrCreate(yEq, variable);
            zt = store.getOrCreate(zEq, variable);
            if (x2Eq != null && y2Eq != null && z2Eq != null && stepsPerTick2 > 0) {
                x2t = store.getOrCreate(x2Eq, variable, variable2);
                y2t = store.getOrCreate(y2Eq, variable, variable2);
                z2t = store.getOrCreate(z2Eq, variable, variable2);
            }
        }

        Location origin = ctx.origin();
        Vec3 base = Vec3.fromLocation(origin);

        for (int i = 0; i < stepsPerTick; i++) {
            double s = step;
            Vec3 offset = new Vec3(xt.get(s), yt.get(s), zt.get(s));
            if (orient && orientPitch) offset = offset.rotateByLocation(origin);
            else if (orient) offset = offset.rotateByYawPitch(origin.getYaw(), 0);

            Vec3 target = base.add(offset);

            if (x2t != null) {
                for (int j = 0; j < stepsPerTick2; j++) {
                    Vec3 off2 = new Vec3(x2t.get(s, miniStep), Objects.requireNonNull(y2t).get(s, miniStep),
                            Objects.requireNonNull(z2t).get(s, miniStep));
                    if (orient && orientPitch) off2 = off2.rotateByLocation(origin);
                    else if (orient) off2 = off2.rotateByYawPitch(origin.getYaw(), 0);
                    displayAbsolute(ctx, target.add(off2));
                    miniStep++;
                }
                if (cycleMiniStep) miniStep = 0;
            } else {
                displayAbsolute(ctx, target);
            }

            step++;
            if (maxSteps > 0 && step > maxSteps) {
                step = 0;
                break;
            }
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public String xEq = "t", yEq = "0", zEq = "0";
        public String variable = "t";
        public int stepsPerTick = 1, maxSteps = 0;
        public boolean orient = true, orientPitch = true;
        public @Nullable String x2Eq = null, y2Eq = null, z2Eq = null;
        public String variable2 = "t2";
        public int stepsPerTick2 = 0;
        public boolean cycleMiniStep = true;

        {
            type = EffectType.REPEATING;
            period = 1;
            iterations = 100;
            particle = Particle.DUST;
        }

        @Contract(mutates = "this")
        public @NotNull Builder x(String eq) {
            xEq = eq;
            return self();
        }

        public @NotNull Builder y(String eq) {
            yEq = eq;
            return self();
        }

        public @NotNull Builder z(String eq) {
            zEq = eq;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder variable(String v) {
            variable = v;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder stepsPerTick(int n) {
            stepsPerTick = n;
            return self();
        }

        public @NotNull Builder maxSteps(int n) {
            maxSteps = n;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder orient(boolean o) {
            orient = o;
            return self();
        }

        public @NotNull Builder orientPitch(boolean o) {
            orientPitch = o;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull EquationEffect build() {
            return new EquationEffect(this);
        }
    }
}