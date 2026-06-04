# FlySpeedLimit

A lightweight **Spigot / Paper** plugin that provides an EssentialsX-style `/speed` command while enforcing configurable fly and walk speed caps — fixing EssentialsX's broken `max-fly-speed` setting.

## Features

- EssentialsX-compatible `/speed <walk|fly> <0-10> [player]` command
- Aliases: `/flyspeed`, `/fspeed`, `/walkspeed`, `/wspeed`
- Configurable max fly speed and max walk speed (0–10 EssentialsX scale)
- Real-time enforcement via PlayerMoveEvent (catches EssentialsX `/speed` too if not disabled)
- Optional clamp on player join
- Bypass permission for admins
- Tab completion for type, value, and player name
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

EssentialsX treats `speed` as the root command — disabling it also disables `/flyspeed`, `/fspeed`, `/walkspeed`, and `/wspeed` from EssentialsX. This plugin will then handle all of them.

If you do **not** disable it in EssentialsX, the PlayerMoveEvent listener still acts as a safety net and will clamp any speed above the configured max in real-time.

## Configuration

```yaml
# plugins/FlySpeedLimit/config.yml

# Max fly speed (EssentialsX 0-10 scale)
# 2 = allows /speed fly 1 and /speed fly 2, blocks /speed fly 3+
max-fly-speed: 2

# Max walk speed (set to 10 for no practical limit)
max-walk-speed: 10

# Clamp fly speed when a player logs in
enforce-on-join: true

messages:
  speed-clamped: "&cYour fly speed has been capped to the server maximum of &e{max}&c."
  reload-success: "&aFlySpeedLimit config reloaded."
  no-permission: "&cYou do not have permission to do that."
  invalid-number: "&cSpeed must be a number between 0 and 10."
  invalid-type: "&cUsage: /speed <walk|fly> <0-10> [player]"
  player-not-found: "&cPlayer not found."
  walk-set-self: "&aWalk speed set to &e{speed}&a."
  fly-set-self: "&aFly speed set to &e{speed}&a."
  walk-set-other: "&aSet &e{player}&a's walk speed to &e{speed}&a."
  fly-set-other: "&aSet &e{player}&a's fly speed to &e{speed}&a."
```

### Speed Scale Reference

| `max-fly-speed` | Effect |
|---|---|
| `1` | Default Minecraft fly speed only |
| `2` | Up to 2× normal (recommended) |
| `5` | Up to 5× normal |
| `10` | No limit (Minecraft hard cap) |

## Commands

| Command | Description |
|---------|-------------|
| `/speed <walk\|fly> <0-10> [player]` | Set walk or fly speed |
| `/flyspeed <0-10> [player]` | Alias — sets fly speed |
| `/walkspeed <0-10> [player]` | Alias — sets walk speed |
| `/fsl reload` | Reload config |

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `flyspeedlimit.speed` | `true` | Use `/speed` |
| `flyspeedlimit.speed.others` | `op` | Change another player's speed |
| `flyspeedlimit.admin` | `op` | Run `/fsl reload` |
| `flyspeedlimit.bypass` | `false` | Bypass speed caps |

## Building

```bash
git clone https://github.com/TheFlood424K/FlySpeedLimit.git
cd FlySpeedLimit
mvn package
# Output: target/FlySpeedLimit-1.1.0.jar
```

## License

MIT — see [LICENSE](LICENSE).
