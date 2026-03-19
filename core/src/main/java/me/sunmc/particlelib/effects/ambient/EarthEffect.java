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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class EarthEffect extends AbstractEffect {
    public final Particle landParticle, oceanParticle;
    public final @Nullable Color landColor, oceanColor;
    public final int precision, particles;
    public final float radius, mountainHeight;

    private List<Vec3> cacheGreen = null, cacheBlue = null;

    private EarthEffect(Builder b) {
        super(b);
        this.landParticle = b.landParticle;
        this.oceanParticle = b.oceanParticle;
        this.landColor = b.landColor;
        this.oceanColor = b.oceanColor;
        this.precision = b.precision;
        this.particles = b.particles;
        this.radius = b.radius;
        this.mountainHeight = b.mountainHeight;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        if (iteration == 0) buildCache();
        Vec3 base = Vec3.fromLocation(ctx.origin());

        ParticleOptions lo = baseParticleOptions.withParticle(landParticle);
        if (landColor != null) lo = lo.withColor(landColor);
        for (Vec3 v : cacheGreen) spawnAt(ctx, base.add(v), lo);

        ParticleOptions oo = baseParticleOptions.withParticle(oceanParticle);
        if (oceanColor != null) oo = oo.withColor(oceanColor);
        for (Vec3 v : cacheBlue) spawnAt(ctx, base.add(v), oo);
    }

    private void spawnAt(@NotNull EffectContext ctx, @NotNull Vec3 pos, ParticleOptions opts) {
        PaperParticleSpawner.getInstance()
                .spawn(opts, pos.toLocation(ctx.world()), ctx.targetPlayers(), visibleRange);
    }

    private void buildCache() {
        List<Vec3> cache = getVec3s();
        float inc = mountainHeight / precision;
        var rng = ThreadLocalRandom.current();
        for (int i = 0; i < precision; i++) {
            double r1 = rng.nextDouble() * 2 * Math.PI, r2 = rng.nextDouble() * 2 * Math.PI, r3 = rng.nextDouble() * 2 * Math.PI;
            int finalI = i;
            cache.replaceAll(v -> {
                Vec3 nv = new Vec3(v.x(), v.y() > 0 ? v.y() + inc : v.y() - inc, v.z());
                return finalI < precision - 1 ? nv.rotateX(r1).rotateY(r2).rotateZ(r3) : nv;
            });
        }
        double minSq = Double.MAX_VALUE, maxSq = -Double.MAX_VALUE;
        for (Vec3 v : cache) {
            double ls = v.lengthSquared();
            if (ls < minSq) minSq = ls;
            if (ls > maxSq) maxSq = ls;
        }
        double avg = (minSq + maxSq) / 2;
        cacheGreen = new ArrayList<>();
        cacheBlue = new ArrayList<>();
        for (Vec3 v : cache) {
            if (v.lengthSquared() >= avg) cacheGreen.add(v);
            else cacheBlue.add(v);
        }
    }

    @NotNull
    private List<Vec3> getVec3s() {
        int sq = (int) Math.sqrt(particles);
        List<Vec3> cache = new ArrayList<>(sq * sq);
        float thetaStep = (float) (Math.PI / sq), phiStep = (float) (2 * Math.PI / sq);
        for (int i = 0; i < sq; i++) {
            float theta = (i + 1) * thetaStep;
            for (int j = 0; j < sq; j++) {
                float phi = j * phiStep;
                float x = radius * (float) (Math.sin(theta) * Math.cos(phi));
                float y = radius * (float) (Math.sin(theta) * Math.sin(phi));
                float z = radius * (float) Math.cos(theta);
                cache.add(new Vec3(x, y, z));
            }
        }
        return cache;
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public Particle landParticle = Particle.HAPPY_VILLAGER, oceanParticle = Particle.DRIPPING_WATER;
        public @Nullable Color landColor = null, oceanColor = null;
        public int precision = 100, particles = 500;
        public float radius = 1f, mountainHeight = 0.5f;

        {
            type = EffectType.REPEATING;
            period = 5;
            iterations = 200;
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull EarthEffect build() {
            return new EarthEffect(this);
        }
    }
}