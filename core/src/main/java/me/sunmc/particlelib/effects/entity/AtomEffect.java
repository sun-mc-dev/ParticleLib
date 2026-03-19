package me.sunmc.particlelib.effects.entity;

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

public final class AtomEffect extends AbstractEffect {

    public final Particle nucleusParticle;
    public final @Nullable Color nucleusColor;
    public final Particle orbitalParticle;
    public final @Nullable Color orbitalColor;
    public final double radius, radiusNucleus, angularVelocity, rotation;
    public final int particlesNucleus, particlesOrbital, orbitals;
    public final boolean orient;

    private AtomEffect(Builder b) {
        super(b);
        this.nucleusParticle = b.nucleusParticle;
        this.nucleusColor = b.nucleusColor;
        this.orbitalParticle = b.orbitalParticle;
        this.orbitalColor = b.orbitalColor;
        this.radius = b.radius;
        this.radiusNucleus = b.radiusNucleus;
        this.angularVelocity = b.angularVelocity;
        this.rotation = b.rotation;
        this.particlesNucleus = b.particlesNucleus;
        this.particlesOrbital = b.particlesOrbital;
        this.orbitals = b.orbitals;
        this.orient = b.orient;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        Vec3 base = Vec3.fromLocation(ctx.origin());
        var rng = ThreadLocalRandom.current();

        // nucleus
        ParticleOptions no = baseParticleOptions.withParticle(nucleusParticle);
        if (nucleusColor != null) no = no.withColor(nucleusColor);
        for (int i = 0; i < particlesNucleus; i++) {
            Vec3 v = randomUnit(rng).multiply(radius * radiusNucleus);
            if (orient) v = v.rotateByLocation(ctx.origin());
            spawn(ctx, base.add(v), no);
        }

        // orbitals
        ParticleOptions oo = baseParticleOptions.withParticle(orbitalParticle);
        if (orbitalColor != null) oo = oo.withColor(orbitalColor);
        double angle = iteration * angularVelocity;
        for (int i = 0; i < particlesOrbital; i++) {
            for (int j = 0; j < orbitals; j++) {
                double xRot = (Math.PI / orbitals) * j;
                Vec3 v = new Vec3(Math.cos(angle), Math.sin(angle), 0).multiply(radius)
                        .rotateX(xRot).rotateY(rotation);
                if (orient) v = v.rotateByLocation(ctx.origin());
                spawn(ctx, base.add(v), oo);
            }
        }
    }

    private void spawn(@NotNull EffectContext ctx, @NotNull Vec3 pos, ParticleOptions opts) {
        PaperParticleSpawner.getInstance()
                .spawn(opts, pos.toLocation(ctx.world()), ctx.targetPlayers(), visibleRange);
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
        public Particle nucleusParticle = Particle.DRIPPING_WATER;
        public @Nullable Color nucleusColor = null;
        public Particle orbitalParticle = Particle.DRIPPING_LAVA;
        public @Nullable Color orbitalColor = null;
        public double radius = 3, radiusNucleus = 0.2, angularVelocity = Math.PI / 80, rotation = 0;
        public int particlesNucleus = 10, particlesOrbital = 10, orbitals = 3;
        public boolean orient = false;

        {
            type = EffectType.REPEATING;
            period = 2;
            iterations = 200;
        }

        @Contract(mutates = "this")
        public @NotNull Builder radius(double r) {
            radius = r;
            return self();
        }

        public @NotNull Builder orbitals(int n) {
            orbitals = n;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder orient(boolean b) {
            orient = b;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull AtomEffect build() {
            return new AtomEffect(this);
        }
    }
}
