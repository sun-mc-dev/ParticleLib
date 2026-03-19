package me.sunmc.particlelib.effects.shape;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class AnimatedBallEffect extends AbstractEffect {

    public final int totalParticles;
    public final int particlesPerTick;
    public final float size;
    public final float xFactor, yFactor, zFactor;
    public final float xOffset, yOffset, zOffset;
    public final double xRot, yRot, zRot;

    private int step = 0;

    private AnimatedBallEffect(Builder b) {
        super(b);
        this.totalParticles = b.totalParticles;
        this.particlesPerTick = b.particlesPerTick;
        this.size = b.size;
        this.xFactor = b.xFactor;
        this.yFactor = b.yFactor;
        this.zFactor = b.zFactor;
        this.xOffset = b.xOffset;
        this.yOffset = b.yOffset;
        this.zOffset = b.zOffset;
        this.xRot = b.xRot;
        this.yRot = b.yRot;
        this.zRot = b.zRot;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        if (iteration == 0) step = 0;
        Vec3 base = Vec3.fromLocation(ctx.origin());

        for (int i = 0; i < particlesPerTick; i++) {
            step++;
            float t = ((float) Math.PI / totalParticles) * step;
            float r = (float) (Math.sin(t) * size);
            float s = 2 * (float) Math.PI * t;

            Vec3 v = new Vec3(
                    xFactor * r * Math.cos(s) + xOffset,
                    yFactor * size * Math.cos(t) + yOffset,
                    zFactor * r * Math.sin(s) + zOffset
            ).rotateX(xRot).rotateY(yRot).rotateZ(zRot);

            displayAbsolute(ctx, base.add(v));
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public int totalParticles = 150, particlesPerTick = 10;
        public float size = 1f;
        public float xFactor = 1f, yFactor = 2f, zFactor = 1f;
        public float xOffset = 0f, yOffset = 0.8f, zOffset = 0f;
        public double xRot = 0, yRot = 0, zRot = 0;

        {
            type = EffectType.REPEATING;
            period = 1;
            iterations = 500;
            particle = Particle.WITCH;
        }

        @Contract(mutates = "this")
        public @NotNull Builder size(float s) {
            size = s;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder totalParticles(int n) {
            totalParticles = n;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull AnimatedBallEffect build() {
            return new AnimatedBallEffect(this);
        }
    }
}