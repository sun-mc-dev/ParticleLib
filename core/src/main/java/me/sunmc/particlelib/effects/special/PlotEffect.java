package me.sunmc.particlelib.effects.special;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import me.sunmc.particlelib.math.equation.EquationStore;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlotEffect extends AbstractEffect {

    public final @Nullable String xEquation, yEquation, zEquation;
    public final double xScale, yScale, zScale;
    public final boolean persistent;

    private PlotEffect(Builder b) {
        super(b);
        this.xEquation = b.xEquation;
        this.yEquation = b.yEquation;
        this.zEquation = b.zEquation;
        this.xScale = b.xScale;
        this.yScale = b.yScale;
        this.zScale = b.zScale;
        this.persistent = b.persistent;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        int base = persistent ? 0 : iteration;
        Vec3 origin = Vec3.fromLocation(ctx.origin());
        var store = EquationStore.get();

        for (int i = base; i <= iteration; i++) {
            double xOff = xEquation != null ? store.getOrCreate(xEquation, "t", "i").get(i, iterations()) : i;
            double yOff = yEquation != null ? store.getOrCreate(yEquation, "t", "i").get(i, iterations()) : i;
            double zOff = zEquation != null ? store.getOrCreate(zEquation, "t", "i").get(i, iterations()) : 0;
            displayAbsolute(ctx, origin.add(xOff * xScale, yOff * yScale, zOff * zScale));
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public @Nullable String xEquation = null, yEquation = null, zEquation = null;
        public double xScale = 1, yScale = 1, zScale = 1;
        public boolean persistent = true;

        {
            type = EffectType.REPEATING;
            period = 1;
            iterations = 100;
            particle = Particle.DUST;
        }

        @Contract(mutates = "this")
        public @NotNull Builder x(String eq) {
            xEquation = eq;
            return self();
        }

        public @NotNull Builder y(String eq) {
            yEquation = eq;
            return self();
        }

        public @NotNull Builder z(String eq) {
            zEquation = eq;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder persistent(boolean b) {
            persistent = b;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull PlotEffect build() {
            return new PlotEffect(this);
        }
    }
}