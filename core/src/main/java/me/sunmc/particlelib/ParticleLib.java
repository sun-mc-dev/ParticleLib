package me.sunmc.particlelib;

import me.sunmc.particlelib.api.ParticleLibAPI;
import me.sunmc.particlelib.api.effect.Effect;
import me.sunmc.particlelib.api.effect.EffectContext;
import me.sunmc.particlelib.api.effect.EffectHandle;
import me.sunmc.particlelib.internal.scheduler.PlatformScheduler;
import me.sunmc.particlelib.math.equation.EquationStore;
import me.sunmc.particlelib.registry.EffectRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main plugin class and static service locator for ParticleLib.
 *
 * <h2>Standalone mode</h2>
 * <p>ParticleLib is installed as its own plugin. Other plugins list it under
 * {@code depend} or {@code softdepend} in their {@code plugin.yml}. At runtime
 * they can retrieve the API either via the Bukkit service manager:</p>
 * <pre>{@code
 * RegisteredServiceProvider<ParticleLibAPI> reg =
 *     Bukkit.getServicesManager().getRegistration(ParticleLibAPI.class);
 * if (reg != null) ParticleLibAPI api = reg.getProvider();
 * }</pre>
 * <p>… or via the static helper (requires a compile dependency on
 * {@code particlelib-api}):</p>
 * <pre>{@code ParticleLib.api().play("sphere", location); }</pre>
 *
 * <h2>Shaded mode</h2>
 * <p>The consumer shades {@code particlelib-core} (relocating
 * {@code me.sunmc.particlelib} to avoid conflicts) and calls
 * {@link #initialize(Plugin)} in {@code onEnable}:</p>
 * <pre>{@code
 * // In your plugin's onEnable:
 * ParticleLib.initialize(this);
 *
 * // Then use the API:
 * ParticleLib.api().play("sphere", location);
 *
 * // In onDisable:
 * ParticleLib.shutdown(this);
 * }</pre>
 */
public final class ParticleLib extends JavaPlugin {

    private static volatile Plugin owningPlugin = null;
    private static volatile ParticleLibAPIImpl apiImpl = null;

    // Per-consumer-plugin API instances (for multi-plugin shaded scenarios)
    private static final Map<String, ParticleLibAPIImpl> pluginApis = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        owningPlugin = this;
        boot(this);

        // Register as a Bukkit service so dependents can look us up
        Bukkit.getServicesManager().register(
                ParticleLibAPI.class, apiImpl, this, ServicePriority.Normal);

        getLogger().info("ParticleLib enabled — platform: "
                + (PlatformScheduler.isFolia() ? "Folia" : "Paper")
                + " | effects: " + EffectRegistry.get().registeredIds().size());
    }

    @Override
    public void onDisable() {
        shutdown();
        Bukkit.getServicesManager().unregisterAll(this);
        getLogger().info("ParticleLib disabled.");
    }

    /**
     * Initializes ParticleLib for a consumer plugin (shaded mode).
     * Call in {@code onEnable} before playing any effects.
     *
     * @param plugin your plugin instance
     */
    public static synchronized void initialize(@NotNull Plugin plugin) {
        if (owningPlugin == null) {
            owningPlugin = plugin;
            boot(plugin);
        }
        // Each consumer plugin gets its own scoped API instance
        pluginApis.computeIfAbsent(plugin.getName(), k -> new ParticleLibAPIImpl(plugin));
    }

    /**
     * Shuts down ParticleLib for a consumer plugin (shaded mode).
     * Call in {@code onDisable}.
     */
    public static synchronized void shutdown(@NotNull Plugin plugin) {
        ParticleLibAPIImpl impl = pluginApis.remove(plugin.getName());
        if (impl != null) impl.dispose();

        if (plugin.equals(owningPlugin)) {
            shutdown();
            owningPlugin = null;
        }
    }

    /**
     * Returns the {@link ParticleLibAPI} for the given plugin (scoped instance).
     *
     * @throws IllegalStateException if {@link #initialize(Plugin)} was not called
     */
    public static @NotNull ParticleLibAPI api(@NotNull Plugin plugin) {
        ParticleLibAPIImpl impl = pluginApis.get(plugin.getName());
        if (impl != null) return impl;
        // Fallback: try the global instance (standalone mode)
        if (apiImpl != null) return apiImpl;
        throw new IllegalStateException(
                "ParticleLib not initialized for plugin '" + plugin.getName()
                        + "'. Call ParticleLib.initialize(this) in onEnable().");
    }

    /**
     * Returns the global API instance (standalone mode or primary shaded consumer).
     *
     * @throws IllegalStateException if ParticleLib has not been initialized
     */
    public static @NotNull ParticleLibAPI api() {
        if (apiImpl != null) return apiImpl;
        // Check Bukkit service (standalone mode)
        RegisteredServiceProvider<ParticleLibAPI> reg =
                Bukkit.getServicesManager().getRegistration(ParticleLibAPI.class);
        if (reg != null) return reg.getProvider();
        throw new IllegalStateException(
                "ParticleLib is not enabled. Install it as a plugin or call initialize().");
    }

    /**
     * Returns the plugin instance used for scheduling.
     * Required by {@link me.sunmc.particlelib.effects.AbstractEffect#play}.
     *
     * @throws IllegalStateException if ParticleLib has not been initialized
     */
    public static @NotNull Plugin getInstance() {
        Plugin p = owningPlugin;
        if (p == null) throw new IllegalStateException(
                "ParticleLib has not been initialized.");
        return p;
    }

    /**
     * Plays a built-in effect by name at {@code location}.
     */
    public static @NotNull EffectHandle play(@NotNull String effectId,
                                             @NotNull org.bukkit.Location location) {
        return api().play(effectId, location);
    }

    /**
     * Plays a pre-built {@link Effect} in the given {@link EffectContext}.
     */
    public static @NotNull EffectHandle play(@NotNull Effect effect,
                                             @NotNull EffectContext ctx) {
        return api().play(effect, ctx);
    }

    private static void boot(@NotNull Plugin plugin) {
        EffectRegistry.get().registerBuiltins();
        apiImpl = new ParticleLibAPIImpl(plugin);
    }

    private static void shutdown() {
        if (apiImpl != null) {
            apiImpl.dispose();
            apiImpl = null;
        }
        pluginApis.values().forEach(ParticleLibAPIImpl::dispose);
        pluginApis.clear();
        EquationStore.get().clear();
    }
}