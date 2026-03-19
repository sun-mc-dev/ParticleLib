package me.sunmc.particlelib.registry;

import me.sunmc.particlelib.api.effect.*;
import me.sunmc.particlelib.config.ConfigManager;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Loads and plays effects from YAML {@link ConfigurationSection}s.
 *
 * <h3>Config format</h3>
 * <pre>
 * my_effect:
 *   class: sphere        # required
 *   particle: DUST
 *   color: "FF0000"
 *   size: 1.5
 *   period: 2
 *   iterations: 100
 *   radius: 2.0          # effect-specific key
 * </pre>
 *
 * <h3>Java 21 reflection fix</h3>
 * <p>The previous version called {@code field.getField(key)} on builder
 * instances without {@code setAccessible(true)}, which throws
 * {@link InaccessibleObjectException} under Java 21's strong encapsulation.
 * All reflective field access now calls {@code setAccessible(true)} inside a
 * try-catch so unknown or inaccessible fields are silently skipped rather than
 * crashing.</p>
 */
public final class ConfigEffectLoader {

    private static final ConfigEffectLoader INSTANCE = new ConfigEffectLoader();

    public static ConfigEffectLoader get() {
        return INSTANCE;
    }

    private ConfigEffectLoader() {
    }

    public @NotNull EffectHandle play(@NotNull ConfigurationSection section,
                                      @NotNull EffectContext ctx) {
        Effect effect = load(section);
        return effect == null ? EffectHandle.terminated() : effect.play(ctx);
    }

    /**
     * Loads an {@link Effect} from {@code section} without playing it.
     * Returns {@code null} when the section is missing or has an unknown class.
     */
    public @Nullable Effect load(@NotNull ConfigurationSection section) {
        String id = section.getString("class");
        if (id == null || id.isBlank()) return null;

        Optional<EffectBuilder<?>> opt = EffectRegistry.get().lookup(id);
        if (opt.isEmpty()) return null;

        EffectBuilder<?> b = opt.get();
        applyCommonKeys(b, section);
        applyExtraKeys(b, section);
        return b.build();
    }

    private void applyCommonKeys(@NotNull EffectBuilder<?> b,
                                 @NotNull ConfigurationSection s) {
        // scheduling
        if (s.contains("type")) {
            try {
                b.type(EffectType.valueOf(s.getString("type", "REPEATING").toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (s.contains("delay")) b.delay(s.getInt("delay", 0));
        if (s.contains("period")) b.period(s.getInt("period", 1));
        if (s.contains("iterations")) b.iterations(s.getInt("iterations", 20));
        if (s.contains("duration")) b.duration(s.getInt("duration"));

        // particle
        if (s.contains("particle")) {
            try {
                b.particle(Particle.valueOf(s.getString("particle", "FLAME").toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (s.contains("color")) b.color(parseColor(s.getString("color")));
        if (s.contains("to_color")) b.toColor(parseColor(s.getString("to_color")));
        if (s.contains("toColor")) b.toColor(parseColor(s.getString("toColor")));
        if (s.contains("size")) b.particleSize((float) s.getDouble("size", 1.0));
        if (s.contains("speed")) b.speed((float) s.getDouble("speed", 0));
        if (s.contains("count")) b.particleCount(s.getInt("count", 1));

        // display
        if (s.contains("visible_range")) b.visibleRange(s.getDouble("visible_range", 32));
        if (s.contains("visibleRange")) b.visibleRange(s.getDouble("visibleRange", 32));
        if (s.contains("probability")) b.probability(s.getDouble("probability", 1));
        if (s.contains("force_show")) b.forceShow(s.getBoolean("force_show", false));
        if (s.contains("forceShow")) b.forceShow(s.getBoolean("forceShow", false));

        // offset  (supports "offset: 0.1,0.1,0.1" or "offset: 0.1" for uniform)
        String offsetStr = s.getString("offset");
        if (offsetStr != null) {
            String[] parts = offsetStr.split(",");
            try {
                if (parts.length == 3) {
                    b.offset(Float.parseFloat(parts[0].trim()),
                            Float.parseFloat(parts[1].trim()),
                            Float.parseFloat(parts[2].trim()));
                } else if (parts.length == 1) {
                    b.offset(Float.parseFloat(parts[0].trim()));
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // async
        if (s.contains("async")) b.async(s.getBoolean("async", true));
        if (s.contains("asynchronous")) b.async(s.getBoolean("asynchronous", true));
    }

    /**
     * Applies effect-specific config keys to the builder via reflection.
     *
     * <p>For each key not in the common set, we attempt to find a public field
     * of the same name (camelCase) on the builder and set it. Unknown keys and
     * inaccessible fields are silently skipped — never crash on bad config.</p>
     *
     * <p><strong>Java 21 fix:</strong> {@code field.setAccessible(true)} is
     * always called before setting the value, wrapped in its own try-catch so
     * a {@link java.lang.reflect.InaccessibleObjectException} doesn't abort
     * the entire config load.</p>
     */
    private void applyExtraKeys(@NotNull EffectBuilder<?> b,
                                @NotNull ConfigurationSection s) {
        for (String key : s.getKeys(false)) {
            if (isCommonKey(key)) continue;

            // Convert kebab-case / snake_case → camelCase
            String fieldName = toCamelCase(key);
            Object rawValue = s.get(key);
            if (rawValue == null) continue;

            Field field = findField(b.getClass(), fieldName);
            if (field == null) field = findField(b.getClass(), key); // try as-is
            if (field == null) continue;

            try {
                field.setAccessible(true);
                setFieldValue(field, b, s, key);
            } catch (Exception ignored) {
                // Unknown type or inaccessible — skip silently
            }
        }
    }

    private void setFieldValue(@NotNull Field field,
                               @NotNull Object target,
                               @NotNull ConfigurationSection s,
                               @NotNull String key) throws IllegalAccessException {
        Class<?> type = field.getType();

        if (type == int.class || type == Integer.class) {
            field.set(target, s.getInt(key));
            return;
        }
        if (type == double.class || type == Double.class) {
            field.set(target, s.getDouble(key));
            return;
        }
        if (type == float.class || type == Float.class) {
            field.set(target, (float) s.getDouble(key));
            return;
        }
        if (type == boolean.class || type == Boolean.class) {
            field.set(target, s.getBoolean(key));
            return;
        }
        if (type == long.class || type == Long.class) {
            field.set(target, s.getLong(key));
            return;
        }
        if (type == String.class) {
            field.set(target, s.getString(key));
            return;
        }
        if (type == Color.class) {
            field.set(target, parseColor(s.getString(key)));
            return;
        }

        if (type.isEnum()) {
            String v = s.getString(key);
            if (v != null) {
                try {
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    Enum<?> en = Enum.valueOf((Class<Enum>) type, v.toUpperCase());
                    field.set(target, en);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private @Nullable Field findField(@NotNull Class<?> clazz, @NotNull String name) {
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
            c = c.getSuperclass();
        }
        return null;
    }

    /**
     * Converts {@code snake_case} and {@code kebab-case} to {@code camelCase}.
     */
    private @NotNull String toCamelCase(@NotNull String key) {
        if (!key.contains("_") && !key.contains("-")) return key;
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : key.toCharArray()) {
            if (c == '_' || c == '-') {
                upper = true;
            } else if (upper) {
                sb.append(Character.toUpperCase(c));
                upper = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private boolean isCommonKey(@NotNull String k) {
        return switch (k.toLowerCase()) {
            case "class", "type", "delay", "period", "iterations", "duration",
                    "particle", "color", "to_color", "tocolor", "size", "speed",
                    "count", "visible_range", "visiblerange", "probability",
                    "force_show", "forceshow", "offset", "async", "asynchronous" -> true;
            default -> false;
        };
    }

    private @Nullable Color parseColor(@Nullable String s) {
        if (s == null) return null;
        if (s.equalsIgnoreCase("random")) {
            var rng = ThreadLocalRandom.current();
            return Color.fromRGB(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
        }
        try {
            String hex = s.startsWith("#") ? s.substring(1) : s;
            return Color.fromRGB(Integer.parseInt(hex, 16));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}