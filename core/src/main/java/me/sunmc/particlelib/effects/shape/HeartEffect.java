package me.sunmc.particlelib.effects.shape;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class HeartEffect extends AbstractEffect {

    public final int particles;
    public final double xRot, yRot, zRot;
    public final double xFactor, yFactor;
    public final double factorInnerSpike;
    public final double compressYFactorTotal;
    public final float compilation;

    private HeartEffect(Builder b) {
        super(b);
        this.particles = b.particles;
        this.xRot = b.xRot;
        this.yRot = b.yRot;
        this.zRot = b.zRot;
        this.xFactor = b.xFactor;
        this.yFactor = b.yFactor;
        this.factorInnerSpike = b.factorInnerSpike;
        this.compressYFactorTotal = b.compressYFactorTotal;
        this.compilation = b.compilation;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        Vec3 base = Vec3.fromLocation(ctx.origin());

        for (int i = 0; i < particles; i++) {
            float alpha = ((float) (Math.PI / compilation) / particles) * i;
            double phi = Math.pow(
                    Math.abs(Math.sin(2 * compilation * alpha))
                            + factorInnerSpike * Math.abs(Math.sin(compilation * alpha)),
                    1.0 / compressYFactorTotal);

            Vec3 v = new Vec3(0,
                    phi * (Math.sin(alpha) + Math.cos(alpha)) * yFactor,
                    phi * (Math.cos(alpha) - Math.sin(alpha)) * xFactor)
                    .rotateX(xRot).rotateY(yRot).rotateZ(zRot);

            displayAbsolute(ctx, base.add(v));
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public int particles = 50;
        public double xRot = 0, yRot = 0, zRot = 0;
        public double xFactor = 1, yFactor = 1;
        public double factorInnerSpike = 0.8, compressYFactorTotal = 2;
        public float compilation = 2f;

        {
            particle = Particle.ENCHANTED_HIT;
        }

        @Contract(mutates = "this")
        public @NotNull Builder particles(int n) {
            particles = n;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder xFactor(double f) {
            xFactor = f;
            return self();
        }

        public @NotNull Builder yFactor(double f) {
            yFactor = f;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull HeartEffect build() {
            return new HeartEffect(this);
        }
    }
}