# ParticleLib

Particle effects library for Paper 1.21+ and Folia. Use it as a standalone plugin or shade it — the API is the same either way.

> ⚠️ This is an alpha release. The API may change between versions.

---

## What's included

**Shapes** — `sphere` `circle` `cube` `cuboid` `donut` `cone` `pyramid` `star` `heart` `square` `grid` `hill` `animated_ball`

**Traces** — `line` `arc` `helix`

**Motion** — `vortex` `tornado` `dna` `wave` `fountain` `dragon`

**Ambient** — `flame` `smoke` `cloud` `love` `music` `warp` `shield` `earth` `explode` `big_bang` `disco_ball` `icon` `trace` `particle`

**Entity** — `bleed` `atom`

**Special** — `equation` `sound` `image` `colored_image` `text` `modified` `plot`

---

## Setup

### Standalone

Put the jar in your `plugins/` folder, then declare the dependency in your `plugin.yml`:

```yaml
depend:
  - ParticleLib
```

Grab the API instance:

```java
RegisteredServiceProvider<ParticleLibAPI> reg =
    Bukkit.getServicesManager().getRegistration(ParticleLibAPI.class);

if (reg != null) {
    ParticleLibAPI api = reg.getProvider();
}
```

Or use the static helper if you have a compile dependency on `particlelib-api`:

```java
ParticleLib.api().play("sphere", player.getLocation());
```

---

### Standalone — Maven (JitPack)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.sun-mc-dev.ParticleLib</groupId>
        <artifactId>particlelib-api</artifactId>
        <version>v0.1-alpha</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

---

### Shaded — Maven (JitPack)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.sun-mc-dev.ParticleLib</groupId>
        <artifactId>particlelib-core</artifactId>
        <version>v0.1-alpha</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

Relocate to avoid conflicts with other plugins shading the same library:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <executions>
        <execution>
            <phase>package</phase>
            <goals><goal>shade</goal></goals>
            <configuration>
                <relocations>
                    <relocation>
                        <pattern>me.sunmc.particlelib</pattern>
                        <shadedPattern>com.yourplugin.libs.particlelib</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Initialize and shut down with your plugin:

```java
@Override
public void onEnable() {
    ParticleLib.initialize(this);
}

@Override
public void onDisable() {
    ParticleLib.shutdown(this);
}
```

---

## Usage

**Play by name:**
```java
ParticleLib.api().play("sphere", player.getLocation());
```

**Fluent builder:**
```java
SphereEffect.builder()
    .radius(2.0)
    .color(Color.AQUA)
    .iterations(100)
    .playAt(player.getLocation());
```

**Only visible to one player:**
```java
ParticleLib.api().play("vortex", location, targetPlayer);
```

**Cancel mid-flight:**
```java
EffectHandle handle = ParticleLib.api().play("tornado", location);
Bukkit.getAsyncScheduler().runDelayed(plugin, t -> handle.cancel(), 3, TimeUnit.SECONDS);
```

**Pause / resume:**
```java
handle.pause();
handle.resume();
```

---

## YAML-driven effects

Define an effect in your config:

```yaml
death_effect:
  class: sphere
  particle: DUST
  color: FF0000
  radius: 2.0
  iterations: 60
  period: 1
  particle-count: 3
```

Play it:

```java
ConfigurationSection section = config.getConfigurationSection("death_effect");
ParticleLib.api().play(section, player.getLocation());
```

The `class` key maps to any registered effect ID. Everything else is optional — unset keys fall back to the effect's own defaults. You can swap `class: sphere` for `class: vortex` and the rest stays the same.

---

## Custom effects

```java
ParticleLib.api().register("myplugin:burst", MyBurstEffect::builder);
ParticleLib.api().play("myplugin:burst", location);
```

---

## Equation effects

```java
EquationEffect.builder()
    .x("4 * sin(t)")
    .y("0")
    .z("4 * cos(t)")
    .stepsPerTick(2)
    .maxSteps(628)
    .playAt(location);
```

Supported functions beyond standard math: `rand(min, max)`, `prob(p, a, b)`, `min(a, b)`, `max(a, b)`, `select(v, neg, zero, pos)`.

---

## Events

```java
@EventHandler
public void onEffectPlay(EffectPlayEvent e) {
    // cancel before it starts
    if (e.getEffect().id().equals("big_bang")) {
        e.setCancelled(true);
    }
}

@EventHandler
public void onEffectCancel(EffectCancelEvent e) {
    // COMPLETED, CANCELLED, or ERROR
    if (e.getReason() == CancelReason.COMPLETED) {
        // do something after the effect finishes naturally
    }
}
```

---

## Suppress particles for a player

```java
ParticleLib.api().ignorePlayer(player);   // stop sending packets to this player
ParticleLib.api().unignorePlayer(player); // resume
```

Useful for accessibility toggles or players on low-end hardware. The ignored state is cleared automatically when the player disconnects.

---

## config.yml

```yaml
# Uses Paper's forceShow flag to bypass the 32-block visibility cap.
# Requires Paper 1.21.4+ or Folia.
force-show-particles: false

performance:
  max-active-effects: 500        # 0 = unlimited
  respect-client-render-distance: true
  min-render-distance-chunks: 4
```

---

## Commands

| Command           | Description                     |
|-------------------|---------------------------------|
| `/plib info`      | Version and active effect count |
| `/plib list`      | All registered effect IDs       |
| `/plib play <id>` | Play an effect at your feet     |
| `/plib reload`    | Reload config.yml               |

Permission: `particlelib.admin` (default: op)

---

## Requirements

- Java 21+
- Paper 1.21.4+ or Folia 1.21.4+

---

## License

MIT

---

## Credits

- SunMC(@sun-mc-dev) - Main Writer
- EffectLib Team - Thank-you for developing so far.
- Other Tech used - Thank-you all!