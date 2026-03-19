package me.sunmc.particlelib.effects.ambient;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectType;
import me.sunmc.particlelib.api.math.Vec3;
import me.sunmc.particlelib.effects.AbstractEffect;
import org.bukkit.Particle;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;

public final class TraceEffect extends AbstractEffect {
    public final int refresh, maxWayPoints;

    private final Deque<Vec3> wayPoints = new ArrayDeque<>();
    private World cachedWorld;

    private TraceEffect(Builder b) {
        super(b);
        this.refresh = b.refresh;
        this.maxWayPoints = b.maxWayPoints;
    }

    @Override
    public void onTick(@NotNull EffectContext ctx, int iteration) {
        if (cachedWorld == null) {
            cachedWorld = ctx.world();
        } else if (!cachedWorld.equals(ctx.world())) {
            wayPoints.clear();
            cachedWorld = ctx.world();
        }

        if (wayPoints.size() >= maxWayPoints) wayPoints.pollFirst();
        wayPoints.addLast(Vec3.fromLocation(ctx.origin()));

        if (iteration % refresh != 0) return;
        for (Vec3 p : wayPoints) displayAbsolute(ctx, p);
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder extends EffectBuilder<Builder> {
        public int refresh = 5, maxWayPoints = 30;

        {
            type = EffectType.REPEATING;
            period = 1;
            iterations = 600;
            particle = Particle.FLAME;
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull TraceEffect build() {
            return new TraceEffect(this);
        }
    }
}