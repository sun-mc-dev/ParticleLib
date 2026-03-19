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

import java.util.ArrayList;
import java.util.List;

/**
 * A moving ocean-style wave effect.
 *
 * <h3>Bug fix</h3>
 * <p>Previous version multiplied velocity by the iteration counter each tick
 * ({@code velocity.multiply(iteration)}), which caused the wave to teleport
 * rather than move smoothly. The fix stores a mutable {@code currentOffset}
 * that is incremented by {@code velocity} each tick.</p>
 */
public final class WaveEffect extends AbstractEffect {

    public final Particle cloudParticle;
    public final @Nullable Color cloudColor;
    public final int particlesFront, particlesBack, rows;
    public final float lengthFront, lengthBack, depthFront, heightBack, height, width;

    // Per-play mutable state — only accessed from the tick thread
    private List<Vec3> waterCache = null;
    private List<Vec3> cloudCache = null;
    private Vec3 velocity = null;   // constant direction × speed
    private Vec3 currentOffset = Vec3.ZERO;

    private WaveEffect(Builder b) {
        super(b);
        this.cloudParticle = b.cloudParticle;
        this.cloudColor = b.cloudColor;
        this.particlesFront = b.particlesFront;
        this.particlesBack = b.particlesBack;
        this.rows = b.rows;
        this.lengthFront = b.lengthFront;
        this.lengthBack = b.lengthBack;
        this.depthFront = b.depthFront;
        this.heightBack = b.heightBack;
        this.height = b.height;
        this.width = b.width;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        Location origin = ctx.origin();

        // First tick: build geometry cache relative to origin direction
        if (waterCache == null) {
            velocity = Vec3.fromVector(origin.getDirection().setY(0).normalize()).multiply(0.2);
            buildCache(origin);
            currentOffset = Vec3.ZERO;
        }

        currentOffset = currentOffset.add(velocity);

        Vec3 base = Vec3.fromLocation(origin).add(currentOffset);

        ParticleOptions wo = baseParticleOptions;
        ParticleOptions co = baseParticleOptions.withParticle(cloudParticle).withCount(1);
        if (cloudColor != null) co = co.withColor(cloudColor);

        PaperParticleSpawner spawner = PaperParticleSpawner.getInstance();

        // Use tick-cached recipients already set by EffectRunner
        List<org.bukkit.entity.Player> recipients =
                spawner.resolveRecipients(origin, visibleRange, ctx.targetPlayers());

        for (Vec3 v : cloudCache) {
            spawner.spawn(co, base.add(v).toLocation(ctx.world()), recipients);
        }
        for (Vec3 v : waterCache) {
            spawner.spawn(wo, base.add(v).toLocation(ctx.world()), recipients);
        }
    }

    @Override
    public void onDone(@NotNull EffectContext ctx) {
        // Reset per-play state so the same instance can be reused
        waterCache = null;
        cloudCache = null;
        velocity = null;
        currentOffset = Vec3.ZERO;
    }

    private void buildCache(@NotNull Location loc) {
        waterCache = new ArrayList<>();
        cloudCache = new ArrayList<>();

        double yaw = Math.toRadians(-loc.getYaw() + 90);

        // Front arc (tube shape)
        Vec3 s1 = new Vec3(-lengthFront, 0, 0);
        Vec3 h = new Vec3(-0.5 * lengthFront, height, 0);
        addArcPoints(s1, h, particlesFront, depthFront, yaw, true);

        // Back arc (rolling back of wave)
        Vec3 s2 = new Vec3(lengthBack, 0, 0);
        addArcPoints(s2, h, particlesBack, heightBack, yaw, false);
    }

    private void addArcPoints(Vec3 s, @NotNull Vec3 h, int count, float depth,
                              double yaw, boolean front) {
        Vec3 sToH = h.subtract(s);
        float len = (float) sToH.length();
        Vec3 mid = s.add(sToH.multiply(0.5));
        Vec3 nDir = sToH.multiply(1.0 / len);

        // Normal perpendicular to arc, pointing "outward"
        Vec3 nPerp = new Vec3(sToH.y(), -sToH.x(), 0).normalize();
        if (nPerp.x() < 0) nPerp = nPerp.negate();

        for (int i = 0; i < count; i++) {
            float ratio = (float) i / count;
            float xi = (ratio - 0.5f) * len;
            float yi = (float) (-depth / Math.pow(len / 2.0, 2) * xi * xi + depth);

            Vec3 pt = mid.add(nDir.multiply(xi)).add(nPerp.multiply(yi));

            for (int j = 0; j < rows; j++) {
                float z = ((float) j / rows - 0.5f) * width;
                Vec3 world = new Vec3(pt.x(), pt.y(), pt.z() + z).rotateY(yaw);

                boolean isEdge = (i == 0 || i == count - 1);
                if (isEdge) cloudCache.add(world);
                else waterCache.add(world);
            }
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {

        public Particle cloudParticle = Particle.CLOUD;
        public @Nullable Color cloudColor = null;
        public int particlesFront = 10, particlesBack = 10, rows = 20;
        public float lengthFront = 1.5f, lengthBack = 3f;
        public float depthFront = 1f, heightBack = 0.5f;
        public float height = 2f, width = 5f;

        {
            type = EffectType.REPEATING;
            period = 5;
            iterations = 50;
            particle = Particle.DRIPPING_WATER;
        }

        @Contract(mutates = "this")
        public @NotNull Builder height(float h) {
            height = h;
            return self();
        }

        public @NotNull Builder width(float w) {
            width = w;
            return self();
        }

        public @NotNull Builder rows(int n) {
            rows = n;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder lengthFront(float l) {
            lengthFront = l;
            return self();
        }

        public @NotNull Builder lengthBack(float l) {
            lengthBack = l;
            return self();
        }

        @Contract(mutates = "this")
        public @NotNull Builder cloudParticle(Particle p) {
            cloudParticle = p;
            return self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull WaveEffect build() {
            return new WaveEffect(this);
        }
    }
}