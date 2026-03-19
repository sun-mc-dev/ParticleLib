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

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Renders a string as particles using AWT font rendering.
 */
public final class TextEffect extends AbstractEffect {

    public final String text;
    public final boolean invert;
    public final int stepX, stepY;
    public final float size;
    public final boolean realtime;
    public final Font font;

    private BufferedImage image = null;
    private String lastText = null;
    private Font lastFont = null;

    private TextEffect(Builder b) {
        super(b);
        this.text = b.text;
        this.invert = b.invert;
        this.stepX = b.stepX;
        this.stepY = b.stepY;
        this.size = b.size;
        this.realtime = b.realtime;
        this.font = b.font;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        if (font == null) return;
        if (image == null || (realtime && (!text.equals(lastText) || !font.equals(lastFont)))) {
            lastText = text;
            lastFont = font;
            image = renderText(font, text);
        }
        if (image == null) return;

        Vec3 base = Vec3.fromLocation(ctx.origin());
        var loc = ctx.origin();
        int iw = image.getWidth(), ih = image.getHeight();

        for (int y = 0; y < ih; y += stepY) {
            for (int x = 0; x < iw; x += stepX) {
                int pixel = image.getRGB(x, y);
                if (!invert && Color.black.getRGB() != pixel) continue;
                if (invert && Color.black.getRGB() == pixel) continue;

                Vec3 v = new Vec3((iw / 2.0 - x) * size, (ih / 2.0 - y) * size, 0)
                        .rotateY(Math.toRadians(-loc.getYaw()));
                displayAbsolute(ctx, base.add(v));
            }
        }
    }

    private static @Nullable BufferedImage renderText(Font font, String text) {
        try {
            BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics g = tmp.getGraphics();
            g.setFont(font);
            FontRenderContext frc = g.getFontMetrics().getFontRenderContext();
            Rectangle2D rect = font.getStringBounds(text, frc);
            g.dispose();

            BufferedImage img = new BufferedImage(
                    (int) Math.ceil(rect.getWidth()), (int) Math.ceil(rect.getHeight()),
                    BufferedImage.TYPE_4BYTE_ABGR);
            g = img.getGraphics();
            g.setColor(Color.black);
            g.setFont(font);
            g.drawString(text, 0, g.getFontMetrics().getAscent());
            g.dispose();
            return img;
        } catch (Exception e) {
            return null;
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public String text = "Text";
        public boolean invert = false, realtime = false;
        public int stepX = 1, stepY = 1;
        public float size = 1f / 5f;
        public Font font = new Font("Tahoma", Font.PLAIN, 16);

        {
            type = EffectType.REPEATING;
            period = 40;
            iterations = 20;
            particle = Particle.FIREWORK;
        }

        @Contract(mutates = "this")
        public @NotNull Builder text(String t) {
            text = t;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder font(Font f) {
            font = f;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder invert(boolean b) {
            invert = b;
            return self();
        }

        public @NotNull Builder realtime(boolean b) {
            realtime = b;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull TextEffect build() {
            return new TextEffect(this);
        }
    }
}