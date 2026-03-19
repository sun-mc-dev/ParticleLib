package me.sunmc.particlelib.effects.shape;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class GridEffect extends AbstractEffect {

    public final int rows, columns;
    public final float widthCell, heightCell;
    public final int particlesWidth, particlesHeight;
    public final double rotation, rotationX, rotationZ;
    public final boolean center;

    private GridEffect(Builder b) {
        super(b);
        this.rows = b.rows;
        this.columns = b.columns;
        this.widthCell = b.widthCell;
        this.heightCell = b.heightCell;
        this.particlesWidth = b.particlesWidth;
        this.particlesHeight = b.particlesHeight;
        this.rotation = b.rotation;
        this.rotationX = b.rotationX;
        this.rotationZ = b.rotationZ;
        this.center = b.center;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        Vec3 base = Vec3.fromLocation(ctx.origin());
        // rows
        for (int i = 0; i <= rows + 1; i++) {
            for (int j = 0; j < particlesWidth * (columns + 1); j++) {
                addPoint(ctx, base, j * widthCell / particlesWidth, i * heightCell, 0);
            }
        }
        // columns
        for (int i = 0; i <= columns + 1; i++) {
            for (int j = 0; j < particlesHeight * (rows + 1); j++) {
                addPoint(ctx, base, i * widthCell, j * heightCell / particlesHeight, 0);
            }
        }
    }

    private void addPoint(EffectContext ctx, Vec3 base, double x, double y, double z) {
        Vec3 v = new Vec3(x, y, z);
        if (center) v = v.add(-widthCell * (columns + 1) / 2.0, -heightCell * (rows + 1) / 2.0, 0);
        v = v.rotateY(rotation);
        if (rotationX != 0) v = v.rotateX(rotationX);
        if (rotationZ != 0) v = v.rotateZ(rotationZ);
        displayAbsolute(ctx, base.add(v));
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public int rows = 5, columns = 10, particlesWidth = 4, particlesHeight = 3;
        public float widthCell = 1f, heightCell = 1f;
        public double rotation = 0, rotationX = 0, rotationZ = 0;
        public boolean center = false;

        {
            type = EffectType.INSTANT;
            particle = Particle.FLAME;
        }

        @Contract(mutates = "this")
        public @NotNull Builder rows(int r) {
            rows = r;
            return self();
        }

        public @NotNull Builder columns(int c) {
            columns = c;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder center(boolean b) {
            center = b;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull GridEffect build() {
            return new GridEffect(this);
        }
    }
}