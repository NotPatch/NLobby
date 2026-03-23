# NLobby — Geliştirme Planı

---

## Özellikler

### Core (Öncelikli)

- [ ] **Spawn Sistemi** — spawn ayarla, join/ölümde ışınla, `/spawn` ve `/setspawn` komutları
- [ ] **Join / Quit Yönetimi** — karşılama mesajı, join başlığı, broadcast
- [ ] **Hotbar Sistemi** — join'de temizle, config'den yüklenebilir slot/item/action tanımı
- [ ] **Koruma Sistemi** — anti-damage, anti-hunger, anti-build, anti-item-pickup, hava kilidi, saat kilidi, anti-void
- [ ] **Veritabanı** — HikariCP, SQLite (varsayılan) veya MySQL, async CRUD
- [ ] **Oyuncu Profili** — coins, level, playtime (DB'de saklanır)
- [ ] **Chat Format** — prefix, isim, suffix, mesaj
- [ ] **Double Jump** — configurable hız çarpanı, particle + ses efekti
- [ ] **Görünürlük Toggle** — oyuncuları gizle/göster (hotbar'dan)
- [ ] **Navigator GUI** — Compass → sunucu seçim menüsü
- [ ] **Queue Sistemi** — sunucuya tıklayınca sıraya gir, sıra gelince Velocity üzerinden aktar
- [ ] **Admin Komutları** — `/nlobby reload`, `/nlobby setspawn`, `/coins`, `/level`

### Sonraki Aşama (Şimdilik Yok)

- [ ] **Kozmetik Sistemi** — trail, şapka, gadget (DB'de saklanır, coin ile satın alınır)

---

## Paket Yapısı

```
com.notpatch.nLobby
├── NLobby.java
├── LanguageLoader.java
├── LibraryLoader.java
├── command/
│   ├── NLobbyCommand.java      (reload, setspawn, version)
│   ├── SpawnCommand.java
│   ├── CoinsCommand.java
│   └── LevelCommand.java
├── listener/
│   ├── PlayerJoinListener.java
│   ├── PlayerQuitListener.java
│   ├── PlayerDeathListener.java
│   ├── PlayerRespawnListener.java
│   ├── PlayerMoveListener.java
│   ├── PlayerInteractListener.java
│   ├── ProtectionListener.java
│   ├── ChatListener.java
│   └── PluginMessageListener.java  (Velocity PlayerCount yanıtları)
├── manager/
│   ├── SpawnManager.java
│   ├── HotbarManager.java
│   ├── DoubleJumpManager.java
│   ├── VisibilityManager.java
│   ├── ChatManager.java
│   └── QueueManager.java           (sıra yönetimi + tick + aktar)
├── gui/
│   ├── BaseGUI.java
│   ├── GUIItem.java
│   └── NavigatorGUI.java
├── database/
│   ├── DatabaseManager.java
│   └── PlayerDAO.java
├── model/
│   ├── LobbyPlayer.java
│   └── QueueEntry.java             (uuid, serverName, joinTime)
├── cache/
│   └── PlayerCache.java
├── config/
│   └── ConfigManager.java
└── util/
    └── LocationSerializer.java
```

---

## Veritabanı Şeması

```sql
CREATE TABLE IF NOT EXISTS nlobby_players (
    uuid       VARCHAR(36) NOT NULL PRIMARY KEY,
    name       VARCHAR(16) NOT NULL,
    coins      BIGINT      NOT NULL DEFAULT 0,
    level      INT         NOT NULL DEFAULT 1,
    playtime   BIGINT      NOT NULL DEFAULT 0,
    first_join TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_join  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

## Config Yapısı

```yaml
lang: en_US

database:
  type: SQLITE       # SQLITE veya MYSQL
  host: localhost
  port: 3306
  name: nlobby
  username: root
  password: ""
  pool-size: 5

spawn:
  location: "world,0.5,64,0.5,0,0"
  teleport-on-join: true
  teleport-on-death: true

protection:
  anti-damage: true
  anti-hunger: true
  anti-build: true
  anti-item-pickup: true
  lock-weather: true
  lock-time: true
  locked-time: 6000

hotbar:
  items:
    navigator:
      slot: 0
      material: COMPASS
      name: "&aNavigator"
      lore:
        - "&7Sunuculara göz at"
      action: OPEN_NAVIGATOR
    visibility:
      slot: 4
      material: ENDER_EYE
      name: "&boyuncuları Gizle/Göster"
      lore:
        - "&7Tıkla"
      action: TOGGLE_VISIBILITY

double-jump:
  enabled: true
  permission-required: false
  permission: nlobby.doublejump
  velocity-multiplier: 1.5
  particle: CLOUD
  sound: ENTITY_FIREWORK_ROCKET_LAUNCH

chat:
  enabled: true
  format: "&7[&f%level%&7] &f%player% &8» &f%message%"

navigator:
  gui-title: "&8Navigator"
  gui-size: 27
  servers:
    - name: "&aSurvival"
      material: GRASS_BLOCK
      lore:
        - "&7Bağlanmak için tıkla"
      slot: 11
      server: survival
    - name: "&cSkyWars"
      material: FEATHER
      lore:
        - "&7Bağlanmak için tıkla"
      slot: 13
      server: skywars

join-effects:
  firework: true
  title: true
  title-text: "&6Tekrar hoş geldin, &f%player%&6!"
  subtitle-text: "&7%coins% coin'in var"
  title-fade-in: 10
  title-stay: 40
  title-fade-out: 10

queue:
  # Kaç saniyede bir sıradaki oyuncu aktarılır
  tick-interval: 3
  # Sunucu dolu görünse bile zorla bağlan (kapasite bilinmiyorsa)
  send-when-unknown: true
  # Oyuncuya action bar'da konum göster
  show-position-actionbar: true
  actionbar-text: "&6Sıra: &f%position% &7/ &f%total% &8| &7Sunucu: &f%server%"
```

---

## Dil Dosyası (en_US.yml)

```yaml
general.no-permission: "&cBunu yapmaya yetkin yok."
general.player-only: "&cBu komut sadece oyuncular tarafından kullanılabilir."
general.plugin-reloaded: "&aNLobby yeniden yüklendi."

join.welcome: "&6Sunucuya hoş geldin, &f%player%&6!"
join.broadcast: "&a%player% &7lobiye katıldı."
quit.broadcast: "&c%player% &7ayrıldı."

spawn.teleported: "&aSpawn'a ışınlandın."
spawn.set: "&aSpawn noktası konumuna ayarlandı."

coins.balance: "&6Bakiyen: &f%coins% coin."
coins.given: "&f%player% &ayerine &f%amount% coin &averildi."
coins.removed: "&f%player% &cyerine &f%amount% coin &calındı."

level.current: "&6Seviyeniz: &f%level%."
level.set: "&f%player% &aseviyes &f%level% &aolarak ayarlandı."

visibility.hidden: "&cOyuncular gizlendi."
visibility.shown: "&aOyuncular gösterildi."

queue.joined: "&aSıraya girdin: &f%server% &7(%position%. sırada)"
queue.left: "&cSıradan çıktın."
queue.sending: "&aAktarılıyorsun: &f%server%"
queue.already-in-queue: "&cZaten bir sunucu sırasındasın."
```

---

## Uygulama Sırası

1. **Altyapı** — `LocationSerializer`, `ConfigManager`, `DatabaseManager`, `LobbyPlayer`, `PlayerCache`, `PlayerDAO`
2. **Oyuncu Yaşam Döngüsü** — `SpawnManager`, `HotbarManager`, `PlayerJoinListener`, `PlayerQuitListener`, `ProtectionListener`, temel komutlar, dil dosyaları
3. **Görsel Sistemler** — `ChatManager`
4. **GUI ve Navigasyon** — `BaseGUI`, `GUIItem`, `NavigatorGUI`, `PlayerInteractListener`
5. **Queue Sistemi** — `QueueEntry`, `QueueManager`, `PluginMessageListener`, action bar tick
6. **Oynanış** — `DoubleJumpManager`, `VisibilityManager`, `PlayerMoveListener`
7. **Ekonomi ve Admin** — `CoinsCommand`, `LevelCommand`, permission kayıtları