package me.sunmc.particlelib.effects.ambient;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.effects.AbstractEffect;
import me.sunmc.particlelib.internal.particle.PaperParticleSpawner;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public final class ExplodeEffect extends AbstractEffect {

    public final @Nullable Sound sound;
    public final float soundVolume;
    public final float soundPitch;

    private ExplodeEffect(Builder b) {
        super(b);
        this.sound = b.sound;
        this.soundVolume = b.soundVolume;
        this.soundPitch = b.soundPitch;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        Location loc = ctx.origin();

        if (sound != null) {
            float pitch = soundPitch * (1f
                    + (ThreadLocalRandom.current().nextFloat()
                    - ThreadLocalRandom.current().nextFloat()) * 0.2f) * 0.7f;
            ctx.world().playSound(loc, sound, SoundCategory.MASTER, soundVolume, pitch);
        }

        var recipients = PaperParticleSpawner.getInstance()
                .resolveRecipients(loc, visibleRange, ctx.targetPlayers());

        PaperParticleSpawner.getInstance().spawn(
                baseParticleOptions.withParticle(Particle.EXPLOSION), loc, recipients);
        PaperParticleSpawner.getInstance().spawn(
                baseParticleOptions.withParticle(Particle.EXPLOSION_EMITTER), loc, recipients);
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {

        public @Nullable Sound sound = Sound.ENTITY_GENERIC_EXPLODE;
        public float soundVolume = 4f;
        public float soundPitch = 1f;

        {
            type = EffectType.INSTANT;
            asynchronous = false;
            speed = 0.5f;
        }

        @Contract(mutates = "this")
        public @NotNull Builder sound(Sound s) {
            sound = s;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder soundVolume(float v) {
            soundVolume = v;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull ExplodeEffect build() {
            return new ExplodeEffect(this);
        }
    }
}