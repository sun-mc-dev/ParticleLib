package me.sunmc.particlelib.effects.shape;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class CuboidEffect extends AbstractEffect {

    public final int particles;
    public final double xLength, yLength, zLength;
    public final double padding;

    private boolean initialized = false;
    private Vec3 minCorner;
    private double uxLen, uyLen, uzLen;

    private CuboidEffect(Builder b) {
        super(b);
        this.particles = b.particles;
        this.xLength = b.xLength;
        this.yLength = b.yLength;
        this.zLength = b.zLength;
        this.padding = b.padding;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        if (iteration == 0) initialized = false;

        if (!initialized) {
            Location from = ctx.origin();
            Location target = ctx.target() != null ? ctx.target() : from;
            minCorner = Vec3.fromLocation(from);

            if (xLength == 0 && yLength == 0 && zLength == 0) {
                Vec3 tv = Vec3.fromLocation(target);
                minCorner = new Vec3(Math.min(minCorner.x(), tv.x()),
                        Math.min(minCorner.y(), tv.y()),
                        Math.min(minCorner.z(), tv.z()));
                uxLen = Math.abs(from.getX() - target.getX());
                uyLen = Math.abs(from.getY() - target.getY());
                uzLen = Math.abs(from.getZ() - target.getZ());
            } else {
                uxLen = xLength;
                uyLen = yLength;
                uzLen = zLength;
            }

            double extra = padding * 2;
            uxLen += extra;
            uyLen += extra;
            uzLen += extra;
            if (padding != 0) minCorner = minCorner.subtract(padding, padding, padding);
            initialized = true;
        }

        for (int i = 0; i < particles; i++) {
            drawEdge(ctx, i, 0, 2, 2);
            drawEdge(ctx, i, 0, 1, 2);
            drawEdge(ctx, i, 0, 1, 1);
            drawEdge(ctx, i, 0, 2, 1);
            drawEdge(ctx, i, 2, 0, 2);
            drawEdge(ctx, i, 1, 0, 2);
            drawEdge(ctx, i, 1, 0, 1);
            drawEdge(ctx, i, 2, 0, 1);
            drawEdge(ctx, i, 2, 2, 0);
            drawEdge(ctx, i, 1, 2, 0);
            drawEdge(ctx, i, 1, 1, 0);
            drawEdge(ctx, i, 2, 1, 0);
        }
    }

    private void drawEdge(EffectContext ctx, int i, int dx, int dy, int dz) {
        double vx = dx == 0 ? uxLen * i / particles : uxLen * (dx - 1);
        double vy = dy == 0 ? uyLen * i / particles : uyLen * (dy - 1);
        double vz = dz == 0 ? uzLen * i / particles : uzLen * (dz - 1);
        displayAbsolute(ctx, minCorner.add(vx, vy, vz));
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public int particles = 8;
        public double xLength = 0, yLength = 0, zLength = 0, padding = 0;

        {
            type = EffectType.REPEATING;
            period = 5;
            iterations = 200;
            particle = Particle.FLAME;
        }

        public @NotNull Builder size(double x, double y, double z) {
            xLength = x;
            yLength = y;
            zLength = z;
            return self();
        }

        public @NotNull Builder padding(double p) {
            padding = p;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull CuboidEffect build() {
            return new CuboidEffect(this);
        }
    }
}
