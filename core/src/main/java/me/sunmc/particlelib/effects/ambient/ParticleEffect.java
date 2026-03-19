package me.sunmc.particlelib.effects.ambient;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ParticleEffect extends AbstractEffect {
    private ParticleEffect(Builder b) {
        super(b);
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        display(ctx, ctx.origin());
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        {
            type = EffectType.REPEATING;
            period = 1;
            iterations = 1;
            particle = Particle.ANGRY_VILLAGER;
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull ParticleEffect build() {
            return new ParticleEffect(this);
        }
    }
}