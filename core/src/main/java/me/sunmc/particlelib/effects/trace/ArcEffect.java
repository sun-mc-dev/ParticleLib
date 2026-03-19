package me.sunmc.particlelib.effects.trace;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ArcEffect extends AbstractEffect {

    public final float height;
    public final int particles;

    private ArcEffect(Builder b) {
        super(b);
        this.height = b.height;
        this.particles = b.particles;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        Location from = ctx.origin();
        Location to = ctx.target();
        if (to == null) return;

        Vec3 vFrom = Vec3.fromLocation(from);
        Vec3 link = Vec3.fromLocation(to).subtract(vFrom);
        double len = link.length();
        float pitch = (float) (4 * height / Math.pow(len, 2));

        for (int i = 0; i < particles; i++) {
            Vec3 v = link.normalize().multiply(len * i / particles);
            float xi = ((float) i / particles) * (float) len - (float) len / 2;
            double yi = -pitch * Math.pow(xi, 2) + height;
            displayAbsolute(ctx, vFrom.add(v).withY(vFrom.y() + v.y() + yi));
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public float height = 2f;
        public int particles = 100;

        {
            type = EffectType.REPEATING;
            period = 1;
            iterations = 200;
            particle = Particle.FLAME;
        }

        @Contract(mutates = "this")
        public @NotNull Builder height(float h) {
            height = h;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder particles(int n) {
            particles = n;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull ArcEffect build() {
            return new ArcEffect(this);
        }
    }
}