package me.sunmc.particlelib.registry;

import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.effects.ambient.*;
import me.sunmc.particlelib.effects.entity.AtomEffect;
import me.sunmc.particlelib.effects.entity.BleedEffect;
import me.sunmc.particlelib.effects.motion.*;
import me.sunmc.particlelib.effects.shape.*;
import me.sunmc.particlelib.effects.special.*;
import me.sunmc.particlelib.effects.trace.ArcEffect;
import me.sunmc.particlelib.effects.trace.HelixEffect;
import me.sunmc.particlelib.effects.trace.LineEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Central registry mapping string IDs → effect builder factories.
 *
 * <p>All built-in effects are registered by {@link #registerBuiltins()},
 * which is called once by {@link me.sunmc.particlelib.ParticleLib} on enable.
 * Third-party plugins register custom effects via {@link #register}.</p>
 */
public final class EffectRegistry {

    private static final EffectRegistry INSTANCE = new EffectRegistry();

    public static EffectRegistry get() {
        return INSTANCE;
    }

    private final Map<String, Supplier<EffectBuilder<?>>> factories =
            new ConcurrentHashMap<>();

    private EffectRegistry() {
    }

    public void register(@NotNull String id,
                         @NotNull Supplier<EffectBuilder<?>> factory) {
        factories.put(id.toLowerCase(), factory);
    }

    public void unregister(@NotNull String id) {
        factories.remove(id.toLowerCase());
    }

    public @NotNull Optional<EffectBuilder<?>> lookup(@NotNull String id) {
        Supplier<EffectBuilder<?>> f = factories.get(id.toLowerCase());
        return f == null ? Optional.empty() : Optional.of(f.get());
    }

    public boolean contains(@NotNull String id) {
        return factories.containsKey(id.toLowerCase());
    }

    public @NotNull @UnmodifiableView Set<String> registeredIds() {
        return Collections.unmodifiableSet(factories.keySet());
    }

    /**
     * Called once by ParticleLib on boot.
     */
    public void registerBuiltins() {

        register("sphere", SphereEffect::builder);
        register("circle", CircleEffect::builder);
        register("cube", CubeEffect::builder);
        register("donut", DonutEffect::builder);
        register("star", StarEffect::builder);
        register("heart", HeartEffect::builder);
        register("square", SquareEffect::builder);
        register("pyramid", PyramidEffect::builder);
        register("cone", ConeEffect::builder);
        register("cuboid", CuboidEffect::builder);
        register("grid", GridEffect::builder);
        register("hill", HillEffect::builder);
        register("animated_ball", AnimatedBallEffect::builder);

        register("line", LineEffect::builder);
        register("arc", ArcEffect::builder);
        register("helix", HelixEffect::builder);

        register("vortex", VortexEffect::builder);
        register("tornado", TornadoEffect::builder);
        register("dna", DnaEffect::builder);
        register("wave", WaveEffect::builder);
        register("fountain", FountainEffect::builder);
        register("dragon", DragonEffect::builder);

        register("flame", FlameEffect::builder);
        register("smoke", SmokeEffect::builder);
        register("cloud", CloudEffect::builder);
        register("love", LoveEffect::builder);
        register("music", MusicEffect::builder);
        register("warp", WarpEffect::builder);
        register("shield", ShieldEffect::builder);
        register("earth", EarthEffect::builder);
        register("explode", ExplodeEffect::builder);
        register("big_bang", BigBangEffect::builder);
        register("disco_ball", DiscoBallEffect::builder);
        register("icon", IconEffect::builder);
        register("trace", TraceEffect::builder);
        register("particle", ParticleEffect::builder);

        register("bleed", BleedEffect::builder);
        register("atom", AtomEffect::builder);

        register("equation", EquationEffect::builder);
        register("sound", SoundEffect::builder);
        register("image", ImageEffect::builder);
        register("colored_image", ColoredImageEffect::builder);
        register("text", TextEffect::builder);
        register("modified", ModifiedEffect::builder);
        register("plot", PlotEffect::builder);
    }
}