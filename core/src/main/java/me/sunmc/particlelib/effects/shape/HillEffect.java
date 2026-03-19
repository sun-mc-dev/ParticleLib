package me.sunmc.particlelib.effects.shape;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class HillEffect extends AbstractEffect {

    public final float height;
    public final int particles;
    public final float edgeLength;
    public final double yRotation;

    private HillEffect(Builder b) {
        super(b);
        this.height = b.height;
        this.particles = b.particles;
        this.edgeLength = b.edgeLength;
        this.yRotation = b.yRotation;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        Vec3 base = Vec3.fromLocation(ctx.origin());
        for (int x = 0; x <= particles; x++) {
            double y1 = Math.sin(Math.PI * x / particles);
            for (int z = 0; z <= particles; z++) {
                double y2 = Math.sin(Math.PI * z / particles);
                Vec3 v = new Vec3(edgeLength * x / particles, height * y1 * y2,
                        edgeLength * z / particles).rotateY(yRotation);
                displayAbsolute(ctx, base.add(v));
            }
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public float height = 2.5f, edgeLength = 6.5f;
        public int particles = 30;
        public double yRotation = Math.PI / 7;

        {
            type = EffectType.REPEATING;
            period = 10;
            iterations = 20;
            particle = Particle.FLAME;
        }

        @Contract(mutates = "this")
        public @NotNull Builder height(float h) {
            height = h;
            return self();
        }

        public @NotNull Builder edgeLength(float e) {
            edgeLength = e;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull HillEffect build() {
            return new HillEffect(this);
        }
    }
}