package me.sunmc.particlelib.effects.special;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * Renders a monochrome image (or GIF frame) as particles.
 * Loads images asynchronously; skips ticks until the image is ready.
 */
public final class ImageEffect extends AbstractEffect {

    public final @Nullable String fileName;
    public final boolean invert;
    public final int stepX, stepY;
    public final float size;
    public final boolean orient, orientPitch, enableRotation;
    public final double angVelX, angVelY, angVelZ;

    private volatile BufferedImage[] frames = null;
    private int rotationStep = 0;

    private ImageEffect(Builder b) {
        super(b);
        this.fileName = b.fileName;
        this.invert = b.invert;
        this.stepX = b.stepX;
        this.stepY = b.stepY;
        this.size = b.size;
        this.orient = b.orient;
        this.orientPitch = b.orientPitch;
        this.enableRotation = b.enableRotation;
        this.angVelX = b.angVelX;
        this.angVelY = b.angVelY;
        this.angVelZ = b.angVelZ;
        if (fileName != null) loadAsync();
    }

    private void loadAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                File f = new File(fileName);
                if (!f.exists() && fileName.startsWith("http")) {
                    BufferedImage img = ImageIO.read(new URL(fileName));
                    frames = new BufferedImage[]{img};
                } else {
                    frames = new BufferedImage[]{ImageIO.read(f)};
                }
            } catch (Exception ignored) {
                frames = new BufferedImage[0];
            }
        });
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        if (frames == null || frames.length == 0) return;
        int fi = iteration % frames.length;
        BufferedImage img = frames[fi];
        if (img == null) return;

        Vec3 base = Vec3.fromLocation(ctx.origin());
        var loc = ctx.origin();
        int iw = img.getWidth(), ih = img.getHeight();

        for (int y = 0; y < ih; y += stepY) {
            for (int x = 0; x < iw; x += stepX) {
                int pixel = img.getRGB(x, y);
                if (!invert && Color.black.getRGB() != pixel) continue;
                if (invert && Color.black.getRGB() == pixel) continue;

                Vec3 v = new Vec3((iw / 2.0 - x) * size, (ih / 2.0 - y) * size, 0);
                if (orientPitch) v = v.rotateX(Math.toRadians(loc.getPitch()));
                if (orient) v = v.rotateY(Math.toRadians(-loc.getYaw()));
                if (enableRotation)
                    v = v.rotateX(angVelX * rotationStep)
                            .rotateY(angVelY * rotationStep)
                            .rotateZ(angVelZ * rotationStep);
                displayAbsolute(ctx, base.add(v));
            }
        }
        rotationStep++;
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public @Nullable String fileName = null;
        public boolean invert = false;
        public int stepX = 10, stepY = 10;
        public float size = 1f / 40f;
        public boolean orient = true, orientPitch = false, enableRotation = true;
        public double angVelX = Math.PI / 200, angVelY = Math.PI / 170, angVelZ = Math.PI / 155;

        {
            type = EffectType.REPEATING;
            period = 2;
            iterations = 200;
            particle = Particle.DUST;
        }

        @Contract(mutates = "this")
        public @NotNull Builder file(String f) {
            fileName = f;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder invert(boolean b) {
            invert = b;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder stepX(int n) {
            stepX = n;
            return self();
        }

        public @NotNull Builder stepY(int n) {
            stepY = n;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder size(float s) {
            size = s;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull ImageEffect build() {
            return new ImageEffect(this);
        }
    }
}