package me.sunmc.particlelib.effects.motion;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class DragonEffect extends AbstractEffect {

    public final float pitch;
    public final int arcs, particles, stepsPerIteration;
    public final float length;

    private final List<Float> rndF = new ArrayList<>();
    private final List<Double> rndAngle = new ArrayList<>();
    private int step = 0;

    private DragonEffect(Builder b) {
        super(b);
        this.pitch = b.pitch;
        this.arcs = b.arcs;
        this.particles = b.particles;
        this.stepsPerIteration = b.stepsPerIteration;
        this.length = b.length;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        if (iteration == 0) step = 0;
        Location loc = ctx.origin();
        Vec3 base = Vec3.fromLocation(loc);
        var rng = ThreadLocalRandom.current();

        for (int j = 0; j < stepsPerIteration; j++) {
            if (step % particles == 0) {
                rndF.clear();
                rndAngle.clear();
            }
            while (rndF.size() < arcs) rndF.add(rng.nextFloat());
            while (rndAngle.size() < arcs) rndAngle.add(rng.nextDouble() * 2 * Math.PI);

            for (int i = 0; i < arcs; i++) {
                float p = rndF.get(i) * 2 * pitch - pitch;
                float x = (step % particles) * length / particles;
                float y = (float) (p * Math.pow(x, 2));

                Vec3 v = new Vec3(x, y, 0)
                        .rotateX(rndAngle.get(i))
                        .rotateZ(Math.toRadians(-loc.getPitch()))
                        .rotateY(Math.toRadians(-(loc.getYaw() + 90)));

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
        public float pitch = 0.1f, length = 4f;
        public int arcs = 20, particles = 30, stepsPerIteration = 2;

        {
            type = EffectType.REPEATING;
            period = 2;
            iterations = 200;
            particle = Particle.FLAME;
        }

        @Contract(mutates = "this")
        public @NotNull Builder pitch(float p) {
            pitch = p;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder arcs(int n) {
            arcs = n;
            return self();
        }

        public @NotNull Builder length(float l) {
            length = l;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull DragonEffect build() {
            return new DragonEffect(this);
        }
    }
}