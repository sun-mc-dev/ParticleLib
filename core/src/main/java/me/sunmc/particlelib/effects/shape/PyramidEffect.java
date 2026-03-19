package me.sunmc.particlelib.effects.shape;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class PyramidEffect extends AbstractEffect {

    public final int particles;
    public final double radius;

    private PyramidEffect(Builder b) {
        super(b);
        this.particles = b.particles;
        this.radius = b.radius;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        Vec3 base = Vec3.fromLocation(ctx.origin());
        for (int i = 0; i < particles; i++) {
            drawEdge(ctx, base, i, 0, 0, -1);
            drawEdge(ctx, base, i, 0, 0, 1);
            drawEdge(ctx, base, i, -1, 0, 0);
            drawEdge(ctx, base, i, 1, 0, 0);
            drawEdge(ctx, base, i, -1, 1, -1);
            drawEdge(ctx, base, i, -1, 1, 1);
            drawEdge(ctx, base, i, 1, 1, -1);
            drawEdge(ctx, base, i, 1, 1, 1);
        }
    }

    private void drawEdge(EffectContext ctx, Vec3 base, int i, int dx, int dy, int dz) {
        double ratio = (double) i / particles;
        double vx, vy, vz;
        if (dy == 1) {
            vy = ratio;
            vx = dx < 0 ? ratio - 1 : 1 - ratio;
            vz = dz < 0 ? ratio - 1 : 1 - ratio;
        } else {
            vy = 0;
            vx = dx == 0 ? ratio * 2 - 1 : dx;
            vz = dz == 0 ? ratio * 2 - 1 : dz;
        }
        displayAbsolute(ctx, base.add(new Vec3(vx, vy, vz).multiply(radius)));
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public int particles = 8;
        public double radius = 1.0;

        {
            type = EffectType.REPEATING;
            period = 5;
            iterations = 200;
            particle = Particle.FLAME;
        }

        @Contract(mutates = "this")
        public @NotNull Builder particles(int n) {
            particles = n;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder radius(double r) {
            radius = r;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull PyramidEffect build() {
            return new PyramidEffect(this);
        }
    }
}
