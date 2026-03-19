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

public final class SmokeEffect extends AbstractEffect {
    public final int particles;

    private SmokeEffect(Builder b) {
        super(b);
        this.particles = b.particles;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        Vec3 base = Vec3.fromLocation(ctx.origin());
        var rng = ThreadLocalRandom.current();
        for (int i = 0; i < particles; i++) {
            double angle = rng.nextDouble() * 2 * Math.PI;
            double r = rng.nextDouble() * 0.6;
            Vec3 v = new Vec3(Math.cos(angle) * r, rng.nextDouble() * 2, Math.sin(angle) * r);
            displayAbsolute(ctx, base.add(v));
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public int particles = 20;

        {
            type = EffectType.REPEATING;
            period = 1;
            iterations = 300;
            particle = Particle.SMOKE;
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
        public @NotNull SmokeEffect build() {
            return new SmokeEffect(this);
        }
    }
}