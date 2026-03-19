package me.sunmc.particlelib.effects.shape;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public final class SphereEffect extends AbstractEffect {

    public final double radius;
    public final double yOffset;
    public final double radiusIncrease;
    public final int particleIncrease;

    private double currentRadius;
    private int currentParticles;

    private SphereEffect(Builder b) {
        super(b);
        this.radius = b.radius;
        this.yOffset = b.yOffset;
        this.radiusIncrease = b.radiusIncrease;
        this.particleIncrease = b.particleIncrease;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        if (iteration == 0) {
            currentRadius = radius;
            currentParticles = baseParticleOptions.count();
        }
        if (radiusIncrease != 0) currentRadius += radiusIncrease;
        if (particleIncrease != 0) currentParticles += particleIncrease;

        var opts = baseParticleOptions.withCount(currentParticles);
        var rng = ThreadLocalRandom.current();

        for (int i = 0; i < currentParticles; i++) {
            Vec3 v = randomOnSphere(rng, currentRadius);
            displayAbsolute(ctx, Vec3.fromLocation(ctx.origin())
                    .add(v).withY(Vec3.fromLocation(ctx.origin()).y() + yOffset + v.y()));
        }
    }

    private @NotNull Vec3 randomOnSphere(@NotNull ThreadLocalRandom rng, double r) {
        double u = rng.nextDouble(), v2 = rng.nextDouble();
        double theta = u * 2 * Math.PI;
        double phi = Math.acos(2 * v2 - 1);
        return new Vec3(r * Math.sin(phi) * Math.cos(theta),
                r * Math.cos(phi),
                r * Math.sin(phi) * Math.sin(theta));
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public double radius = 0.6, yOffset = 0, radiusIncrease = 0;
        public int particleIncrease = 0;

        {
            type = EffectType.REPEATING;
            period = 1;
            iterations = 500;
            particle = Particle.ENTITY_EFFECT;
        }

        @Contract(mutates = "this")
        public @NotNull Builder radius(double r) {
            radius = r;
            return self();
        }

        public @NotNull Builder yOffset(double y) {
            yOffset = y;
            return self();
        }

        public @NotNull Builder radiusIncrease(double d) {
            radiusIncrease = d;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder particleIncrease(int n) {
            particleIncrease = n;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull SphereEffect build() {
            return new SphereEffect(this);
        }
    }
}
