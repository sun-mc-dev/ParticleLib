package me.sunmc.particlelib.effects.ambient;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class MusicEffect extends AbstractEffect {
    public final double radialsPerStep;
    public final float radius;

    private MusicEffect(Builder b) {
        super(b);
        this.radialsPerStep = b.radialsPerStep;
        this.radius = b.radius;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        Vec3 base = Vec3.fromLocation(ctx.origin()).withY(Vec3.fromLocation(ctx.origin()).y() + 1.9);
        double angle = radialsPerStep * iteration;
        displayAbsolute(ctx, base.add(new Vec3(Math.cos(angle) * radius, 0, Math.sin(angle) * radius)));
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public double radialsPerStep = Math.PI / 8;
        public float radius = 0.4f;

        {
            type = EffectType.REPEATING;
            period = 1;
            iterations = 400;
            particle = Particle.NOTE;
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull MusicEffect build() {
            return new MusicEffect(this);
        }
    }
}