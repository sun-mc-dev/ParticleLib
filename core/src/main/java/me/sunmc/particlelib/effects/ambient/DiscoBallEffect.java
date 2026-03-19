package me.sunmc.particlelib.effects.ambient;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.api.particle.ParticleOptions;
import me.sunmc.particlelib.effects.AbstractEffect;
import me.sunmc.particlelib.internal.particle.PaperParticleSpawner;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public final class DiscoBallEffect extends AbstractEffect {
    public final float sphereRadius;
    public final int max, maxLines, lineParticles, sphereParticles;
    public final Particle sphereParticle, lineParticle;
    public final @Nullable Color sphereColor, lineColor;

    private DiscoBallEffect(Builder b) {
        super(b);
        this.sphereRadius = b.sphereRadius;
        this.max = b.max;
        this.maxLines = b.maxLines;
        this.lineParticles = b.lineParticles;
        this.sphereParticles = b.sphereParticles;
        this.sphereParticle = b.sphereParticle;
        this.lineParticle = b.lineParticle;
        this.sphereColor = b.sphereColor;
        this.lineColor = b.lineColor;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        Vec3 base = Vec3.fromLocation(ctx.origin());
        var rng = ThreadLocalRandom.current();
        int mL = rng.nextInt(maxLines - 2) + 2;

        // lines
        ParticleOptions lo = baseParticleOptions.withParticle(lineParticle).withCount(1);
        if (lineColor != null) lo = lo.withColor(lineColor);
        for (int m = 0; m < mL * 2; m++) {
            double x = rng.nextInt(max * 2) - max;
            double y = rng.nextInt(max * 2) - max;
            double z = rng.nextInt(max * 2) - max;
            Vec3 target = base.subtract(x, y, z);
            Vec3 link = target.subtract(base).normalize();
            double len = target.distanceTo(base);
            Vec3 step = link.multiply(len / lineParticles);
            Vec3 cur = base.subtract(step);
            for (int i = 0; i < lineParticles; i++) {
                cur = cur.add(step);
                final ParticleOptions flo = lo;
                PaperParticleSpawner.getInstance()
                        .spawn(flo, cur.toLocation(ctx.world()), ctx.targetPlayers(), visibleRange);
            }
        }

        // sphere
        ParticleOptions so = baseParticleOptions.withParticle(sphereParticle).withCount(1);
        if (sphereColor != null) so = so.withColor(sphereColor);
        for (int i = 0; i < sphereParticles; i++) {
            Vec3 v = randomUnit(rng).multiply(sphereRadius);
            PaperParticleSpawner.getInstance()
                    .spawn(so, base.add(v).toLocation(ctx.world()), ctx.targetPlayers(), visibleRange);
        }
    }

    private @NotNull Vec3 randomUnit(@NotNull ThreadLocalRandom rng) {
        double u = rng.nextDouble(), v = rng.nextDouble();
        double theta = u * 2 * Math.PI, phi = Math.acos(2 * v - 1);
        return new Vec3(Math.sin(phi) * Math.cos(theta), Math.cos(phi), Math.sin(phi) * Math.sin(theta));
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public float sphereRadius = 0.6f;
        public int max = 15, maxLines = 7, lineParticles = 100, sphereParticles = 50;
        public Particle sphereParticle = Particle.FLAME, lineParticle = Particle.DUST;
        public @Nullable Color sphereColor = null, lineColor = null;

        {
            type = EffectType.REPEATING;
            period = 7;
            iterations = 500;
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull DiscoBallEffect build() {
            return new DiscoBallEffect(this);
        }
    }
}