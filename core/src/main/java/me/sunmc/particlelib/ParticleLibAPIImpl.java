package me.sunmc.particlelib;

import me.sunmc.particlelib.api.ParticleLibAPI;
import me.sunmc.particlelib.api.effect.Effect;
import me.sunmc.particlelib.api.effect.EffectBuilder;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectHandle;
import me.sunmc.particlelib.internal.lifecycle.EffectManager;
import me.sunmc.particlelib.registry.ConfigEffectLoader;
import me.sunmc.particlelib.registry.EffectRegistry;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Concrete implementation of {@link ParticleLibAPI}.
 *
 * <p>One instance is created per owning plugin. In standalone mode the
 * owning plugin is {@link ParticleLib} itself; in shaded mode it is whatever
 * plugin called {@link ParticleLib#initialize(Plugin)}.</p>
 *
 * <p>Registered as a Bukkit service under {@link ParticleLibAPI} so
 * dependent plugins can retrieve it without a hard class reference:</p>
 * <pre>{@code
 * getServer().getServicesManager().getRegistration(ParticleLibAPI.class)
 * }</pre>
 */
public final class ParticleLibAPIImpl implements ParticleLibAPI {

    private final EffectManager manager;

    public ParticleLibAPIImpl(@NotNull Plugin plugin) {
        this.manager = new EffectManager(plugin);
    }

    @Override
    public @NotNull EffectHandle play(@NotNull String effectId, @NotNull Location location) {
        return EffectRegistry.get().lookup(effectId)
                .map(b -> manager.play(b.build(), EffectContext.at(location)))
                .orElse(EffectHandle.terminated());
    }

    @Override
    public @NotNull EffectHandle play(@NotNull String effectId,
                                      @NotNull Location location,
                                      @NotNull Player targetPlayer) {
        return EffectRegistry.get().lookup(effectId)
                .map(b -> manager.play(b.build(), EffectContext.forPlayer(location, targetPlayer)))
                .orElse(EffectHandle.terminated());
    }

    @Override
    public @NotNull EffectHandle play(@NotNull String effectId,
                                      @NotNull Location origin,
                                      @NotNull Location target) {
        return EffectRegistry.get().lookup(effectId)
                .map(b -> manager.play(b.build(), EffectContext.at(origin, target)))
                .orElse(EffectHandle.terminated());
    }

    @Override
    public @NotNull EffectHandle play(@NotNull ConfigurationSection section,
                                      @NotNull Location location) {
        Effect effect = ConfigEffectLoader.get().load(section);
        if (effect == null) return EffectHandle.terminated();
        return manager.play(effect, EffectContext.at(location));
    }

    @Override
    public @NotNull EffectHandle play(@NotNull ConfigurationSection section,
                                      @NotNull Location location,
                                      @NotNull Player targetPlayer) {
        Effect effect = ConfigEffectLoader.get().load(section);
        if (effect == null) return EffectHandle.terminated();
        return manager.play(effect, EffectContext.forPlayer(location, targetPlayer));
    }

    @Override
    public @NotNull EffectHandle play(@NotNull Effect effect, @NotNull EffectContext context) {
        return manager.play(effect, context);
    }

    @Override
    public @NotNull Optional<EffectBuilder<?>> builder(@NotNull String effectId) {
        return EffectRegistry.get().lookup(effectId);
    }

    @Override
    public void register(@NotNull String id, @NotNull Supplier<EffectBuilder<?>> factory) {
        EffectRegistry.get().register(id, factory);
    }

    @Override
    public void unregister(@NotNull String id) {
        EffectRegistry.get().unregister(id);
    }

    @Override
    public void ignorePlayer(@NotNull Player p) {
        manager.ignorePlayer(p);
    }

    @Override
    public void unignorePlayer(@NotNull Player p) {
        manager.unignorePlayer(p);
    }

    @Override
    public boolean isIgnored(@NotNull Player p) {
        return manager.isIgnored(p);
    }

    @Override
    public void cancelAll() {
        manager.cancelAll();
    }

    /**
     * Called by {@link ParticleLib} on disable.
     */
    void dispose() {
        manager.dispose();
    }
}