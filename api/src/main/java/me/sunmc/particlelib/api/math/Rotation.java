package me.sunmc.particlelib.api.math;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable Euler rotation (all angles stored in <em>radians</em>).
 *
 * <p>Provides helpers to convert from Bukkit degree-based yaw/pitch and
 * to apply the rotation to a {@link Vec3}.</p>
 */
public record Rotation(double x, double y, double z) {

    public static final Rotation IDENTITY = new Rotation(0, 0, 0);

    /**
     * Creates a rotation from angles in <em>degrees</em>.
     */
    public static @NotNull Rotation ofDegrees(double xDeg, double yDeg, double zDeg) {
        return new Rotation(Math.toRadians(xDeg), Math.toRadians(yDeg), Math.toRadians(zDeg));
    }

    /**
     * Creates a rotation from a Bukkit {@link Location}'s yaw and pitch
     * (converting from degrees to radians automatically).
     */
    public static @NotNull Rotation fromLocation(@NotNull Location loc) {
        return new Rotation(
                Math.toRadians(loc.getPitch()),
                Math.toRadians(loc.getYaw()),
                0
        );
    }

    public @NotNull Rotation add(@NotNull Rotation o) {
        return new Rotation(x + o.x, y + o.y, z + o.z);
    }

    public @NotNull Rotation multiply(double s) {
        return new Rotation(x * s, y * s, z * s);
    }

    public @NotNull Rotation withX(double nx) {
        return new Rotation(nx, y, z);
    }

    public @NotNull Rotation withY(double ny) {
        return new Rotation(x, ny, z);
    }

    public @NotNull Rotation withZ(double nz) {
        return new Rotation(x, y, nz);
    }

    /**
     * Applies this rotation (X then Y then Z) to a vector.
     */
    public @NotNull Vec3 apply(@NotNull Vec3 v) {
        Vec3 result = v;
        if (x != 0) result = result.rotateX(x);
        if (y != 0) result = result.rotateY(y);
        if (z != 0) result = result.rotateZ(z);
        return result;
    }

    public double xDegrees() {
        return Math.toDegrees(x);
    }

    public double yDegrees() {
        return Math.toDegrees(y);
    }

    public double zDegrees() {
        return Math.toDegrees(z);
    }

    @Override
    public String toString() {
        return "Rotation[x=%.3f°, y=%.3f°, z=%.3f°]"
                .formatted(xDegrees(), yDegrees(), zDegrees());
    }
}