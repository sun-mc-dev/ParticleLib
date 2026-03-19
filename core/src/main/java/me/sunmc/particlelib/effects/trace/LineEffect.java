package me.sunmc.particlelib.effects.trace;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class LineEffect extends AbstractEffect {

    public final int particles;
    public final double length;     // 0 = use target
    public final double maxLength;  // 0 = no cap
    public final boolean isZigZag;
    public final int zigZags;
    public final Vec3 zigZagOffset;
    public final Vec3 zigZagRelativeOffset;

    private LineEffect(Builder b) {
        super(b);
        this.particles = b.particles;
        this.length = b.length;
        this.maxLength = b.maxLength;
        this.isZigZag = b.isZigZag;
        this.zigZags = b.zigZags;
        this.zigZagOffset = b.zigZagOffset;
        this.zigZagRelativeOffset = b.zigZagRelativeOffset;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        Location from = ctx.origin();
        Location to;

        if (length > 0) {
            Vec3 dir = Vec3.fromVector(from.getDirection()).normalize().multiply(length);
            to = Vec3.fromLocation(from).add(dir).toLocation(ctx.world(), from.getYaw(), from.getPitch());
        } else {
            to = ctx.target();
        }
        if (to == null) return;

        Vec3 vFrom = Vec3.fromLocation(from);
        Vec3 link = Vec3.fromLocation(to).subtract(vFrom);
        double len = link.length();
        if (maxLength > 0) len = Math.min(len, maxLength);

        Vec3 step = link.normalize().multiply(len / particles);
        Vec3 cur = vFrom.subtract(step);
        int zagCount = 0;
        boolean zag = false;
        int zagPer = Math.max(1, particles / Math.max(1, zigZags));

        for (int i = 0; i < particles; i++) {
            if (isZigZag) {
                Vec3 rel = zigZagRelativeOffset.rotateByLocation(from);
                if (zag) cur = cur.add(rel).add(zigZagOffset);
                else cur = cur.subtract(rel).subtract(zigZagOffset);
                if (++zagCount >= zagPer) {
                    zag = !zag;
                    zagCount = 0;
                }
            }
            cur = cur.add(step);
            displayAbsolute(ctx, cur);
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public int particles = 100;
        public double length = 0, maxLength = 0;
        public boolean isZigZag = false;
        public int zigZags = 10;
        public Vec3 zigZagOffset = new Vec3(0, 0.1, 0);
        public Vec3 zigZagRelativeOffset = Vec3.ZERO;

        {
            type = EffectType.REPEATING;
            period = 1;
            iterations = 1;
            particle = Particle.FLAME;
        }

        @Contract(mutates = "this")
        public @NotNull Builder particles(int n) {
            particles = n;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder length(double l) {
            length = l;
            return self();
        }

        public @NotNull Builder maxLength(double l) {
            maxLength = l;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder zigZag(boolean b) {
            isZigZag = b;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder zigZags(int n) {
            zigZags = n;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull LineEffect build() {
            return new LineEffect(this);
        }
    }
}
