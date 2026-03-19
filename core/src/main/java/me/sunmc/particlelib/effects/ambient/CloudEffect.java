package me.sunmc.particlelib.effects.ambient;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.api.particle.ParticleOptions;
import me.sunmc.particlelib.effects.AbstractEffect;
import me.sunmc.particlelib.internal.particle.PaperParticleSpawner;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public final class CloudEffect extends AbstractEffect {
    public final Particle cloudParticle;
    public final @Nullable Color cloudColor;
    public final float cloudSpeed;
    public final int cloudParticles;
    public final Particle mainParticle;
    public final int mainParticles;
    public final float cloudSize;
    public final float particleRadius;
    public final double yOffset;
    public final boolean increaseHeight;

    private CloudEffect(Builder b) {
        super(b);
        this.cloudParticle = b.cloudParticle;
        this.cloudColor = b.cloudColor;
        this.cloudSpeed = b.cloudSpeed;
        this.cloudParticles = b.cloudParticles;
        this.mainParticle = b.mainParticle;
        this.mainParticles = b.mainParticles;
        this.cloudSize = b.cloudSize;
        this.particleRadius = b.particleRadius;
        this.yOffset = b.yOffset;
        this.increaseHeight = b.increaseHeight;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        Vec3 base = Vec3.fromLocation(ctx.origin()).withY(Vec3.fromLocation(ctx.origin()).y() + yOffset);
        var rng = ThreadLocalRandom.current();
        ParticleOptions co = baseParticleOptions.withParticle(cloudParticle)
                .withSpeed(cloudSpeed).withCount(1);
        if (cloudColor != null) co = co.withColor(cloudColor);

        for (int i = 0; i < cloudParticles; i++) {
            double angle = rng.nextDouble() * 2 * Math.PI;
            double r = rng.nextDouble() * cloudSize;
            Vec3 v = new Vec3(Math.cos(angle) * r, 0, Math.sin(angle) * r);
            display(ctx, base.add(v).toLocation(ctx.world()), co);
        }

        Vec3 rainBase = increaseHeight ? base.withY(base.y() + 0.2) : base;
        ParticleOptions mo = baseParticleOptions.withParticle(mainParticle).withCount(1);
        for (int i = 0; i < mainParticles; i++) {
            if (rng.nextBoolean()) {
                double x = (rng.nextDouble() * 2 - 1) * particleRadius;
                double z = (rng.nextDouble() * 2 - 1) * particleRadius;
                display(ctx, rainBase.add(new Vec3(x, 0, z)).toLocation(ctx.world()), mo);
            }
        }
    }

    protected void display(@NotNull EffectContext ctx, @NotNull Location loc, @NotNull ParticleOptions opts) {
        PaperParticleSpawner.getInstance()
                .spawn(opts, loc, ctx.targetPlayers(), visibleRange);
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public Particle cloudParticle = Particle.CLOUD;
        public @Nullable Color cloudColor = null;
        public float cloudSpeed = 0f, cloudSize = 0.7f, particleRadius = 0.6f;
        public int cloudParticles = 50, mainParticles = 15;
        public Particle mainParticle = Particle.DRIPPING_WATER;
        public double yOffset = 0.8;
        public boolean increaseHeight = true;

        {
            type = EffectType.REPEATING;
            period = 5;
            iterations = 50;
        }

        public @NotNull Builder cloudSize(float s) {
            cloudSize = s;
            particleRadius = s - 0.1f;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull CloudEffect build() {
            return new CloudEffect(this);
        }
    }
}
