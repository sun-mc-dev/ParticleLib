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

public final class SquareEffect extends AbstractEffect {

    public final double radius;
    public final double yOffset;
    public final double radiusIncrease;
    public final int particleIncrease;

    private double curRadius;
    private int curParticles;

    private SquareEffect(Builder b) {
        super(b);
        this.radius = b.radius;
        this.yOffset = b.yOffset;
        this.radiusIncrease = b.radiusIncrease;
        this.particleIncrease = b.particleIncrease;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        if (iteration == 0) {
            curRadius = radius;
            curParticles = baseParticleOptions.count();
        }
        if (radiusIncrease != 0) curRadius += radiusIncrease;
        if (particleIncrease != 0) curParticles += particleIncrease;

        Vec3 base = Vec3.fromLocation(ctx.origin()).withY(Vec3.fromLocation(ctx.origin()).y() + yOffset);
        var rng = ThreadLocalRandom.current();

        for (int i = 0; i < curParticles; i++) {
            double x = (rng.nextDouble() * 2 - 1) * curRadius;
            double z = (rng.nextDouble() * 2 - 1) * curRadius;
            displayAbsolute(ctx, base.add(new Vec3(x, 0, z)));
        }
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

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull SquareEffect build() {
            return new SquareEffect(this);
        }
    }
}
