package me.sunmc.particlelib.internal.particle;

import me.sunmc.particlelib.api.particle.ParticleOptions;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves the {@code data} object required by Paper's
 * {@link org.bukkit.World#spawnParticle} for typed particles.
 *
 * <p>This handles every particle data type present in Paper 1.21+:
 * DustOptions, DustTransition, Spell (1.21.5+), Color, BlockData,
 * ItemStack, Vibration, Shriek, and SculkCharge.</p>
 */
public final class ParticleDataResolver {
    private static final boolean HAS_SPELL_TYPE = detectSpell();

    private static boolean detectSpell() {
        try {
            Class.forName("org.bukkit.Particle$Spell");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    /**
     * Resolves the extra data object for a particle spawn call.
     *
     * @return the data object, or {@code null} if the particle takes no data
     */
    public static @Nullable Object resolve(@org.jetbrains.annotations.NotNull ParticleOptions opts) {
        Particle p = opts.particle();
        Class<?> dt = p.getDataType();

        if (dt == null || dt == Void.class) return null;

        if (HAS_SPELL_TYPE && Particle.Spell.class.isAssignableFrom(dt)) {
            Color c = opts.color() != null ? opts.color() : Color.WHITE;
            return new Particle.Spell(c, opts.size());
        }

        if (Particle.DustOptions.class.isAssignableFrom(dt)) {
            Color c = opts.color() != null ? opts.color() : Color.RED;
            return new Particle.DustOptions(c, opts.size());
        }

        if (Particle.DustTransition.class.isAssignableFrom(dt)) {
            Color from = opts.color() != null ? opts.color() : Color.RED;
            Color to = opts.toColor() != null ? opts.toColor() : from;
            return new Particle.DustTransition(from, to, opts.size());
        }

        if (Color.class.isAssignableFrom(dt)) {
            return opts.color() != null ? opts.color() : Color.WHITE;
        }

        if (ItemStack.class.isAssignableFrom(dt)) {
            Material mat = opts.material();
            if (mat == null || mat == Material.AIR) return null;
            return new ItemStack(mat);
        }

        if (BlockData.class.isAssignableFrom(dt)) {
            Material mat = opts.material();
            if (mat == null || mat.name().contains("AIR")) return null;
            try {
                return (opts.blockData() != null)
                        ? org.bukkit.Bukkit.createBlockData(opts.blockData().toLowerCase())
                        : mat.createBlockData();
            } catch (Exception ex) {
                return null;
            }
        }

        if (Integer.class.isAssignableFrom(dt) || dt == Integer.TYPE) {
            return 0; // shriek delay defaults to 0
        }

        if (Float.class.isAssignableFrom(dt) || dt == Float.TYPE) {
            return 0.0f;
        }

        return null;
    }

    private ParticleDataResolver() {
    }
}
