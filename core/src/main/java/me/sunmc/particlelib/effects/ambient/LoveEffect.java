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

public final class LoveEffect extends AbstractEffect {
    private LoveEffect(Builder b) {
        super(b);
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        var rng = ThreadLocalRandom.current();
        Vec3 base = Vec3.fromLocation(ctx.origin());
        double angle = rng.nextDouble() * 2 * Math.PI;
        double r = rng.nextDouble() * 0.6;
        double yAdd = rng.nextDouble() * 2;
        displayAbsolute(ctx, base.add(new Vec3(Math.cos(angle) * r, yAdd, Math.sin(angle) * r)));
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        {
            type = EffectType.REPEATING;
            period = 2;
            iterations = 600;
            particle = Particle.HEART;
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull LoveEffect build() {
            return new LoveEffect(this);
        }
    }
}
