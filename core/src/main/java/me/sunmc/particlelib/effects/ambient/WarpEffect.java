package me.sunmc.particlelib.effects.ambient;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class WarpEffect extends AbstractEffect {
    public final float radius, grow;
    public final int particles, rings;

    private WarpEffect(Builder b) {
        super(b);
        this.radius = b.radius;
        this.grow = b.grow;
        this.particles = b.particles;
        this.rings = b.rings;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        int curStep = iteration % (rings + 1);
        Vec3 base = Vec3.fromLocation(ctx.origin()).withY(Vec3.fromLocation(ctx.origin()).y() + curStep * grow);
        for (int i = 0; i < particles; i++) {
            double angle = 2 * Math.PI * i / particles;
            displayAbsolute(ctx, base.add(new Vec3(Math.cos(angle) * radius, 0, Math.sin(angle) * radius)));
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public float radius = 1f, grow = 0.2f;
        public int particles = 20, rings = 12;

        {
            type = EffectType.REPEATING;
            period = 2;
            iterations = 100;
            particle = Particle.FIREWORK;
        }

        @Contract(mutates = "this")
        public @NotNull Builder radius(float r) {
            radius = r;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull WarpEffect build() {
            return new WarpEffect(this);
        }
    }
}