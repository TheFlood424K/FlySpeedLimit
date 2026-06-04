# FlySpeedLimit

[![Build](https://github.com/TheFlood424K/FlySpeedLimit/actions/workflows/build.yml/badge.svg)](https://github.com/TheFlood424K/FlySpeedLimit/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A lightweight **Spigot / Paper** plugin that provides an EssentialsX-style `/speed` command while enforcing configurable fly and walk speed caps — fixing EssentialsX’s broken `max-fly-speed` setting.

---

## Compatibility

### Server Software

| Software | Supported | Notes |
|---|---|---|
| [Paper](https://papermc.io) | ✅ Full | Primary target. All features work. |
| [Purpur](https://purpurmc.org) | ✅ Full | Drop-in Paper fork. All features work. |
| [Pufferfish](https://github.com/pufferfish-gg/Pufferfish) | ✅ Full | Paper fork. All features work. |
| [Leaf](https://github.com/Winds-Studio/Leaf) | ✅ Full | Paper fork. All features work. |
| [Spigot](https://www.spigotmc.org) | ⚠️ Partial | Fully functional, but Paper API extensions not available. Recommended to use Paper instead. |
| [Folia](https://github.com/PaperMC/Folia) | ❌ Not supported | Folia’s region-threaded architecture is incompatible with the PlayerMoveEvent listener used for real-time speed enforcement. A Folia-specific build is not planned. |
| CraftBukkit / Bukkit | ❌ Not supported | Missing API features required by this plugin. |

### Minecraft Versions

| Version | Status | Notes |
|---|---|---|
| 1.21.4 | ✅ Tested | Primary build target (`paper-api 1.21.4-R0.1-SNAPSHOT`) |
| 1.21.3 | ✅ Compatible | Same API surface |
| 1.21.2 | ✅ Compatible | Same API surface |
| 1.21.1 | ✅ Compatible | Same API surface |
| 1.21 | ✅ Compatible | Same API surface |
| 1.20.x | ⚠️ Untested | May work; not officially supported |
| 1.19.x and below | ❌ Not supported | API differences may cause build or runtime failures |

> **Recommended:** Paper 1.21.4 with Java 21.

---

## Features

- EssentialsX-compatible `/speed <walk|fly> <0-10> [player]` command
- Separate `/flyspeed` and `/walkspeed` commands with **independent permissions**
- **Per-player max speed overrides** via permission nodes (`flyspeedlimit.maxfly.<n>`)
- Configurable default max fly and walk speed (0–10 EssentialsX scale)
- Real-time fly speed enforcement via `PlayerMoveEvent` — catches EssentialsX bypasses
- **Block-placement rate limiter** — prevents fastplace advantage from elevated fly speed
- Optional clamp on player join (`enforce-on-join`)
- Tab completion filtered to the sender’s actual permissions
- In-game `/flyspeedlimit reload` (`/fsl reload`) — no restart required

---

## Requirements

| Requirement | Version |
|---|---|
| Java | 17 minimum, **21 recommended** |
| Server software | Paper / Purpur / Pufferfish / Leaf 1.21+ |
| EssentialsX | Optional (see [Overriding EssentialsX](#overriding-essentialsx)) |

---

## Installation

1. Download the latest JAR from [Releases](https://github.com/TheFlood424K/FlySpeedLimit/releases) or [build it yourself](#building).
2. Drop the JAR into your server’s `plugins/` folder.
3. Start or restart the server.
4. Edit `plugins/FlySpeedLimit/config.yml` to your liking.
5. Run `/fsl reload` to apply config changes without a restart.

---

## Overriding EssentialsX

To make this plugin own `/speed` and all its aliases instead of EssentialsX, disable the `speed` command in EssentialsX’s config:

```yaml
# plugins/Essentials/config.yml
disabled-commands:
  - speed
```

Disabling `speed` in EssentialsX also disables its `/flyspeed`, `/fspeed`, `/walkspeed`, and `/wspeed` aliases. This plugin registers all of them and will take over cleanly.

If you do **not** disable it in EssentialsX, the `PlayerMoveEvent` listener still acts as a safety net and clamps any speed above the configured max in real time.

---

## Permissions

### Command Permissions

| Permission | Default | Description |
|---|---|---|
| `flyspeedlimit.speed` | `false` | **Parent node** — grants both `.speed.fly` and `.speed.walk` implicitly |
| `flyspeedlimit.speed.fly` | `false` | Allows `/flyspeed`, `/fspeed`, and `/speed fly` |
| `flyspeedlimit.speed.walk` | `false` | Allows `/walkspeed`, `/wspeed`, and `/speed walk` |
| `flyspeedlimit.speed.others` | `op` | Allows changing another player’s speed |
| `flyspeedlimit.admin` | `op` | Allows `/fsl reload` |
| `flyspeedlimit.bypass` | `false` | Bypasses all speed caps and the block-place rate limiter |

> **Note:** Granting `flyspeedlimit.speed` is a shortcut that implies both `.speed.fly` and `.speed.walk`. You can also grant each independently to restrict a rank to one command type.

### Per-Player Max Speed Overrides

Grant `flyspeedlimit.maxfly.<n>` or `flyspeedlimit.maxwalk.<n>` (where `<n>` is 1–10) to raise a player’s effective speed cap above the config default. The **highest** node the player has wins. If they have none, the config value is used as the fallback.

| Permission | Effect |
|---|---|
| `flyspeedlimit.maxfly.3` | Player can fly up to speed 3 |
| `flyspeedlimit.maxfly.5` | Player can fly up to speed 5 |
| `flyspeedlimit.maxwalk.5` | Player can walk up to speed 5 |
| `flyspeedlimit.bypass` | Player ignores all caps (equivalent to maxfly.10 + maxwalk.10) |

**LuckPerms examples:**

```bash
# Give "donor" rank fly access capped at speed 5
/lp group donor permission set flyspeedlimit.speed.fly true
/lp group donor permission set flyspeedlimit.maxfly.5 true

# Give "vip" rank both fly and walk commands, with a fly cap of 3
/lp group vip permission set flyspeedlimit.speed true
/lp group vip permission set flyspeedlimit.maxfly.3 true

# Give a specific player a personal fly cap of 8
/lp user Steve permission set flyspeedlimit.maxfly.8 true
```

---

## Commands

| Command | Permission | Description |
|---|---|---|
| `/speed <walk\|fly> <0-10> [player]` | `.speed.fly` or `.speed.walk` | Set speed; tab-completion reflects what you’re allowed |
| `/flyspeed <0-10> [player]` | `.speed.fly` | Set fly speed |
| `/fspeed <0-10> [player]` | `.speed.fly` | Alias for `/flyspeed` |
| `/walkspeed <0-10> [player]` | `.speed.walk` | Set walk speed |
| `/wspeed <0-10> [player]` | `.speed.walk` | Alias for `/walkspeed` |
| `/flyspeedlimit reload` | `.admin` | Reload `config.yml` |
| `/fsl reload` | `.admin` | Alias for `/flyspeedlimit reload` |

---

## Configuration

Full default `config.yml`:

```yaml
# Max fly speed — fallback when no flyspeedlimit.maxfly.<n> permission is present
# EssentialsX 0-10 scale (native Minecraft: value / 10)
max-fly-speed: 2

# Max walk speed — fallback when no flyspeedlimit.maxwalk.<n> permission is present
# Set to 10 for no practical default limit
max-walk-speed: 10

# Clamp fly speed to the effective max when a player logs in
enforce-on-join: true

# Prevent fastplace advantage from elevated fly speed
# Scales block placement cooldown proportionally so faster fliers
# cannot place blocks faster than a player at baseline fly speed
block-place-rate-limit: true

messages:
  speed-clamped: "&cYour fly speed has been capped to the server maximum of &e{max}&c."
  reload-success: "&aFlySpeedLimit config reloaded."
  no-permission: "&cYou do not have permission to do that."
  invalid-number: "&cSpeed must be a number between 0 and 10."
  usage-speed: "&eUsage: /speed <walk|fly> <0-10> [player]"
  usage-flyspeed: "&eUsage: /flyspeed <0-10> [player]"
  usage-walkspeed: "&eUsage: /walkspeed <0-10> [player]"
  player-not-found: "&cPlayer not found."
  walk-set-self: "&aWalk speed set to &e{speed}&a."
  fly-set-self: "&aFly speed set to &e{speed}&a."
  walk-set-other: "&aSet &e{player}&a's walk speed to &e{speed}&a."
  fly-set-other: "&aSet &e{player}&a's fly speed to &e{speed}&a."
```

### Speed Scale Reference

| Value | Approx. effect |
|---|---|
| `1` | Default Minecraft creative fly speed |
| `2` | 2× default (recommended server cap) |
| `5` | 5× default |
| `10` | Minecraft hard cap (no practical limit) |

---

## How the Fastplace Fix Works

Higher fly speeds cause the client to send more movement packets per second. Because Minecraft’s block placement is driven by client interaction packets rather than an independent server-side timer, faster fliers can effectively place blocks more rapidly than players at default speed.

`BlockPlaceRateLimiter` enforces a proportional cooldown:

```
required_interval = 250ms × (player_fly_speed / baseline_speed)
```

A player flying at 2× speed must wait 500 ms between placements, keeping their effective blocks-per-second identical to a player at baseline. Throttling only applies while the player is **actively flying**. Walking players and players with `flyspeedlimit.bypass` are completely unaffected.

---

## Building

### Prerequisites

- Java 17 or 21
- Maven 3.8+

```bash
git clone https://github.com/TheFlood424K/FlySpeedLimit.git
cd FlySpeedLimit
mvn package
# Output: target/FlySpeedLimit-<version>.jar
```

The GitHub Actions workflow builds on every push to `main` and on every pull request. JARs are uploaded as workflow artifacts and attached to tagged releases automatically.

---

## License

MIT — see [LICENSE](LICENSE).
