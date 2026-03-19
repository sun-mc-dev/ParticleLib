package me.sunmc.particlelib.effects.motion;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public final class FountainEffect extends AbstractEffect {

    public final int strands, particlesStrand, particlesSpout;
    public final float radius, radiusSpout, height, heightSpout;
    public final double rotation;

    private FountainEffect(Builder b) {
        super(b);
        this.strands = b.strands;
        this.particlesStrand = b.particlesStrand;
        this.particlesSpout = b.particlesSpout;
        this.radius = b.radius;
        this.radiusSpout = b.radiusSpout;
        this.height = b.height;
        this.heightSpout = b.heightSpout;
        this.rotation = b.rotation;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        Vec3 base = Vec3.fromLocation(ctx.origin());
        var rng = ThreadLocalRandom.current();

        for (int i = 1; i <= strands; i++) {
            double angle = 2 * i * Math.PI / strands + rotation;
            for (int j = 1; j <= particlesStrand; j++) {
                float ratio = (float) j / particlesStrand;
                Vec3 v = new Vec3(
                        Math.cos(angle) * radius * ratio,
                        Math.sin(Math.PI * j / particlesStrand) * height,
                        Math.sin(angle) * radius * ratio);
                displayAbsolute(ctx, base.add(v));
            }
        }

        for (int i = 0; i < particlesSpout; i++) {
            double a = rng.nextDouble() * 2 * Math.PI;
            double r = rng.nextDouble() * radius * radiusSpout;
            Vec3 v = new Vec3(Math.cos(a) * r, rng.nextDouble() * heightSpout, Math.sin(a) * r);
            displayAbsolute(ctx, base.add(v));
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public int strands = 10, particlesStrand = 150, particlesSpout = 200;
        public float radius = 5f, radiusSpout = 0.1f, height = 3f, heightSpout = 7f;
        public double rotation = Math.PI / 4;

        {
            type = EffectType.REPEATING;
            period = 2;
            iterations = 100;
            particle = Particle.SPLASH;
        }

        @Contract(mutates = "this")
        public @NotNull Builder strands(int n) {
            strands = n;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder radius(float r) {
            radius = r;
            return self();
        }

        public @NotNull Builder height(float h) {
            height = h;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull FountainEffect build() {
            return new FountainEffect(this);
        }
    }
}