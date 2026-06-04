# FlySpeedLimit

A lightweight **Spigot / Paper** plugin that provides an EssentialsX-style `/speed` command while enforcing configurable fly and walk speed caps — fixing EssentialsX's broken `max-fly-speed` setting.

## Features

- EssentialsX-compatible `/speed <walk|fly> <0-10> [player]` command
- Separate `/flyspeed` and `/walkspeed` commands with independent permissions
- **Per-player max speed overrides** via permission nodes (`flyspeedlimit.maxfly.<n>`)
- Configurable default max fly speed and max walk speed (0–10 EssentialsX scale)
- Real-time fly speed enforcement via PlayerMoveEvent
- **Block-placement rate limiter** — prevents fastplace advantage from high fly speed
- Optional clamp on player join
- Bypass permission for admins
- Tab completion filtered to what the sender is allowed to do
- In-game `/flyspeedlimit reload` (`/fsl reload`)

## Requirements

| Software | Version |
|----------|---------|
| Java | 17+ |
| Spigot / Paper | 1.21+ |
| EssentialsX | Optional |

## Installation

1. Drop the JAR into your `plugins/` folder.
2. Restart the server.
3. Edit `plugins/FlySpeedLimit/config.yml`.
4. Run `/fsl reload` to apply changes without a restart.

## Overriding EssentialsX

To make this plugin own `/speed` and all its aliases instead of EssentialsX, disable EssentialsX's `speed` command in `plugins/Essentials/config.yml`:

```yaml
disabled-commands:
  - speed
```

## Permissions

### Command permissions

| Permission | Default | Description |
|---|---|---|
| `flyspeedlimit.speed` | `false` | Grants both `.speed.fly` **and** `.speed.walk` (parent node) |
| `flyspeedlimit.speed.fly` | `false` | Allows `/flyspeed`, `/fspeed`, and `/speed fly` |
| `flyspeedlimit.speed.walk` | `false` | Allows `/walkspeed`, `/wspeed`, and `/speed walk` |
| `flyspeedlimit.speed.others` | `op` | Allows changing another player's speed |
| `flyspeedlimit.admin` | `op` | Run `/fsl reload` |
| `flyspeedlimit.bypass` | `false` | Bypass all speed caps and the rate limiter |

> **Note:** `flyspeedlimit.speed` is a convenience parent — giving a player this permission automatically implies both `.speed.fly` and `.speed.walk`. You can also grant each individually.

### Per-player max speed overrides

Grant `flyspeedlimit.maxfly.<n>` or `flyspeedlimit.maxwalk.<n>` (where `<n>` is 1–10) to raise a player's effective speed cap above the config default. The **highest** permission node the player has wins. If they have none, the config value is used.

**Examples (LuckPerms):**

```bash
# Give the "donor" group a fly cap of 5 instead of the default 2
/lp group donor permission set flyspeedlimit.maxfly.5 true

# Give a specific player a fly cap of 8
/lp user Steve permission set flyspeedlimit.maxfly.8 true

# Give the "vip" group access to /flyspeed only (not /walkspeed)
/lp group vip permission set flyspeedlimit.speed.fly true
```

## Configuration

```yaml
# Default max fly speed — overridable per-player via flyspeedlimit.maxfly.<n>
max-fly-speed: 2

# Default max walk speed — overridable per-player via flyspeedlimit.maxwalk.<n>
max-walk-speed: 10

# Clamp fly speed when a player logs in
enforce-on-join: true

# Prevent fastplace advantage from high fly speed
block-place-rate-limit: true
```

### Speed Scale Reference

| Value | Effect |
|---|---|
| `1` | Default Minecraft fly speed |
| `2` | 2× normal (recommended default cap) |
| `5` | 5× normal |
| `10` | No limit (Minecraft hard cap) |

## How the Fastplace Fix Works

Higher fly speeds cause the client to send more movement packets per second. Because Minecraft’s block placement is driven by client interaction packets — not an independent server-side timer — faster fliers can place blocks more rapidly than players at default speed.

The `BlockPlaceRateLimiter` enforces a proportional cooldown:

```
required_interval = 250ms × (player_fly_speed / baseline_speed)
```

Placement is only throttled while the player is **actively flying**. Walking players and players with `flyspeedlimit.bypass` are unaffected.

## Commands

| Command | Permission | Description |
|---|---|---|
| `/speed <walk\|fly> <0-10> [player]` | `.speed.fly` or `.speed.walk` | Set speed (tab-completion reflects what you’re allowed) |
| `/flyspeed <0-10> [player]` | `.speed.fly` | Set fly speed |
| `/fspeed <0-10> [player]` | `.speed.fly` | Alias for `/flyspeed` |
| `/walkspeed <0-10> [player]` | `.speed.walk` | Set walk speed |
| `/wspeed <0-10> [player]` | `.speed.walk` | Alias for `/walkspeed` |
| `/fsl reload` | `.admin` | Reload config |

## Building

```bash
git clone https://github.com/TheFlood424K/FlySpeedLimit.git
cd FlySpeedLimit
mvn package
# Output: target/FlySpeedLimit-1.3.0.jar
```

## License

MIT — see [LICENSE](LICENSE).
