# FlySpeedLimit

A lightweight **Spigot / Paper** plugin that caps fly speed to a configurable maximum value — filling the gap left by EssentialsX's broken `max-fly-speed` setting.

## Features

- Configurable maximum fly speed (uses the same **0–10 scale** as `/speed fly` in EssentialsX)
- Enforces the cap in real-time as players fly (catches `/speed fly` commands immediately)
- Optionally clamps a player's fly speed when they join
- Bypass permission for admins / trusted players
- In-game `/flyspeedlimit reload` command
- Friendly, configurable messages

## Requirements

| Software | Version |
|----------|---------|
| Java | 17+ |
| Spigot / Paper | 1.21+ |
| EssentialsX | Optional (not required) |

## Installation

1. Download the latest JAR from [Releases](../../releases).
2. Drop it into your `plugins/` folder.
3. Restart the server.
4. Edit `plugins/FlySpeedLimit/config.yml` to your liking.
5. Run `/fsl reload` to apply changes without a restart.

## Building from Source

```bash
git clone https://github.com/TheFlood424K/FlySpeedLimit.git
cd FlySpeedLimit
mvn package
# Output: target/FlySpeedLimit-1.0.0.jar
```

## Configuration

```yaml
# plugins/FlySpeedLimit/config.yml

# Maximum fly speed on the EssentialsX 0–10 scale.
# Default (2) allows /speed fly 1 and /speed fly 2, but blocks 3+.
max-fly-speed: 2

# If true, a player's fly speed is clamped when they log in.
enforce-on-join: true

messages:
  speed-clamped: "&cYour fly speed has been capped to the server maximum of &e{max}&c."
  reload-success: "&aFlySpeedLimit config reloaded."
```

### Speed Scale Reference

| `max-fly-speed` | Meaning |
|---|---|
| `1` | Default Minecraft fly speed only |
| `2` | Up to 2× normal (recommended) |
| `5` | Up to 5× normal |
| `10` | No limit (Minecraft hard cap) |

## Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/flyspeedlimit reload` | `flyspeedlimit.admin` | Reloads the config file |
| `/fsl reload` | `flyspeedlimit.admin` | Alias for the above |

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `flyspeedlimit.admin` | `op` | Allows `/fsl reload` |
| `flyspeedlimit.bypass` | `false` | Bypasses the fly speed limit |

## How It Works

EssentialsX's `max-fly-speed` config only accepts 0.1–1.0 and [doesn't reliably enforce limits](https://github.com/EssentialsX/Essentials/issues/942). This plugin listens for player move events while flying (at `LOWEST` priority, after EssentialsX sets the speed) and immediately resets the speed to the configured maximum if it's been exceeded.

Minecraft stores fly speed as a float from `0.0` to `1.0`. EssentialsX's `/speed fly` command accepts `0`–`10` and divides by 10 internally. FlySpeedLimit uses the same conversion, so `max-fly-speed: 2` in the config corresponds directly to `/speed fly 2`.

## License

MIT — see [LICENSE](LICENSE).
