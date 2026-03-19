package me.sunmc.particlelib.effects.special;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.api.particle.ParticleOptions;
import me.sunmc.particlelib.effects.AbstractEffect;
import me.sunmc.particlelib.internal.particle.PaperParticleSpawner;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * Renders an image as colored dust particles, one color per pixel.
 */
public final class ColoredImageEffect extends AbstractEffect {

    public final String fileName;
    public final int stepX, stepY;
    public final float size;
    public final boolean orient, orientPitch;

    private volatile BufferedImage image = null;

    private ColoredImageEffect(Builder b) {
        super(b);
        this.fileName = b.fileName;
        this.stepX = b.stepX;
        this.stepY = b.stepY;
        this.size = b.size;
        this.orient = b.orient;
        this.orientPitch = b.orientPitch;
        loadAsync();
    }

    private void loadAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                File f = new File(fileName);
                image = f.exists() ? ImageIO.read(f) : ImageIO.read(new URL(fileName));
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        if (image == null) return;
        Vec3 base = Vec3.fromLocation(ctx.origin());
        var loc = ctx.origin();
        int iw = image.getWidth(), ih = image.getHeight();

        for (int y = 0; y < ih; y += stepY) {
            for (int x = 0; x < iw; x += stepX) {
                int rgb = image.getRGB(x, y);
                if (((rgb >> 24) & 0xFF) == 0) continue; // transparent

                java.awt.Color ac = new java.awt.Color(rgb, true);
                Color bc = Color.fromRGB(ac.getRed(), ac.getGreen(), ac.getBlue());

                Vec3 v = new Vec3((iw / 2.0 - x) * size, (ih / 2.0 - y) * size, 0);
                if (orientPitch) v = v.rotateX(Math.toRadians(loc.getPitch()));
                if (orient) v = v.rotateY(Math.toRadians(-loc.getYaw()));

                ParticleOptions opts = baseParticleOptions.withColor(bc);
                PaperParticleSpawner.getInstance().spawn(opts,
                        base.add(v).toLocation(ctx.world()), ctx.targetPlayers(), visibleRange);
            }
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public String fileName = "";
        public int stepX = 10, stepY = 10;
        public float size = 1f / 40f;
        public boolean orient = true, orientPitch = false;

        {
            particle = Particle.DUST;
        }

        @Contract(mutates = "this")
        public @NotNull Builder file(String f) {
            fileName = f;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull ColoredImageEffect build() {
            return new ColoredImageEffect(this);
        }
    }
}
