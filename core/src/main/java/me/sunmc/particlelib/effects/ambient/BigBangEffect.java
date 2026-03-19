package me.sunmc.particlelib.effects.ambient;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public final class BigBangEffect extends AbstractEffect {
    public final FireworkEffect.Type fireworkType;
    public final Color color2, color3, fadeColor;
    public final int intensity, explosions, soundInterval;
    public final Sound sound;
    public final float soundVolume, soundPitch;

    private BigBangEffect(Builder b) {
        super(b);
        this.fireworkType = b.fireworkType;
        this.color2 = b.color2;
        this.color3 = b.color3;
        this.fadeColor = b.fadeColor;
        this.intensity = b.intensity;
        this.explosions = b.explosions;
        this.soundInterval = b.soundInterval;
        this.sound = b.sound;
        this.soundVolume = b.soundVolume;
        this.soundPitch = b.soundPitch;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        var fe = FireworkEffect.builder().with(fireworkType)
                .withColor(baseParticleOptions.color() != null ? baseParticleOptions.color() : Color.RED)
                .withColor(color2).withColor(color3).withFade(fadeColor).trail(true).build();

        var rng = ThreadLocalRandom.current();
        Vec3 base = Vec3.fromLocation(ctx.origin());
        World world = ctx.world();

        for (int i = 0; i < explosions; i++) {
            Vec3 v = randomUnit(rng).multiply(2.0);
            Location spawnLoc = base.add(v).toLocation(world);
            Firework fw = (Firework) world.spawnEntity(spawnLoc, EntityType.FIREWORK_ROCKET);
            FireworkMeta meta = fw.getFireworkMeta();
            meta.setPower(0);
            for (int j = 0; j < intensity; j++) meta.addEffect(fe);
            fw.setFireworkMeta(meta);
            fw.detonate();
        }

        if (soundInterval > 0 && iteration % soundInterval == 0)
            world.playSound(ctx.origin(), sound, soundVolume, soundPitch);
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
        public FireworkEffect.Type fireworkType = FireworkEffect.Type.BURST;
        public Color color2 = Color.ORANGE, color3 = Color.BLACK, fadeColor = Color.BLACK;
        public int intensity = 2, explosions = 10, soundInterval = 5;
        public Sound sound = Sound.ENTITY_GENERIC_EXPLODE;
        public float soundVolume = 100f, soundPitch = 1f;

        {
            type = EffectType.REPEATING;
            period = 2;
            iterations = 400;
            asynchronous = false;
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull BigBangEffect build() {
            return new BigBangEffect(this);
        }
    }
}