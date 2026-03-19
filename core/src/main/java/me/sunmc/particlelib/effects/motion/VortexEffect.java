package me.sunmc.particlelib.effects.motion;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class VortexEffect extends AbstractEffect {

    public final float radius;
    public final float radiusGrow;
    public final float initRange;
    public final float grow;
    public final double radials;
    public final int circles;
    public final int helixes;

    private int step = 0;

    private VortexEffect(Builder b) {
        super(b);
        this.radius = b.radius;
        this.radiusGrow = b.radiusGrow;
        this.initRange = b.initRange;
        this.grow = b.grow;
        this.radials = b.radials;
        this.circles = b.circles;
        this.helixes = b.helixes;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        if (iteration == 0) step = 0;
        var loc = ctx.origin();

        for (int x = 0; x < circles; x++) {
            for (int i = 0; i < helixes; i++) {
                double angle = step * radials + (2 * Math.PI * i / helixes);
                double r = radius + step * radiusGrow;
                Vec3 v = new Vec3(Math.cos(angle) * r, initRange + step * grow, Math.sin(angle) * r);
                v = v.rotateX(Math.toRadians(loc.getPitch() + 90))
                        .rotateY(Math.toRadians(-loc.getYaw()));
                displayAbsolute(ctx, Vec3.fromLocation(loc).add(v));
            }
            step++;
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public float radius = 2f, radiusGrow = 0f, initRange = 0f, grow = 0.05f;
        public double radials = Math.PI / 16;
        public int circles = 3, helixes = 4;

        {
            type = EffectType.REPEATING;
            period = 1;
            iterations = 200;
            particle = Particle.FLAME;
        }

        @Contract(mutates = "this")
        public @NotNull Builder radius(float r) {
            radius = r;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder circles(int c) {
            circles = c;
            return self();
        }

        public @NotNull Builder helixes(int h) {
            helixes = h;
            return self();
        }

        public @NotNull Builder grow(float g) {
            grow = g;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull VortexEffect build() {
            return new VortexEffect(this);
        }
    }
}