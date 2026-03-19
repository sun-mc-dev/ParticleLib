package me.sunmc.particlelib.effects.ambient;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class IconEffect extends AbstractEffect {
    public final int yOffset;

    private IconEffect(Builder b) {
        super(b);
        this.yOffset = b.yOffset;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        Vec3 base = Vec3.fromLocation(ctx.origin()).withY(Vec3.fromLocation(ctx.origin()).y() + yOffset);
        displayAbsolute(ctx, base);
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public int yOffset = 2;

        {
            type = EffectType.REPEATING;
            period = 4;
            iterations = 25;
            particle = Particle.ANGRY_VILLAGER;
        }

        @Contract(mutates = "this")
        public @NotNull Builder yOffset(int y) {
            yOffset = y;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull IconEffect build() {
            return new IconEffect(this);
        }
    }
}