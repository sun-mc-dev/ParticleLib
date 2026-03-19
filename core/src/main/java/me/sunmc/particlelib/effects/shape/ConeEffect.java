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

public final class ConeEffect extends AbstractEffect {

    public final float lengthGrow;
    public final double angularVelocity;
    public final int particles;
    public final float radiusGrow;
    public final int particlesCone;
    public final double rotation;
    public final boolean randomize;
    public final boolean solid;
    public final int strands;

    private int step = 0;
    private double curRotation;

    private ConeEffect(Builder b) {
        super(b);
        this.lengthGrow = b.lengthGrow;
        this.angularVelocity = b.angularVelocity;
        this.particles = b.particles;
        this.radiusGrow = b.radiusGrow;
        this.particlesCone = b.particlesCone;
        this.rotation = b.rotation;
        this.randomize = b.randomize;
        this.solid = b.solid;
        this.strands = b.strands;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        if (iteration == 0) {
            step = 0;
            curRotation = rotation;
        }
        var loc = ctx.origin();
        Vec3 base = Vec3.fromLocation(loc);
        var rng = ThreadLocalRandom.current();

        for (int x = 0; x < particles; x++) {
            if (step > particlesCone) step = 0;
            if (randomize && step == 0) curRotation = rng.nextDouble() * 2 * Math.PI;

            for (int y = 0; y < strands; y++) {
                double angle = step * angularVelocity + curRotation + (2 * Math.PI * y / strands);
                float radius = solid ? step * radiusGrow * rng.nextFloat() : step * radiusGrow;
                float length = step * lengthGrow;

                Vec3 v = new Vec3(Math.cos(angle) * radius, length, Math.sin(angle) * radius)
                        .rotateX(Math.toRadians(loc.getPitch() + 90))
                        .rotateY(Math.toRadians(-loc.getYaw()));
                displayAbsolute(ctx, base.add(v));
            }
            step++;
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public float lengthGrow = 0.05f, radiusGrow = 0.006f;
        public double angularVelocity = Math.PI / 16, rotation = 0;
        public int particles = 10, particlesCone = 180, strands = 1;
        public boolean randomize = false, solid = false;

        {
            type = EffectType.REPEATING;
            period = 1;
            iterations = 200;
            particle = Particle.FLAME;
        }

        public @NotNull Builder strands(int n) {
            strands = n;
            return self();
        }

        public @NotNull Builder solid(boolean b) {
            solid = b;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull ConeEffect build() {
            return new ConeEffect(this);
        }
    }
}