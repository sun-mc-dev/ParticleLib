# ParticleLib

A modern particle effects library for Paper 1.21+ and Folia. Drop it in as a standalone plugin or shade it into your own — either way you get the same API.

Built as a clean rewrite with proper async support, a fluent builder API, YAML-driven effects, and first-class Folia compatibility.

---

## Effects

**Shapes** — `sphere`, `circle`, `cube`, `cuboid`, `donut`, `cone`, `pyramid`, `star`, `heart`, `square`, `grid`, `hill`, `animated_ball`

**Traces** — `line`, `arc`, `helix`

**Motion** — `vortex`, `tornado`, `dna`, `wave`, `fountain`, `dragon`

**Ambient** — `flame`, `smoke`, `cloud`, `love`, `music`, `warp`, `shield`, `earth`, `explode`, `big_bang`, `disco_ball`, `icon`, `trace`, `particle`

**Entity** — `bleed`, `atom`

**Special** — `equation`, `sound`, `image`, `colored_image`, `text`, `modified`, `plot`

---

## Installation

### Standalone

Download the jar and drop it in your `plugins/` folder. Add it as a dependency in your `plugin.yml`:

```yaml
depend:
  - ParticleLib
```

Then get the API:

```java
RegisteredServiceProvider<ParticleLibAPI> reg =
    Bukkit.getServicesManager().getRegistration(ParticleLibAPI.class);

if (reg != null) {
    ParticleLibAPI api = reg.getProvider();
}
```

### Shaded (JitPack)

Add JitPack to your repositories:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Add the dependency:

```xml
<dependencies>
    <dependency>
        <groupId>com.github.sun-mc-dev</groupId>
        <artifactId>ParticleLib</artifactId>
        <version>VERSION</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

Relocate the package to avoid conflicts with other plugins shading the same library:

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

Initialize in your plugin:

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

### Play by name

```java
ParticleLib.api().play("sphere", player.getLocation());
```

### Fluent builder

```java
SphereEffect.builder()
    .radius(2.0)
    .color(Color.AQUA)
    .iterations(100)
    .playAt(player.getLocation());
```

### Play for a specific player only

```java
ParticleLib.api().play("vortex", location, targetPlayer);
```

### Cancel mid-flight

```java
EffectHandle handle = ParticleLib.api().play("tornado", location);

// cancel after 3 seconds
Bukkit.getAsyncScheduler().runDelayed(plugin, t -> handle.cancel(), 3, TimeUnit.SECONDS);
```

### Pause and resume

```java
handle.pause();
handle.resume();
```

### YAML-driven effects

Define effects in your config:

```yaml
death_effect:
  class: sphere
  particle: DUST
  color: FF0000
  radius: 2.0
  iterations: 60
  period: 1
```

Then play them:

```java
ConfigurationSection section = config.getConfigurationSection("death_effect");
ParticleLib.api().play(section, player.getLocation());
```

### Custom effect

```java
ParticleLib.api().register("myplugin:custom", MyCustomEffect::builder);

// play it like any built-in
ParticleLib.api().play("myplugin:custom", location);
```

### Equation-based effect

```java
EquationEffect.builder()
    .x("4 * sin(t)")
    .y("0")
    .z("4 * cos(t)")
    .stepsPerTick(2)
    .maxSteps(628)
    .playAt(location);
```

### Events

```java
@EventHandler
public void onEffectPlay(EffectPlayEvent e) {
    if (e.getEffect().id().equals("big_bang")) {
        e.setCancelled(true);
    }
}

@EventHandler
public void onEffectCancel(EffectCancelEvent e) {
    if (e.getReason() == CancelReason.COMPLETED) {
        // effect finished naturally
    }
}
```

### Suppress particles for a player

```java
// useful for accessibility or low-end clients
ParticleLib.api().ignorePlayer(player);
ParticleLib.api().unignorePlayer(player);
```

---

## Configuration

```yaml
# When true, particles bypass the default 32-block client visibility cap.
# Only effective on Paper 1.21.4+ and Folia.
force-show-particles: false

performance:
  # 0 = unlimited
  max-active-effects: 500

  # Exclude players whose render distance is below the threshold
  respect-client-render-distance: true
  min-render-distance-chunks: 4
```

---

## Commands

| Command           | Description                         |
|-------------------|-------------------------------------|
| `/plib info`      | Version and registered effect count |
| `/plib list`      | List all registered effect IDs      |
| `/plib play <id>` | Play an effect at your location     |
| `/plib reload`    | Reload config                       |

Permission: `particlelib.admin` (default: op)

---

## Requirements

- Java 21+
- Paper 1.21.4+ or Folia 1.21.4+

---

## License

MIT