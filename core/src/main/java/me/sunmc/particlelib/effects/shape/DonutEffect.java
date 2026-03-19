package me.sunmc.particlelib.effects.shape;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class DonutEffect extends AbstractEffect {

    public final int particlesCircle;
    public final int circles;
    public final float radiusDonut;
    public final float radiusTube;
    public final double xRot, yRot, zRot;

    private DonutEffect(Builder b) {
        super(b);
        this.particlesCircle = b.particlesCircle;
        this.circles = b.circles;
        this.radiusDonut = b.radiusDonut;
        this.radiusTube = b.radiusTube;
        this.xRot = b.xRot;
        this.yRot = b.yRot;
        this.zRot = b.zRot;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        var loc = ctx.origin();
        Vec3 base = Vec3.fromLocation(loc);

        for (int i = 0; i < circles; i++) {
            double theta = 2 * Math.PI * i / circles;
            for (int j = 0; j < particlesCircle; j++) {
                double phi = 2 * Math.PI * j / particlesCircle;
                double cosPhi = Math.cos(phi);
                Vec3 v = new Vec3(
                        (radiusDonut + radiusTube * cosPhi) * Math.cos(theta),
                        (radiusDonut + radiusTube * cosPhi) * Math.sin(theta),
                        radiusTube * Math.sin(phi));
                v = v.rotateX(xRot).rotateY(yRot).rotateZ(zRot);
                v = v.rotateX(Math.toRadians(loc.getPitch() + 90))
                        .rotateY(Math.toRadians(-loc.getYaw()));
                displayAbsolute(ctx, base.add(v));
            }
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public int particlesCircle = 10, circles = 36;
        public float radiusDonut = 2f, radiusTube = 0.5f;
        public double xRot = 0, yRot = 0, zRot = 0;

        {
            type = EffectType.REPEATING;
            period = 10;
            iterations = 20;
            particle = Particle.FLAME;
        }

        @Contract(mutates = "this")
        public @NotNull Builder radiusDonut(float r) {
            radiusDonut = r;
            return self();
        }

        public @NotNull Builder radiusTube(float r) {
            radiusTube = r;
            return self();
        }

        public @NotNull Builder circles(int c) {
            circles = c;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull DonutEffect build() {
            return new DonutEffect(this);
        }
    }
}
