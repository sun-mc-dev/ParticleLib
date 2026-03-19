package me.sunmc.particlelib.effects.special;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SoundEffect extends AbstractEffect {

    public final @Nullable Sound sound;
    public final @Nullable String customSound;
    public final float volume;
    public final float pitch;
    public final @NotNull SoundCategory category;

    private SoundEffect(Builder b) {
        super(b);
        this.sound = b.sound;
        this.customSound = b.customSound;
        this.volume = b.volume;
        this.pitch = b.pitch;
        this.category = b.category;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        // Runs on region/main thread because asynchronous=false
        Location loc = ctx.origin();
        var world = ctx.world();

        if (sound != null) {
            if (ctx.targetPlayers() != null) {
                ctx.targetPlayers().forEach(p -> p.playSound(loc, sound, category, volume, pitch));
            } else {
                world.playSound(loc, sound, category, volume, pitch);
            }
        }

        if (customSound != null) {
            if (ctx.targetPlayers() != null) {
                ctx.targetPlayers().forEach(p -> p.playSound(loc, customSound, category, volume, pitch));
            } else {
                world.playSound(loc, customSound, category, volume, pitch);
            }
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {

        public @Nullable Sound sound = null;
        public @Nullable String customSound = null;
        public float volume = 1.0f;
        public float pitch = 1.0f;
        public @NotNull SoundCategory category = SoundCategory.MASTER;

        {
            type = EffectType.INSTANT;
            asynchronous = false;
        }

        @Contract(mutates = "this")
        public @NotNull Builder sound(@NotNull Sound s) {
            sound = s;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder custom(@NotNull String key) {
            customSound = key;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder volume(float v) {
            volume = v;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder pitch(float p) {
            pitch = p;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder category(@NotNull SoundCategory c) {
            category = c;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull SoundEffect build() {
            return new SoundEffect(this);
        }
    }
}
