package me.sunmc.particlelib.effects.shape;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class CircleEffect extends AbstractEffect {

    public final double radius;
    public final int particles;
    public final double maxAngle;
    public final boolean wholeCircle;
    public final boolean orient;
    public final boolean enableRotation;
    public final double angVelX, angVelY, angVelZ;
    public final double xRot, yRot, zRot;
    public final boolean resetCircle;

    private double step = 0;

    private CircleEffect(Builder b) {
        super(b);
        this.radius = b.radius;
        this.particles = b.particles;
        this.maxAngle = b.maxAngle;
        this.wholeCircle = b.wholeCircle;
        this.orient = b.orient;
        this.enableRotation = b.enableRotation;
        this.angVelX = b.angVelX;
        this.angVelY = b.angVelY;
        this.angVelZ = b.angVelZ;
        this.xRot = b.xRot;
        this.yRot = b.yRot;
        this.zRot = b.zRot;
        this.resetCircle = b.resetCircle;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        double inc = maxAngle / particles;
        int steps = wholeCircle ? particles : 1;

        for (int i = 0; i < steps; i++) {
            double angle = step * inc;
            Vec3 v = new Vec3(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);

            // apply fixed rotation
            v = v.rotateX(xRot).rotateY(yRot).rotateZ(zRot);

            // apply pitch/yaw from origin location
            var loc = ctx.origin();
            v = v.rotateX(Math.toRadians(loc.getPitch()))
                    .rotateY(Math.toRadians(-loc.getYaw()));

            // velocity rotation
            if (enableRotation) {
                v = v.rotateX(angVelX * step)
                        .rotateY(angVelY * step)
                        .rotateZ(angVelZ * step);
            }

            if (orient) v = v.rotateByLocation(loc);

            displayAbsolute(ctx, Vec3.fromLocation(loc).add(v));
            step++;
        }
        if (resetCircle) step = 0;
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public double radius = 0.4, maxAngle = Math.PI * 2;
        public int particles = 20;
        public boolean wholeCircle = false, orient = false;
        public boolean enableRotation = true, resetCircle = false;
        public double angVelX = Math.PI / 200, angVelY = Math.PI / 170, angVelZ = Math.PI / 155;
        public double xRot = 0, yRot = 0, zRot = 0;

        {
            type = EffectType.REPEATING;
            period = 2;
            iterations = 50;
            particle = Particle.HAPPY_VILLAGER;
        }

        @Contract(mutates = "this")
        public @NotNull Builder radius(double r) {
            radius = r;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder particles(int n) {
            particles = n;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder wholeCircle(boolean b) {
            wholeCircle = b;
            return self();
        }

        public @NotNull Builder orient(boolean b) {
            orient = b;
            return self();
        }

        public @NotNull Builder maxAngle(double a) {
            maxAngle = a;
            return self();
        }

        public @NotNull Builder rotation(double x, double y, double z) {
            xRot = x;
            yRot = y;
            zRot = z;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull CircleEffect build() {
            return new CircleEffect(this);
        }
    }
}
