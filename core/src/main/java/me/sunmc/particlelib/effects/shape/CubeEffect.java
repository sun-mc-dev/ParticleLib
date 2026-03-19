package me.sunmc.particlelib.effects.shape;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class CubeEffect extends AbstractEffect {

    public final float edgeLength;
    public final int particles;
    public final boolean outlineOnly;
    public final boolean enableRotation;
    public final boolean orient;
    public final double angVelX, angVelY, angVelZ;

    private int step = 0;

    private CubeEffect(Builder b) {
        super(b);
        this.edgeLength = b.edgeLength;
        this.particles = b.particles;
        this.outlineOnly = b.outlineOnly;
        this.enableRotation = b.enableRotation;
        this.orient = b.orient;
        this.angVelX = b.angVelX;
        this.angVelY = b.angVelY;
        this.angVelZ = b.angVelZ;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        if (iteration == 0) step = 0;
        double rx = enableRotation ? step * angVelX : 0;
        double ry = enableRotation ? step * angVelY : 0;
        double rz = enableRotation ? step * angVelZ : 0;
        float a = edgeLength / 2f;
        var loc = ctx.origin();
        Vec3 base = Vec3.fromLocation(loc);

        for (int face = 0; face < 4; face++) {
            double ay = face * Math.PI / 2;
            // top/bottom edges
            for (int j = 0; j < 2; j++) {
                double ax = j * Math.PI;
                for (int p = 0; p <= particles; p++) {
                    Vec3 v = new Vec3(a, a, edgeLength * p / particles - a);
                    v = v.rotateX(ax).rotateY(ay);
                    v = applyRotAndOrient(v, rx, ry, rz, orient, loc);
                    displayAbsolute(ctx, base.add(v));
                }
            }
            // pillar edges
            for (int p = 0; p <= particles; p++) {
                Vec3 v = new Vec3(a, edgeLength * p / particles - a, a);
                v = v.rotateY(ay);
                v = applyRotAndOrient(v, rx, ry, rz, orient, loc);
                displayAbsolute(ctx, base.add(v));
            }
        }
        step++;
    }

    private Vec3 applyRotAndOrient(Vec3 v, double rx, double ry, double rz,
                                   boolean orient, org.bukkit.Location loc) {
        if (enableRotation) v = v.rotateX(rx).rotateY(ry).rotateZ(rz);
        if (orient) v = v.rotateByLocation(loc);
        return v;
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public float edgeLength = 3f;
        public int particles = 8;
        public boolean outlineOnly = true, enableRotation = true, orient = false;
        public double angVelX = Math.PI / 200, angVelY = Math.PI / 170, angVelZ = Math.PI / 155;

        {
            type = EffectType.REPEATING;
            period = 5;
            iterations = 200;
            particle = Particle.FLAME;
        }

        @Contract(mutates = "this")
        public @NotNull Builder edgeLength(float e) {
            edgeLength = e;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder particles(int n) {
            particles = n;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull CubeEffect build() {
            return new CubeEffect(this);
        }
    }
}