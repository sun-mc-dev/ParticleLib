package me.sunmc.particlelib.effects.trace;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class HelixEffect extends AbstractEffect {

    public final int strands, particles;
    public final float radius, curve;
    public final double rotation;
    public final boolean orient, enableRotation;
    public final double xRot, yRot, zRot;
    public final double angVelX, angVelY, angVelZ;

    private float step = 0;

    private HelixEffect(Builder b) {
        super(b);
        this.strands = b.strands;
        this.particles = b.particles;
        this.radius = b.radius;
        this.curve = b.curve;
        this.rotation = b.rotation;
        this.orient = b.orient;
        this.enableRotation = b.enableRotation;
        this.xRot = b.xRot;
        this.yRot = b.yRot;
        this.zRot = b.zRot;
        this.angVelX = b.angVelX;
        this.angVelY = b.angVelY;
        this.angVelZ = b.angVelZ;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        if (iteration == 0) step = 0;
        Vec3 base = Vec3.fromLocation(ctx.origin());

        for (int i = 1; i <= strands; i++) {
            for (int j = 1; j <= particles; j++) {
                float ratio = (float) j / particles;
                double angle = curve * ratio * 2 * Math.PI / strands
                        + (2 * Math.PI * i / strands) + rotation;

                Vec3 v = new Vec3(Math.cos(angle) * ratio * radius, 0, Math.sin(angle) * ratio * radius)
                        .rotateX(xRot).rotateY(yRot).rotateZ(zRot);

                if (enableRotation)
                    v = v.rotateX(angVelX * step).rotateY(angVelY * step).rotateZ(angVelZ * step);
                if (orient)
                    v = v.rotateByLocation(ctx.origin());

                displayAbsolute(ctx, base.add(v));
                step++;
            }
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public int strands = 8, particles = 80;
        public float radius = 10f, curve = 10f;
        public double rotation = Math.PI / 4;
        public boolean orient = false, enableRotation = false;
        public double xRot = 0, yRot = 0, zRot = 0;
        public double angVelX = Math.PI / 200, angVelY = Math.PI / 170, angVelZ = Math.PI / 155;

        {
            type = EffectType.REPEATING;
            period = 10;
            iterations = 8;
            particle = Particle.FLAME;
        }

        @Contract(mutates = "this")
        public @NotNull Builder strands(int n) {
            strands = n;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder radius(float r) {
            radius = r;
            return self();
        }

        public @NotNull Builder curve(float c) {
            curve = c;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder orient(boolean o) {
            orient = o;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull HelixEffect build() {
            return new HelixEffect(this);
        }
    }
}