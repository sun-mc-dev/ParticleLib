package me.sunmc.particlelib.api;

import me.sunmc.particlelib.api.effect.Effect;
import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectHandle;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Primary public API entry point for ParticleLib.
 *
 * <p>This interface is implemented by the core module and exposed via the
 * Bukkit {@link org.bukkit.plugin.ServicesManager}, so dependent plugins can
 * obtain an instance without a hard compile-time dependency on the
 * implementation classes:</p>
 *
 * <pre>{@code
 * // Retrieve the service
 * RegisteredServiceProvider<ParticleLibAPI> prov =
 *     getServer().getServicesManager().getRegistration(ParticleLibAPI.class);
 * if (prov != null) {
 *     ParticleLibAPI api = prov.getProvider();
 *     api.play("sphere", location);
 * }
 * }</pre>
 *
 * <p>Alternatively, when ParticleLib is <em>shaded</em> into your plugin,
 * call {@link me.sunmc.particlelib.ParticleLib#initialize(Plugin)} in
 * {@code onEnable} and use the static helpers directly on
 * {@link me.sunmc.particlelib.ParticleLib}.</p>
 *
 * <h3>Quick-start examples</h3>
 * <pre>{@code
 * // 1. Play a built-in effect by name
 * api.play("sphere", player.getLocation());
 *
 * // 2. Play with a config section (YAML-driven)
 * api.play(configSection, player.getLocation());
 *
 * // 3. Fluent builder (requires core compile dep)
 * SphereEffect.builder().radius(3).color(Color.AQUA).playAt(location);
 *
 * // 4. Cancel mid-flight
 * EffectHandle h = api.play("tornado", location);
 * Bukkit.getScheduler().runTaskLater(plugin, h::cancel, 100L);
 * }</pre>
 */
public interface ParticleLibAPI {


    /**
     * Plays a registered effect by its string ID at {@code location}.
     *
     * @param effectId registered effect ID (e.g. {@code "sphere"}, {@code "vortex"})
     * @param location where to play the effect
     * @return active handle, or {@link EffectHandle#terminated()} if the ID is unknown
     */
    @NotNull EffectHandle play(@NotNull String effectId, @NotNull Location location);

    /**
     * Plays an effect that is only visible to a specific player.
     */
    @NotNull EffectHandle play(@NotNull String effectId,
                               @NotNull Location location,
                               @NotNull Player targetPlayer);

    /**
     * Plays an effect from origin to a target location (for directional effects
     * like {@code LineEffect}).
     */
    @NotNull EffectHandle play(@NotNull String effectId,
                               @NotNull Location origin,
                               @NotNull Location target);


    /**
     * Loads and plays an effect from a YAML {@link ConfigurationSection}.
     *
     * <p>The section must contain a {@code class} key matching a registered
     * effect ID. All other keys are applied as effect parameters.</p>
     *
     * <pre>{@code
     * # config.yml
     * death_effect:
     *   class: sphere
     *   particle: DUST
     *   color: FF0000
     *   radius: 2.0
     *   iterations: 60
     * }</pre>
     *
     * @param section  config section describing the effect
     * @param location where to play
     * @return active handle, or {@link EffectHandle#terminated()} if config is invalid
     */
    @NotNull EffectHandle play(@NotNull ConfigurationSection section,
                               @NotNull Location location);

    @NotNull EffectHandle play(@NotNull ConfigurationSection section,
                               @NotNull Location location,
                               @NotNull Player targetPlayer);


    /**
     * Plays a pre-built {@link Effect} (obtained via a builder) in the given
     * context.
     */
    @NotNull EffectHandle play(@NotNull Effect effect, @NotNull EffectContext context);

    /**
     * Returns a fresh {@link EffectBuilder} for the registered effect ID,
     * or {@link Optional#empty()} if the ID is unknown.
     *
     * <p>Use this when you want builder-level control without a compile-time
     * dependency on the concrete effect class:</p>
     * <pre>{@code
     * api.builder("sphere")
     *    .ifPresent(b -> b.particleCount(5).playAt(loc));
     * }</pre>
     */
    @NotNull Optional<EffectBuilder<?>> builder(@NotNull String effectId);

    /**
     * Registers a custom effect factory under {@code id}.
     *
     * @param id      unique identifier (use a namespaced format, e.g. {@code "myplugin:custom"})
     * @param factory supplier of fresh builders for this effect
     */
    void register(@NotNull String id,
                  @NotNull Supplier<EffectBuilder<?>> factory);

    /**
     * Removes a previously registered effect. Built-in effects cannot be
     * removed (they are silently ignored).
     */
    void unregister(@NotNull String id);

    /**
     * Suppresses all particle packets for {@code player}.
     * Useful for accessibility settings or low-end client modes.
     */
    void ignorePlayer(@NotNull Player player);

    /**
     * Re-enables particles for {@code player}.
     */
    void unignorePlayer(@NotNull Player player);

    /**
     * Returns {@code true} if particles are suppressed for {@code player}.
     */
    boolean isIgnored(@NotNull Player player);

    /**
     * Cancels all effects currently tracked by this API instance.
     * Called automatically on plugin disable when using the standalone deployment.
     */
    void cancelAll();
}