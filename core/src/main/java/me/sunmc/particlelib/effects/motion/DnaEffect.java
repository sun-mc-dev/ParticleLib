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

public final class DnaEffect extends AbstractEffect {

    public final Particle helixParticle;
    public final @Nullable Color helixColor;
    public final Particle base1Particle;
    public final @Nullable Color base1Color;
    public final Particle base2Particle;
    public final @Nullable Color base2Color;

    public final double radials;
    public final float radius;
    public final int particlesHelix;
    public final int particlesBase;
    public final float length;
    public final float grow;
    public final int baseInterval;

    private int step = 0;

    private DnaEffect(Builder b) {
        super(b);
        this.helixParticle = b.helixParticle;
        this.helixColor = b.helixColor;
        this.base1Particle = b.base1Particle;
        this.base1Color = b.base1Color;
        this.base2Particle = b.base2Particle;
        this.base2Color = b.base2Color;
        this.radials = b.radials;
        this.radius = b.radius;
        this.particlesHelix = b.particlesHelix;
        this.particlesBase = b.particlesBase;
        this.length = b.length;
        this.grow = b.grow;
        this.baseInterval = b.baseInterval;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        if (iteration == 0) step = 0;
        Location loc = ctx.origin();
        Vec3 base = Vec3.fromLocation(loc);

        for (int j = 0; j < particlesHelix; j++) {
            if (step * grow > length) step = 0;

            // double helix strands
            for (int i = 0; i < 2; i++) {
                double angle = step * radials + Math.PI * i;
                Vec3 v = new Vec3(Math.cos(angle) * radius, step * grow, Math.sin(angle) * radius)
                        .rotateX(Math.toRadians(loc.getPitch() + 90))
                        .rotateY(Math.toRadians(-loc.getYaw()));
                display(ctx, base.add(v).toLocation(ctx.world()), helixColor != null
                        ? baseParticleOptions.withParticle(helixParticle).withColor(helixColor)
                        : baseParticleOptions.withParticle(helixParticle));
            }

            // base pairs
            if (step % baseInterval == 0) {
                for (int i = -particlesBase; i <= particlesBase; i++) {
                    if (i == 0) continue;
                    Particle bp = i > 0 ? base1Particle : base2Particle;
                    Color bColor = i > 0 ? base1Color : base2Color;
                    double angle = step * radials;
                    Vec3 v = new Vec3(Math.cos(angle), 0, Math.sin(angle))
                            .multiply(radius * i / particlesBase)
                            .withY(step * grow)
                            .rotateX(Math.toRadians(loc.getPitch() + 90))
                            .rotateY(Math.toRadians(-loc.getYaw()));
                    ParticleOptions opts = baseParticleOptions.withParticle(bp);
                    if (bColor != null) opts = opts.withColor(bColor);
                    display(ctx, base.add(v).toLocation(ctx.world()), opts);
                }
            }
            step++;
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
        public Particle helixParticle = Particle.FLAME;
        public Particle base1Particle = Particle.FISHING;
        public Particle base2Particle = Particle.DUST;
        public @Nullable Color helixColor = null, base1Color = null, base2Color = null;
        public double radials = Math.PI / 30;
        public float radius = 1.5f, length = 15f, grow = 0.2f;
        public int particlesHelix = 3, particlesBase = 15, baseInterval = 10;

        {
            type = EffectType.REPEATING;
            period = 1;
            iterations = 500;
        }

        @Contract(mutates = "this")
        public @NotNull Builder radius(float r) {
            radius = r;
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
        public @NotNull DnaEffect build() {
            return new DnaEffect(this);
        }
    }
}
