package me.sunmc.particlelib.api.math;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable 3D vector, independent of any mutable Bukkit state.
 *
 * <p>All arithmetic operations return new instances — this record is safe
 * to share across threads without synchronization.</p>
 */
public record Vec3(double x, double y, double z) {

    public static final Vec3 ZERO = new Vec3(0, 0, 0);
    public static final Vec3 ONE = new Vec3(1, 1, 1);
    public static final Vec3 UP = new Vec3(0, 1, 0);
    public static final Vec3 NORTH = new Vec3(0, 0, -1);
    public static final Vec3 EAST = new Vec3(1, 0, 0);

    public @NotNull Vec3 add(@NotNull Vec3 o) {
        return new Vec3(x + o.x, y + o.y, z + o.z);
    }

    public @NotNull Vec3 add(double dx, double dy, double dz) {
        return new Vec3(x + dx, y + dy, z + dz);
    }

    public @NotNull Vec3 subtract(@NotNull Vec3 o) {
        return new Vec3(x - o.x, y - o.y, z - o.z);
    }

    public @NotNull Vec3 subtract(double dx, double dy, double dz) {
        return new Vec3(x - dx, y - dy, z - dz);
    }

    public @NotNull Vec3 multiply(double s) {
        return new Vec3(x * s, y * s, z * s);
    }

    public @NotNull Vec3 divide(double s) {
        return multiply(1.0 / s);
    }

    public @NotNull Vec3 negate() {
        return new Vec3(-x, -y, -z);
    }

    public @NotNull Vec3 withX(double nx) {
        return new Vec3(nx, y, z);
    }

    public @NotNull Vec3 withY(double ny) {
        return new Vec3(x, ny, z);
    }

    public @NotNull Vec3 withZ(double nz) {
        return new Vec3(x, y, nz);
    }

    public double lengthSquared() {
        return x * x + y * y + z * z;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double dot(@NotNull Vec3 o) {
        return x * o.x + y * o.y + z * o.z;
    }

    public double distanceTo(@NotNull Vec3 o) {
        return subtract(o).length();
    }

    public double distanceSquaredTo(@NotNull Vec3 o) {
        return subtract(o).lengthSquared();
    }

    public @NotNull Vec3 cross(@NotNull Vec3 o) {
        return new Vec3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x);
    }

    public @NotNull Vec3 normalize() {
        double l = length();
        return l < 1e-10 ? ZERO : divide(l);
    }

    public @NotNull Vec3 lerp(@NotNull Vec3 target, double t) {
        return new Vec3(x + (target.x - x) * t,
                y + (target.y - y) * t,
                z + (target.z - z) * t);
    }

    public @NotNull Vec3 rotateX(double a) {
        double c = Math.cos(a), s = Math.sin(a);
        return new Vec3(x, y * c - z * s, y * s + z * c);
    }

    public @NotNull Vec3 rotateY(double a) {
        double c = Math.cos(a), s = Math.sin(a);
        return new Vec3(x * c + z * s, y, -x * s + z * c);
    }

    public @NotNull Vec3 rotateZ(double a) {
        double c = Math.cos(a), s = Math.sin(a);
        return new Vec3(x * c - y * s, x * s + y * c, z);
    }

    /**
     * Rotates this vector to match the yaw/pitch of a Bukkit {@link Location},
     * using the same convention EffectLib used (SexyToad's algorithm).
     *
     * @param yawDeg   yaw in degrees
     * @param pitchDeg pitch in degrees
     */
    public @NotNull Vec3 rotateByYawPitch(float yawDeg, float pitchDeg) {
        double yaw = Math.toRadians(-1.0 * (yawDeg + 90));
        double pitch = Math.toRadians(-pitchDeg);
        double cosY = Math.cos(yaw), sinY = Math.sin(yaw);
        double cosP = Math.cos(pitch), sinP = Math.sin(pitch);
        // pitch rotation (Z axis)
        double x1 = x * cosP - y * sinP;
        double y1 = x * sinP + y * cosP;
        // yaw rotation (Y axis)
        double z1 = z * cosY - x1 * sinY;
        double x2 = z * sinY + x1 * cosY;
        return new Vec3(x2, y1, z1);
    }

    /**
     * Rotates this vector using a {@link Location}'s yaw and pitch.
     */
    public @NotNull Vec3 rotateByLocation(@NotNull Location loc) {
        return rotateByYawPitch(loc.getYaw(), loc.getPitch());
    }

    public static @NotNull Vec3 fromLocation(@NotNull Location loc) {
        return new Vec3(loc.x(), loc.y(), loc.z());
    }

    public static @NotNull Vec3 fromVector(@NotNull Vector v) {
        return new Vec3(v.getX(), v.getY(), v.getZ());
    }

    public @NotNull Location toLocation(@NotNull World world) {
        return new Location(world, x, y, z);
    }

    public @NotNull Location toLocation(@NotNull World world, float yaw, float pitch) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public @NotNull Vector toVector() {
        return new Vector(x, y, z);
    }

    @Override
    public String toString() {
        return "Vec3[x=%.4f, y=%.4f, z=%.4f]".formatted(x, y, z);
    }
}