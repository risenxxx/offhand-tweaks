# Offhand Tweaks

**Stop accidental shield blocks and misplacing torches!** Offhand Tweaks is a lightweight, highly customizable Quality of Life (QoL) mod that gives you complete control over your offhand slot. Selectively disable offhand right-click interactions based on item categories, freeing up your Right Mouse Button (RMB) for interacting with the world, opening chests, or tweaking machinery without constantly unequipping your offhand item.

## ✨ Features

Every player can configure their own independent settings. The mod allows you to toggle offhand right-click interactions for specific categories:

* 🛡️ **Shields:** Disable right-click blocking. Perfect when you only want the shield for passive stats or are working with complex block interactions.
* 💡 **Light Sources:** Stop accidentally placing torches, lanterns, or lamps while trying to interact with blocks in front of you.
* 🍖 **Food:** Toggle the ability to eat food directly from your offhand. 
* 🧱 **Other Blocks & Items:** A global toggle to restrict placing or using any other unspecified blocks and items from the offhand slot.

## ⚙️ How It Works (Zero Desync)

Unlike simple client-side macros or basic server-side scripts that cause annoying "ghost blocks," inventory desyncs, or jittery animations, **Offhand Tweaks uses a robust Client-Server synchronization system.** When you change your settings, your client immediately notifies the server via a custom network packet. This ensures that the event is gracefully canceled on both logical sides at the exact same time. 
* **No ghost blocks.**
* **No fake hand-swing animations.**
* **No wasted items.**

## 🔧 Configuration & Setup

* **Client-Side Config:** Players can easily change their preferences through the standard Forge config menu (or by editing the config file directly). 
* **Multiplayer Ready:** Each player on the server has their own isolated configuration. Disabling your offhand shield won't affect your friends!
* **Installation:** For multiplayer, this mod **must be installed on BOTH the Client and the Server**. In singleplayer, just drop it into your `mods` folder.

## 📦 Requirements
* Minecraft 1.20.1
* Minecraft Forge

## Technical Details

Each client decides — independently of the server — whether the offhand should activate on RMB for:

| Toggle                  | Covers                                                                                    | Default |
|-------------------------|-------------------------------------------------------------------------------------------|---------|
| `allowShieldRMB`        | Any `ShieldItem` (vanilla shields and modded shields)                                     | `true`  |
| `allowFoodRMB`          | Edible items (`Item.isEdible()`)                                                          | `true`  |
| `allowLightSourcesRMB`  | Items in the `#offhandtweaks:light_sources` tag (torches, lanterns, candles, glowstone, …) | `false` |
| `allowOtherBlocksRMB`   | Any other offhand item not covered above                                                  | `false` |

Shields and food are allowed by default (the common QoL target is preventing torch/block misplacement, not blocking the defensive/eating flow). Flip any toggle to change that — `true` restores vanilla behaviour for that category, `false` blocks it.

### How it prevents desync

The mod cancels interactions on **both logical sides**:

- **Client side** — so there is no prediction, no ghost placement, no hand swing.
- **Server side** — so the authoritative action never runs.

The server knows each player's preferences because the client sends a `ConfigSyncPacket` over a Forge `SimpleChannel` on login and on every config reload. The server stashes that state in a per-UUID cache (`ServerConfigCache`) and clears it on disconnect. If a client never syncs (e.g. a vanilla client on a modded server), the server falls open to vanilla behaviour rather than blocking.

### Development

```sh
./gradlew runClient      # launch a dev client
./gradlew runServer      # launch a dev server
./gradlew build          # produce build/libs/offhandtweaks-<version>.jar
```

Requires JDK 17.

### Configuration file

After running once, edit `run/config/offhandtweaks-client.toml`:

```toml
allowShieldRMB = true
allowLightSourcesRMB = false
allowFoodRMB = true
allowOtherBlocksRMB = false
```

Reloading the config (it hot-reloads) re-pushes the new state to the connected server automatically.

The mod also ships a **built-in config screen**: open the Mods list → select Offhand Tweaks → click **Config**. Each toggle is a vanilla `CycleButton`, so ON shows in green and OFF in red — identical styling to Minecraft's own options menu. Changes save and re-sync to the server immediately.

### Extending the light-sources tag

Add items to `#offhandtweaks:light_sources` from any datapack or modpack override — no recompile required.

### CI / Publishing

GitHub Actions (`.github/workflows/build-and-publish.yml`) builds on every push to `main` and every PR. The workflow also **auto-creates a GitHub Release** whenever `mod_version` in `gradle.properties` is bumped to a value that does not already have a matching `vX.Y.Z` git tag.

Release flow:

1. Edit `mod_version=X.Y.Z` in [gradle.properties](gradle.properties).
2. Commit and push to `main`.
3. The workflow reads the version, checks whether `vX.Y.Z` already exists as a tag, and — if not — builds, creates the tag + GitHub Release, and uploads the built jar.
4. If the optional Modrinth / CurseForge secrets are set, the same run additionally publishes to those platforms.

Required repository secrets for third-party publishing (all optional — workflow skips silently if absent):

- `MODRINTH_TOKEN`
- `CURSEFORGE_TOKEN`

Project identifiers are not secret and live in [gradle.properties](gradle.properties) as `modrinth_project_id` and `curseforge_project_id`.

Tasks are wired up through `com.modrinth.minotaur` and `net.darkhax.curseforgegradle` in `build.gradle`. Publish tasks only materialise when their respective env vars are present, so missing secrets never fail the build.

## First-time setup note

If `gradle/wrapper/gradle-wrapper.jar` is missing (some distribution channels omit binary blobs), regenerate it once:

```sh
gradle wrapper --gradle-version 8.1.1 --distribution-type bin
```

After that, `./gradlew` works everywhere including CI.

## License

MIT.
