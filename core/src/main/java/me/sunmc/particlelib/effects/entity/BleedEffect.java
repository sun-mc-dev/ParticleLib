package me.sunmc.particlelib.effects.entity;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public final class BleedEffect extends AbstractEffect {

    public final boolean hurt;
    public final double height;
    public final Material material;

    private BleedEffect(Builder b) {
        super(b);
        this.hurt = b.hurt;
        this.height = b.height;
        this.material = b.material;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        // Runs on region thread — safe to access block/world data
        double yAdd = ThreadLocalRandom.current().nextDouble() * height;
        Vec3 base = Vec3.fromLocation(ctx.origin()).withY(ctx.origin().getY() + yAdd);
        var loc = base.toLocation(ctx.world());

        // Spawn block crack particles (requires region thread on Folia)
        ctx.world().spawnParticle(
                Particle.BLOCK, loc, 3,
                0.15, 0.15, 0.15, 0,
                material.createBlockData()
        );
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {

        public boolean hurt = true;
        public double height = 1.75;
        public Material material = Material.REDSTONE_BLOCK;

        {
            type = EffectType.REPEATING;
            period = 4;
            iterations = 25;
            asynchronous = false;
        }

        @Contract(mutates = "this")
        public @NotNull Builder hurt(boolean h) {
            hurt = h;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder height(double h) {
            height = h;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder material(Material m) {
            material = m;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull BleedEffect build() {
            return new BleedEffect(this);
        }
    }
}