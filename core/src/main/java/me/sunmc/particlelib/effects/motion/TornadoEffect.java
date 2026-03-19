package me.sunmc.particlelib.effects.motion;

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

public final class TornadoEffect extends AbstractEffect {

    public final @Nullable Color tornadoColor;
    public final Particle cloudParticle;
    public final @Nullable Color cloudColor;
    public final float cloudSize;
    public final double yOffset;
    public final float tornadoHeight;
    public final float maxTornadoRadius;
    public final boolean showCloud;
    public final boolean showTornado;
    public final double distance;
    public final int circleParticles;
    public final int cloudParticles;
    public final double circleHeight;

    private TornadoEffect(Builder b) {
        super(b);
        this.tornadoColor = b.tornadoColor;
        this.cloudParticle = b.cloudParticle;
        this.cloudColor = b.cloudColor;
        this.cloudSize = b.cloudSize;
        this.yOffset = b.yOffset;
        this.tornadoHeight = b.tornadoHeight;
        this.maxTornadoRadius = b.maxTornadoRadius;
        this.showCloud = b.showCloud;
        this.showTornado = b.showTornado;
        this.distance = b.distance;
        this.circleParticles = b.circleParticles;
        this.cloudParticles = b.cloudParticles;
        this.circleHeight = b.circleHeight;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        Location originLoc = ctx.origin().clone().add(0, yOffset, 0);
        Vec3 base = Vec3.fromLocation(originLoc);
        var rng = ThreadLocalRandom.current();

        // cloud
        if (showCloud) {
            ParticleOptions co = baseParticleOptions.withParticle(cloudParticle)
                    .withCount(1).withSpeed(cloudSize > 0 ? cloudSize : 0.01f);
            if (cloudColor != null) co = co.withColor(cloudColor);
            for (int i = 0; i < (int) (cloudParticles * cloudSize); i++) {
                Vec3 v = randomCircle(rng, cloudSize);
                display(ctx, base.add(v).toLocation(ctx.world()), co);
            }
        }

        // tornado body
        if (showTornado) {
            Vec3 top = base.withY(base.y() + 0.2);
            double r = 0.45 * (maxTornadoRadius * (2.35 / tornadoHeight));
            double fr;
            for (double y = 0; y < tornadoHeight; y += distance) {
                fr = Math.min(r * y, maxTornadoRadius);
                int amount = (int) (fr * circleParticles);
                double inc = amount > 0 ? (2 * Math.PI) / amount : 0;
                for (int i = 0; i < amount; i++) {
                    double angle = i * inc;
                    Vec3 yJitter = circleHeight > 0
                            ? new Vec3(0, rng.nextDouble() * circleHeight - circleHeight / 2, 0)
                            : Vec3.ZERO;
                    Vec3 v = new Vec3(fr * Math.cos(angle), y, fr * Math.sin(angle)).add(yJitter);
                    ParticleOptions to = baseParticleOptions.withCount(1);
                    if (tornadoColor != null) to = to.withColor(tornadoColor);
                    display(ctx, top.add(v).toLocation(ctx.world()), to);
                }
            }
        }
    }

    private @NotNull Vec3 randomCircle(@NotNull ThreadLocalRandom rng, double size) {
        double angle = rng.nextDouble() * 2 * Math.PI;
        double radius = rng.nextDouble() * size;
        return new Vec3(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
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
        public @Nullable Color tornadoColor = null;
        public Particle cloudParticle = Particle.CLOUD;
        public @Nullable Color cloudColor = null;
        public float cloudSize = 2.5f, tornadoHeight = 5f, maxTornadoRadius = 5f;
        public double yOffset = 0.8, distance = 0.375, circleHeight = 0;
        public int circleParticles = 64, cloudParticles = 100;
        public boolean showCloud = true, showTornado = true;

        {
            type = EffectType.REPEATING;
            period = 5;
            iterations = 20;
            particle = Particle.FLAME;
        }

        public @NotNull Builder height(float h) {
            tornadoHeight = h;
            return self();
        }

        public @NotNull Builder maxRadius(float r) {
            maxTornadoRadius = r;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull TornadoEffect build() {
            return new TornadoEffect(this);
        }
    }
}