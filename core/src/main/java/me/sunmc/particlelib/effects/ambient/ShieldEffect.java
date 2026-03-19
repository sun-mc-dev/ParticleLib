package me.sunmc.particlelib.effects.ambient;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public final class ShieldEffect extends AbstractEffect {
    public final double radius;
    public final int particles;
    public final boolean sphere;
    public final boolean reverse;

    private ShieldEffect(Builder b) {
        super(b);
        this.radius = b.radius;
        this.particles = b.particles;
        this.sphere = b.sphere;
        this.reverse = b.reverse;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        Vec3 base = Vec3.fromLocation(ctx.origin());
        var rng = ThreadLocalRandom.current();
        for (int i = 0; i < particles; i++) {
            Vec3 v = randomUnit(rng).multiply(radius);
            if (!sphere) v = v.withY(reverse ? -Math.abs(v.y()) : Math.abs(v.y()));
            displayAbsolute(ctx, base.add(v));
        }
    }

    private @NotNull Vec3 randomUnit(@NotNull ThreadLocalRandom rng) {
        double u = rng.nextDouble(), v = rng.nextDouble();
        double theta = u * 2 * Math.PI, phi = Math.acos(2 * v - 1);
        return new Vec3(Math.sin(phi) * Math.cos(theta), Math.cos(phi), Math.sin(phi) * Math.sin(theta));
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public double radius = 3.0;
        public int particles = 50;
        public boolean sphere = false, reverse = false;

        {
            type = EffectType.REPEATING;
            period = 1;
            iterations = 500;
            particle = Particle.FLAME;
        }

        @Contract(mutates = "this")
        public @NotNull Builder radius(double r) {
            radius = r;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder sphere(boolean b) {
            sphere = b;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull ShieldEffect build() {
            return new ShieldEffect(this);
        }
    }
}