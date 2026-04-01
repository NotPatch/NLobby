# NLobby

A feature-rich lobby plugin for [PaperMC](https://papermc.io/) servers, designed for use with BungeeCord/Velocity proxy networks.

## Features

- **Spawn Management** — Set a spawn point and automatically teleport players on join or death.
- **Double Jump** — Let players double-jump in the lobby with customizable particles and sounds.
- **Jet Boost** — Hotbar item that launches players forward or upward with a configurable cooldown.
- **Player Visibility** — Toggle visibility of other players with a single hotbar click.
- **Navigator GUI** — Compass-triggered server selector menu with configurable server entries.
- **Queue System** — Orderly BungeeCord/Velocity queue that sends players to target servers one by one.
- **Direct Connect** — Instantly send one player or all players to a specified server.
- **Chat Formatting** — Custom chat format with level and player name placeholders.
- **Coins System** — Track and manage per-player coin balances stored in a database.
- **Level System** — Track and manage per-player levels with level-up notifications.
- **Join Effects** — Welcome fireworks and customizable title/subtitle on join.
- **Lobby Protection** — Block damage, hunger, building, item pickup, and lock weather/time.
- **Void Teleport** — Automatically teleport players back to spawn when they fall below a configurable Y level.
- **Multi-language Support** — Ships with `en_US` and `tr_TR` locales; easily extendable.
- **SQLite / MySQL Database** — Player data (coins, levels) persisted via HikariCP connection pool.

## Requirements

| Requirement | Version |
|---|---|
| Java | 21+ |
| PaperMC | 1.21+ |
| Proxy | BungeeCord or Velocity (for queue/direct features) |

## Installation

1. Download or build the plugin JAR (see [Building](#building)).
2. Place the JAR in your lobby server's `plugins/` folder.
3. Start (or restart) the server to generate the default configuration.
4. Edit `plugins/NLobby/config.yml` to fit your server setup.
5. Reload the plugin with `/nlobby reload` or restart the server.

## Building

```bash
mvn clean package
```

The compiled JAR will be in the `target/` directory.

## Commands

| Command | Description | Permission |
|---|---|---|
| `/nlobby reload` | Reload the plugin configuration | `nlobby.admin` |
| `/nlobby setspawn` | Set the spawn to your current location | `nlobby.admin` |
| `/nlobby version` | Show the plugin version | — |
| `/spawn` | Teleport yourself to the spawn point | — |
| `/coins give <player> <amount>` | Give coins to a player | `nlobby.coins.give` |
| `/coins take <player> <amount>` | Remove coins from a player | `nlobby.coins.take` |
| `/coins set <player> <amount>` | Set a player's coin balance | `nlobby.coins.set` |
| `/coins check <player>` | Check a player's coin balance | — |
| `/level set <player> <level>` | Set a player's level | `nlobby.admin` |
| `/level check [player]` | Check your or another player's level | — |
| `/queue <player\|*> <server>` | Add a player (or all players) to a queue | `nlobby.admin` |
| `/direct <player\|*> <server>` | Directly send a player (or all players) to a server | `nlobby.admin` |

## Permissions

| Permission | Description |
|---|---|
| `nlobby.admin` | Full administrative access (reload, setspawn, queue, direct, level set) |
| `nlobby.doublejump` | Use double jump (when `permission-required: true`) |
| `nlobby.jet` | Use jet boost (when `permission-required: true`) |
| `nlobby.coins.give` | Give coins to players |
| `nlobby.coins.take` | Take coins from players |
| `nlobby.coins.set` | Set a player's coin balance |

## Configuration

The main configuration file is `config.yml`. Key sections are described below.

### General

```yaml
lang: en_US   # Locale file to use (en_US or tr_TR)
```

### Database

```yaml
database:
  type: SQLITE      # SQLITE or MYSQL
  host: localhost
  port: 3306
  name: nlobby
  username: root
  password: ""
  pool-size: 5
```

### Spawn

```yaml
spawn:
  location: "world,0,64,0,0,0"   # world,x,y,z,yaw,pitch
  teleport-on-join: true
  teleport-on-death: true
```

### Protection

```yaml
protection:
  anti-damage: true
  anti-hunger: true
  anti-build: true
  anti-item-pickup: true
  lock-weather: true
  lock-time: true
  locked-time: 6000   # Minecraft ticks (6000 = noon)
```

### Double Jump

```yaml
double-jump:
  enabled: true
  permission-required: false
  permission: nlobby.doublejump
  velocity-multiplier: 1.5
  particle: CLOUD
  sound: ENTITY_FIREWORK_ROCKET_LAUNCH
```

### Jet Boost

```yaml
jet:
  enabled: true
  permission-required: false
  permission: nlobby.jet
  cooldown-ms: 625
  forward-power: 1.6
  upward-power: 1.2
```

### Chat

```yaml
chat:
  enabled: true
  format: "&7[&f%level%&7] &f%player% &8» &f%message%"
```

### Navigator

```yaml
navigator:
  gui-title: "&8Navigator"
  gui-size: 27
  servers:
    - name: "&aSurvival"
      material: GRASS_BLOCK
      lore:
        - "&7Click to connect"
      slot: 11
      server: survival
```

### Queue

```yaml
queue:
  tick-interval: 3              # Seconds between each queue tick
  show-position-actionbar: true
  actionbar-text: "&6Queue: &f%position% &7/ &f%total% &8| &7Server: &f%server%"
  servers:
    - survival
    - skywars
```

### Join Effects

```yaml
join-effects:
  firework: true
  title: true
  title-text: "&6Welcome back, &f%player%&6!"
  subtitle-text: "&7You have %coins% coins"
  title-fade-in: 10
  title-stay: 40
  title-fade-out: 10
```

### Void Teleport

```yaml
void-teleport:
  enabled: true
  min-y: 0   # Teleport to spawn when player Y falls below this value
```

## Localization

Language files live in `plugins/NLobby/languages/`. The plugin ships with:

- `en_US.yml` — English
- `tr_TR.yml` — Turkish

To add a new language, copy one of the existing files, rename it to your locale code (e.g., `de_DE.yml`), translate the values, and set `lang: de_DE` in `config.yml`.

## Dependencies

| Library | Purpose |
|---|---|
| [PaperMC API](https://papermc.io/) | Minecraft server API |
| [NLib](https://github.com/notpatch/NLib) | Shared utilities by NotPatch |
| [HikariCP](https://github.com/brettwooldridge/HikariCP) | Database connection pooling |
| [SQLite JDBC](https://github.com/xerial/sqlite-jdbc) | SQLite driver |
| [Lombok](https://projectlombok.org/) | Boilerplate reduction |

## Author

**NotPatch**
