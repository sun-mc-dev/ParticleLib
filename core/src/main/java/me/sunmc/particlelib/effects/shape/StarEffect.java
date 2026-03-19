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

public final class StarEffect extends AbstractEffect {

    public final int particles;
    public final float spikeHeight;
    public final int spikesHalf;
    public final float innerRadius;

    private StarEffect(Builder b) {
        super(b);
        this.particles = b.particles;
        this.spikeHeight = b.spikeHeight;
        this.spikesHalf = b.spikesHalf;
        this.innerRadius = b.innerRadius;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        float radius = (float) (3 * innerRadius / Math.sqrt(3));
        Vec3 base = Vec3.fromLocation(ctx.origin());
        var rng = ThreadLocalRandom.current();

        for (int i = 0; i < spikesHalf * 2; i++) {
            double xRot = i * Math.PI / spikesHalf;
            for (int x = 0; x < particles; x++) {
                double angle = 2 * Math.PI * x / particles;
                float height = rng.nextFloat() * spikeHeight;
                Vec3 v = new Vec3(Math.cos(angle), 0, Math.sin(angle))
                        .multiply((spikeHeight - height) * radius / spikeHeight)
                        .withY(innerRadius + height)
                        .rotateX(xRot);
                displayAbsolute(ctx, base.add(v));

                // mirror
                v = v.rotateX(Math.PI).rotateY(Math.PI / 2);
                displayAbsolute(ctx, base.add(v));
            }
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public int particles = 50, spikesHalf = 3;
        public float spikeHeight = 3.5f, innerRadius = 0.5f;

        {
            type = EffectType.REPEATING;
            period = 4;
            iterations = 50;
            particle = Particle.FLAME;
        }

        @Contract(mutates = "this")
        public @NotNull Builder particles(int n) {
            particles = n;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder spikeHeight(float h) {
            spikeHeight = h;
            return self();
        }

        public @NotNull Builder spikesHalf(int n) {
            spikesHalf = n;
            return self();
        }

        public @NotNull Builder innerRadius(float r) {
            innerRadius = r;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull StarEffect build() {
            return new StarEffect(this);
        }
    }
}
