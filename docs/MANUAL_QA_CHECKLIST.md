# Ashmare Manual QA and Validation Checklist

Version under test: `1.0.2`
Target: Minecraft `1.21.11`, Fabric Loader `0.19.3+`, Java `21`  
Status values: `PASS`, `FAIL`, `BLOCKED`, `NOT RUN`

## Test Environment

Use a dedicated test server and disposable world. Keep backups of
`config/ashmare/`, `banned-players.json`, `ops.json`, and the world before each
destructive test group.

Required accounts and roles:

- `OWNER`: the configured Ashmare owner, preferably `Shyha`, not present in `ops.json`.
- `OP4`: vanilla operator permission level 4.
- `OP2`: vanilla operator permission level 2, not an Ashmare owner.
- `P1`, `P2`, `P3`: normal non-operator players.
- `VIEWER`: normal player used to observe names, skins, chat, and sounds.
- For stress tests, 20 or more real clients or controlled test bots.

Unless a test says otherwise:

- Start with online mode enabled and all clients on Minecraft `1.21.11`.
- Run commands from a player client, not the server console.
- Use `/ashmare chat <toggle> true` to restore chat defaults after chat tests.
- Use `/ashmare names clear all`, `/ashmare skins clear all`, and
  `/ashmare unban <player>` during cleanup.
- Record screenshots, relevant server-log lines, and config diffs as evidence.

Expected first-run files under `config/ashmare/`:

- `deathban.json`
- `sound.json`
- `chat.json`
- `names.json`
- `skins.json`
- `exclusions.json`
- `owners.json`
- `skins.txt`

## Project and Startup

### PS-01 Fresh server startup

- **Purpose:** Verify Ashmare starts on a clean Fabric server.
- **Setup:** New server directory containing Fabric Loader, Fabric API, and the built Ashmare JAR; no world or `config/ashmare/`.
- **Exact steps:** 1. Accept the EULA. 2. Start the server with Java 21. 3. Wait for `Done`. 4. Stop with `stop`.
- **Expected result:** Server reaches `Done` without Ashmare exceptions and stops cleanly.
- **Files/configs affected:** New world files, logs, and all files in `config/ashmare/`.
- **Pass/fail criteria:** PASS if startup and shutdown complete with no Ashmare error or mixin failure; otherwise FAIL.

### PS-02 Empty repository build verification

- **Purpose:** Verify a clean source checkout builds without generated artifacts.
- **Setup:** Fresh checkout with `build/`, `.gradle/`, `run/`, and local config absent.
- **Exact steps:** 1. Install/select Java 21. 2. Run `./gradlew clean build` or `gradlew.bat clean build`. 3. Inspect `build/libs/`.
- **Expected result:** Gradle reports `BUILD SUCCESSFUL`; remapped and sources JARs are produced.
- **Files/configs affected:** `.gradle/`, `build/`.
- **Pass/fail criteria:** PASS if the clean build succeeds without source changes; otherwise FAIL.

### PS-03 Mod loading verification

- **Purpose:** Confirm Fabric discovers and initializes Ashmare.
- **Setup:** Running test server with Ashmare and Fabric API installed.
- **Exact steps:** 1. Start the server. 2. Inspect `logs/latest.log`. 3. As `OP2`, run `/ashmare help`.
- **Expected result:** Log contains `Ashmare initialized.` and the command returns the Ashmare help line.
- **Files/configs affected:** `logs/latest.log`.
- **Pass/fail criteria:** PASS if both initialization and command registration are confirmed; otherwise FAIL.

### PS-04 Config folder creation

- **Purpose:** Verify the required config directory is created automatically.
- **Setup:** Stop server and remove only `config/ashmare/`.
- **Exact steps:** 1. Confirm the directory is absent. 2. Start server. 3. Inspect `config/`.
- **Expected result:** `config/ashmare/` exists before gameplay begins.
- **Files/configs affected:** `config/ashmare/`.
- **Pass/fail criteria:** PASS if the directory is automatically created; otherwise FAIL.

### PS-05 Config file auto-generation

- **Purpose:** Verify every required config file is generated with usable defaults.
- **Setup:** `config/ashmare/` absent.
- **Exact steps:** 1. Start and stop server once. 2. List the directory. 3. Parse each JSON file. 4. Confirm `skins.txt` exists.
- **Expected result:** All eight expected files exist; JSON is valid; deathban defaults to delay `10` and duration `1d`; sound defaults to thunder and radius `64`; all chat toggles, including death messages, default to `true`.
- **Files/configs affected:** All Ashmare config files.
- **Pass/fail criteria:** PASS only if every file and default is correct; otherwise FAIL.

### PS-06 Missing config recovery

- **Purpose:** Verify a single missing config is recreated without resetting others.
- **Setup:** Existing configs with a recognizable non-default value, such as sound radius `37`.
- **Exact steps:** 1. Stop server. 2. Delete `chat.json` only. 3. Restart. 4. Inspect `chat.json` and `sound.json`.
- **Expected result:** `chat.json` is regenerated with defaults; `sound.json` still contains radius `37`.
- **Files/configs affected:** Recreated `chat.json`; other configs read only.
- **Pass/fail criteria:** PASS if only the missing file is reset; otherwise FAIL.

### PS-07 Server restart persistence

- **Purpose:** Verify saved state survives a normal restart.
- **Setup:** Running server with `OP2`, `P1`, and at least one changed setting and persisted assignment.
- **Exact steps:** 1. Set sound radius `41`. 2. Disable advancement messages. 3. Exclude `P1`. 4. Stop and restart. 5. Inspect configs and commands.
- **Expected result:** Radius, toggle, and exclusion remain unchanged after restart.
- **Files/configs affected:** `sound.json`, `chat.json`, `exclusions.json`.
- **Pass/fail criteria:** PASS if all three values persist and are active; otherwise FAIL.

## Owner System

### OW-01 owners.json first-run generation

- **Purpose:** Verify owner config creation.
- **Setup:** Server stopped; `config/ashmare/owners.json` absent.
- **Exact steps:** 1. Start server. 2. Wait 15 seconds. 3. Inspect `owners.json`.
- **Expected result:** File exists and contains an `owners` array.
- **Files/configs affected:** `owners.json`.
- **Pass/fail criteria:** PASS if the file is generated automatically and parses as JSON; otherwise FAIL.

### OW-02 Default Shyha owner

- **Purpose:** Verify `Shyha` is the only first-generation default owner.
- **Setup:** Fresh config generation.
- **Exact steps:** 1. Open the newly generated `owners.json`. 2. Count entries. 3. Inspect `lastKnownUsername`.
- **Expected result:** Exactly one entry exists and its username is `Shyha`; UUID may briefly be `null` before lookup completes.
- **Files/configs affected:** `owners.json`.
- **Pass/fail criteria:** PASS if exactly one default entry named `Shyha` exists; otherwise FAIL.

### OW-03 Shyha UUID resolution

- **Purpose:** Verify Mojang profile resolution stores UUID and canonical username.
- **Setup:** Online server with outbound access to Mojang APIs and unresolved `Shyha` entry.
- **Exact steps:** 1. Start server. 2. Wait up to 30 seconds. 3. Inspect `owners.json` and `latest.log`. 4. Compare UUID with Mojang's official profile lookup.
- **Expected result:** UUID becomes non-null and matches the official account; log reports resolution. If the API is unavailable, username remains and the retry warning is logged.
- **Files/configs affected:** `owners.json`, `logs/latest.log`.
- **Pass/fail criteria:** PASS when reachable API resolves correctly; BLOCKED, not FAIL, if external Mojang service is unavailable and retry state is preserved.

### OW-04 Existing owners.json is not reseeded

- **Purpose:** Verify first-run defaults never overwrite an existing file.
- **Setup:** Stop server; write valid `{"owners":[]}` to `owners.json`.
- **Exact steps:** 1. Record file hash/content. 2. Start server. 3. Wait 30 seconds. 4. Stop server. 5. Compare content.
- **Expected result:** Owners list remains empty; `Shyha` is not re-added.
- **Files/configs affected:** Existing `owners.json`.
- **Pass/fail criteria:** PASS if the existing empty list remains empty; otherwise FAIL.

### OW-05 Owner permissions without operator status

- **Purpose:** Verify configured owners can use Ashmare without vanilla op.
- **Setup:** `OWNER` is in `owners.json` with correct UUID and removed from `ops.json`.
- **Exact steps:** 1. Restart or reconnect `OWNER`. 2. Run `/ashmare help`. 3. Run `/ashmare sound test`. 4. Confirm `/op`-only vanilla commands remain denied.
- **Expected result:** Ashmare commands work; unrelated vanilla operator privileges are not granted.
- **Files/configs affected:** `owners.json`; logs only.
- **Pass/fail criteria:** PASS if owner access is limited to Ashmare's owner authority; otherwise FAIL.

### OW-06 Owner bypasses deathban

- **Purpose:** Verify owners are never deathbanned.
- **Setup:** `OWNER` online, not op; delay `1`; duration `30m`.
- **Exact steps:** 1. Kill `OWNER`. 2. Wait 5 seconds. 3. Run `/ashmare ban list` as `OP2`. 4. Reconnect `OWNER` if needed.
- **Expected result:** No pending/active deathban or vanilla ban exists for `OWNER`.
- **Files/configs affected:** `deathban.json`, `banned-players.json`.
- **Pass/fail criteria:** PASS if owner remains unbanned and absent from both lists; otherwise FAIL.

### OW-07 Owner bypasses name randomization

- **Purpose:** Verify owners retain real names.
- **Setup:** `OWNER`, `P1`, and `VIEWER` online.
- **Exact steps:** 1. Run `/ashmare names randomize`. 2. Inspect command output, tab, chat, and nametags.
- **Expected result:** `P1` receives a fake name; `OWNER` is absent from assignments and remains visually real.
- **Files/configs affected:** `names.json`.
- **Pass/fail criteria:** PASS if no owner assignment exists and all owner displays remain real; otherwise FAIL.

### OW-08 Owner bypasses skin randomization

- **Purpose:** Verify owners retain real skins.
- **Setup:** Valid populated `skins.txt`; `OWNER`, `P1`, and `VIEWER` online.
- **Exact steps:** 1. Run `/ashmare skins randomize`. 2. Wait for completion. 3. Inspect `skins.json` and both players visually.
- **Expected result:** `P1` receives an assignment; `OWNER` does not and keeps the real skin.
- **Files/configs affected:** `skins.txt`, `skins.json`.
- **Pass/fail criteria:** PASS if owner has no assignment and no visual replacement; otherwise FAIL.

### OW-09 Owner add command

- **Purpose:** Verify authorized owner creation and immediate persistence.
- **Setup:** `OP4` or existing `OWNER`; `P1` has a resolvable profile.
- **Exact steps:** 1. Run `/ashmare owners add P1`. 2. Inspect `owners.json`. 3. De-op `P1`. 4. Reconnect and run `/ashmare help`.
- **Expected result:** UUID/name entry is saved immediately and `P1` gains owner access without op.
- **Files/configs affected:** `owners.json`.
- **Pass/fail criteria:** PASS if entry and privileges are both present; otherwise FAIL.

### OW-10 Owner remove command

- **Purpose:** Verify owner removal and permission revocation.
- **Setup:** `P1` is a configured owner but not op; another owner or `OP4` is available.
- **Exact steps:** 1. Run `/ashmare owners remove P1`. 2. Inspect file. 3. Have `P1` reconnect. 4. Run `/ashmare help`.
- **Expected result:** Entry is removed immediately and `P1` loses Ashmare command access.
- **Files/configs affected:** `owners.json`.
- **Pass/fail criteria:** PASS if the entry disappears and access is denied after reconnect; otherwise FAIL.

### OW-11 Owner list command

- **Purpose:** Verify owner list accuracy and unresolved-state display.
- **Setup:** One resolved owner and optionally one manually created unresolved entry.
- **Exact steps:** 1. Run `/ashmare owners list`. 2. Compare each displayed name/UUID with `owners.json`.
- **Expected result:** Count and identities match; null UUID entries display `UUID unresolved`.
- **Files/configs affected:** `owners.json` read only.
- **Pass/fail criteria:** PASS if command output exactly represents the file; otherwise FAIL.

### OW-12 Non-owners cannot modify owners

- **Purpose:** Protect owner administration from normal users and level-2 operators.
- **Setup:** `P1` non-op and `OP2`, neither configured as owner.
- **Exact steps:** 1. As each account, try `/ashmare owners list`, `add`, and `remove`. 2. Inspect `owners.json`.
- **Expected result:** Owner subcommands are unavailable or denied; file remains unchanged.
- **Files/configs affected:** `owners.json` read only.
- **Pass/fail criteria:** PASS if neither account can read or mutate through owner commands; otherwise FAIL.

### OW-13 Level-4 operators can modify owners

- **Purpose:** Verify emergency owner administration by vanilla server owners.
- **Setup:** `OP4` not listed in `owners.json`; `P2` resolvable.
- **Exact steps:** 1. Run list. 2. Add `P2`. 3. Remove `P2`. 4. Inspect file after each mutation.
- **Expected result:** All commands succeed and changes save immediately.
- **Files/configs affected:** `owners.json`.
- **Pass/fail criteria:** PASS if level 4 can list/add/remove without being configured owner; otherwise FAIL.

### OW-14 UUID lookup failure and startup retry

- **Purpose:** Verify a failed default-owner lookup preserves the username and retries later.
- **Setup:** Fresh `owners.json` generation; temporarily block Mojang profile API access.
- **Exact steps:** 1. Start server with API blocked. 2. Confirm `Shyha` remains with `uuid: null` and a retry warning is logged. 3. Stop server. 4. Restore API access. 5. Restart and wait up to 30 seconds. 6. Inspect file.
- **Expected result:** First startup keeps the unresolved username without deleting/reseeding the entry; later startup resolves and saves UUID/canonical username.
- **Files/configs affected:** `owners.json`, `logs/latest.log`.
- **Pass/fail criteria:** PASS if failure is non-destructive and the next startup resolves automatically; BLOCKED if Mojang remains externally unavailable.

## Permissions

### PM-01 Non-operator command denial

- **Purpose:** Verify ordinary players cannot access Ashmare commands.
- **Setup:** `P1` is non-op, non-owner.
- **Exact steps:** 1. Run `/ashmare`. 2. Try every visible subcommand by typing it directly.
- **Expected result:** Root command is unknown or denied and no config changes.
- **Files/configs affected:** All configs read only.
- **Pass/fail criteria:** PASS if no Ashmare action executes; otherwise FAIL.

### PM-02 Level-2 operator command access

- **Purpose:** Verify permission level 2 grants normal Ashmare administration.
- **Setup:** `OP2` is not an owner.
- **Exact steps:** 1. Run help, sound test, chat GUI, excluded list, ban list, and names/skins clear all. 2. Try owner list.
- **Expected result:** Normal commands work; `/ashmare owners ...` remains denied.
- **Files/configs affected:** Potentially all non-owner configs.
- **Pass/fail criteria:** PASS if access is split at the documented boundary; otherwise FAIL.

### PM-03 Owner command access

- **Purpose:** Verify configured owner access is UUID-backed and independent of op.
- **Setup:** Resolved owner entry; player not in `ops.json`.
- **Exact steps:** 1. Reconnect owner. 2. Execute one command from every subsystem. 3. Execute `/ashmare owners list`.
- **Expected result:** All Ashmare commands execute successfully.
- **Files/configs affected:** Depends on commands; `owners.json` read.
- **Pass/fail criteria:** PASS if all Ashmare branches are available to owner; otherwise FAIL.

### PM-04 Permission edge cases

- **Purpose:** Verify console, command blocks, stale owner names, and removed owners behave safely.
- **Setup:** Console access, command block enabled, renamed resolved owner if available, and removable test owner.
- **Exact steps:** 1. Run normal command from console. 2. Run owner command from console. 3. Try normal command from command block. 4. Test resolved owner after username change. 5. Remove owner and reconnect.
- **Expected result:** Console follows vanilla maximum permissions; command block follows its permission set; resolved owner matches UUID despite rename; removed owner loses access.
- **Files/configs affected:** `owners.json` may refresh last-known username.
- **Pass/fail criteria:** PASS if no username-only privilege leak exists after UUID resolution and revocation works; otherwise FAIL.

## Exclusion System

### EX-01 Exclude player

- **Purpose:** Verify exclusion stores UUID and last known username.
- **Setup:** `OP2` and `P1` online; `P1` not already excluded.
- **Exact steps:** 1. Run `/ashmare exclude P1`. 2. Inspect command response and `exclusions.json`.
- **Expected result:** One entry with `P1` UUID and current username is saved immediately.
- **Files/configs affected:** `exclusions.json`; possibly names, skins, and deathban state cleared visually.
- **Pass/fail criteria:** PASS if correct identity is persisted exactly once; otherwise FAIL.

### EX-02 Include player

- **Purpose:** Verify exclusion removal.
- **Setup:** `P1` excluded and online.
- **Exact steps:** 1. Run `/ashmare include P1`. 2. Inspect file. 3. Run `/ashmare excluded`.
- **Expected result:** Entry is removed and list no longer contains `P1`.
- **Files/configs affected:** `exclusions.json`.
- **Pass/fail criteria:** PASS if UUID is removed immediately; otherwise FAIL.

### EX-03 Excluded list

- **Purpose:** Verify list count and identities.
- **Setup:** Exclude `P1` and `P2`.
- **Exact steps:** 1. Run `/ashmare excluded`. 2. Compare output to JSON.
- **Expected result:** Both players appear with UUID and last-known username; count is `2`.
- **Files/configs affected:** `exclusions.json` read only.
- **Pass/fail criteria:** PASS if output and file match; otherwise FAIL.

### EX-04 Exclusion persistence

- **Purpose:** Verify exclusions survive restart.
- **Setup:** `P1` excluded.
- **Exact steps:** 1. Stop server normally. 2. Restart. 3. Run excluded list.
- **Expected result:** `P1` remains excluded with same UUID.
- **Files/configs affected:** `exclusions.json`.
- **Pass/fail criteria:** PASS if exclusion is retained and active; otherwise FAIL.

### EX-05 Exclusion interaction with names

- **Purpose:** Verify excluded players are ignored by name assignment and presentation.
- **Setup:** `P1` excluded; `P2` included; both online.
- **Exact steps:** 1. Randomize names. 2. Inspect `names.json`, tab, chat, and nametags.
- **Expected result:** `P2` is assigned; `P1` has no active fake name and displays real identity.
- **Files/configs affected:** `exclusions.json`, `names.json`.
- **Pass/fail criteria:** PASS if exclusion blocks both assignment and display; otherwise FAIL.

### EX-06 Exclusion interaction with skins

- **Purpose:** Verify excluded players are ignored by skin assignment and presentation.
- **Setup:** Valid `skins.txt`; `P1` excluded; `P2` included.
- **Exact steps:** 1. Randomize skins. 2. Inspect assignments and remote client views.
- **Expected result:** `P2` changes; `P1` has no assignment and keeps real skin.
- **Files/configs affected:** `exclusions.json`, `skins.txt`, `skins.json`.
- **Pass/fail criteria:** PASS if exclusion blocks assignment and visible replacement; otherwise FAIL.

### EX-07 Exclusion interaction with deathban

- **Purpose:** Verify exclusion prevents and clears deathbans.
- **Setup:** Delay `10`; `P1` included initially.
- **Exact steps:** 1. Kill `P1`. 2. Before 10 seconds, exclude `P1`. 3. Wait 15 seconds. 4. Check Ashmare and vanilla ban lists. 5. Repeat with `P1` already excluded before death.
- **Expected result:** Pending deathban is removed and no new deathban is created.
- **Files/configs affected:** `exclusions.json`, `deathban.json`, `banned-players.json`.
- **Pass/fail criteria:** PASS if both pre- and post-death exclusion paths prevent banning; otherwise FAIL.

## Deathban System

### DB-01 Deathban trigger

- **Purpose:** Verify a normal player death creates a pending deathban.
- **Setup:** `P1` included, non-owner; delay `10`; duration `30m`.
- **Exact steps:** 1. Kill `P1`. 2. Immediately run `/ashmare ban list`. 3. Inspect `deathban.json`.
- **Expected result:** One pending entry records UUID, username, death time, ban time, duration, and status.
- **Files/configs affected:** `deathban.json`.
- **Pass/fail criteria:** PASS if a correct pending entry appears immediately; otherwise FAIL.

### DB-02 Delay countdown

- **Purpose:** Verify ban timing honors the configured delay.
- **Setup:** Delay `10`; duration `30m`; `P1` online.
- **Exact steps:** 1. Record time and kill `P1`. 2. Confirm no ban at 5 seconds. 3. Check at 10-12 seconds.
- **Expected result:** Ban is not early and becomes active approximately at configured delay.
- **Files/configs affected:** `deathban.json`, `banned-players.json`.
- **Pass/fail criteria:** PASS if activation falls within two seconds after target and never before it; otherwise FAIL.

### DB-03 Custom delay values

- **Purpose:** Verify delay command validation and immediate persistence.
- **Setup:** `OP2`.
- **Exact steps:** 1. Set delay `0`, inspect JSON, and test immediate death. 2. Set `3` and test. 3. Try `-1`.
- **Expected result:** `0` and `3` save and apply; negative input is rejected by command parsing without file change.
- **Files/configs affected:** `deathban.json`.
- **Pass/fail criteria:** PASS if valid boundaries work and invalid input is rejected; otherwise FAIL.

### DB-04 Temporary ban durations

- **Purpose:** Verify supported temporary duration selection.
- **Setup:** `OP2`; clean deathban list.
- **Exact steps:** 1. Set each of `30m`, `12h`, and `1d`. 2. After each, kill a player with delay `0`. 3. Inspect expiration in Ashmare and vanilla ban files; unban between runs.
- **Expected result:** Expiration is approximately 30 minutes, 12 hours, or 1 day after ban time.
- **Files/configs affected:** `deathban.json`, `banned-players.json`.
- **Pass/fail criteria:** PASS if all three durations map to correct expirations; otherwise FAIL.

### DB-05 Permanent bans

- **Purpose:** Verify permanent duration has no expiration.
- **Setup:** Set delay `0`; set duration `permanent`.
- **Exact steps:** 1. Kill `P1`. 2. Inspect ban list command and both JSON files. 3. Attempt reconnect.
- **Expected result:** Entry reports active permanently; vanilla expiration is absent/null; reconnect is denied.
- **Files/configs affected:** `deathban.json`, `banned-players.json`.
- **Pass/fail criteria:** PASS if ban has no expiration and blocks reconnect; otherwise FAIL.

### DB-06 Ban expiration

- **Purpose:** Verify temporary bans are removed when their expiration is reached.
- **Setup:** Disposable backup; create a temporary active entry, then with server stopped adjust its Ashmare and vanilla expiration to 1-2 minutes ahead, preserving valid JSON.
- **Exact steps:** 1. Start server. 2. Confirm `P1` initially blocked. 3. Wait past expiration. 4. Attempt reconnect. 5. Inspect files.
- **Expected result:** Ashmare entry and Ashmare-owned vanilla ban are removed; player can join.
- **Files/configs affected:** `deathban.json`, `banned-players.json`.
- **Pass/fail criteria:** PASS if cleanup occurs automatically and reconnect succeeds; otherwise FAIL.

### DB-07 Unban command

- **Purpose:** Verify `/ashmare unban` clears only Ashmare's deathban.
- **Setup:** Active deathban for `P1`.
- **Exact steps:** 1. Run `/ashmare unban P1`. 2. Inspect files. 3. Reconnect. 4. Repeat using UUID. 5. Try an unknown player.
- **Expected result:** Username/UUID both remove matching Ashmare ban; reconnect succeeds; unknown target reports no ban.
- **Files/configs affected:** `deathban.json`, `banned-players.json`.
- **Pass/fail criteria:** PASS if removal is complete and unknown input is harmless; otherwise FAIL.

### DB-08 Deathban persistence

- **Purpose:** Verify pending and active bans survive restart.
- **Setup:** Delay `60`; kill `P1`; active ban for `P2`.
- **Exact steps:** 1. Stop after `P1` becomes pending. 2. Restart before target time. 3. Verify `P1` activates on schedule. 4. Restart again. 5. Verify `P2` remains blocked.
- **Expected result:** Both pending timing and active vanilla ban are restored.
- **Files/configs affected:** `deathban.json`, `banned-players.json`.
- **Pass/fail criteria:** PASS if neither state is lost or duplicated; otherwise FAIL.

### DB-09 Disconnect before ban fires

- **Purpose:** Verify delayed deathban does not depend on the player remaining online.
- **Setup:** Delay `10`; `P1` included and non-owner.
- **Exact steps:** 1. Kill `P1`. 2. Disconnect immediately. 3. Wait 15 seconds. 4. Attempt reconnect.
- **Expected result:** Ban activates while offline and reconnect is denied.
- **Files/configs affected:** `deathban.json`, `banned-players.json`.
- **Pass/fail criteria:** PASS if offline activation occurs; otherwise FAIL.

### DB-10 Reconnect after ban

- **Purpose:** Verify active vanilla enforcement and disconnect messaging.
- **Setup:** Active deathban for `P1`.
- **Exact steps:** 1. Attempt to join during ban. 2. Record disconnect screen. 3. Unban. 4. Join again.
- **Expected result:** Join is denied while active and succeeds after unban.
- **Files/configs affected:** `deathban.json`, `banned-players.json`, server log.
- **Pass/fail criteria:** PASS if enforcement and recovery both work; otherwise FAIL.

### DB-11 Excluded player death

- **Purpose:** Verify excluded deaths never schedule deathbans.
- **Setup:** `P1` excluded; delay `0`.
- **Exact steps:** 1. Kill `P1`. 2. Wait 3 seconds. 3. Inspect ban list and files.
- **Expected result:** No pending/active or vanilla ban entry is created.
- **Files/configs affected:** `exclusions.json`; deathban files read only.
- **Pass/fail criteria:** PASS if all ban state remains absent; otherwise FAIL.

### DB-12 Owner death

- **Purpose:** Verify owner immunity independently of exclusions.
- **Setup:** Resolved `OWNER`, not excluded; delay `0`.
- **Exact steps:** 1. Kill owner. 2. Wait 3 seconds. 3. Inspect both ban lists.
- **Expected result:** No owner ban entry exists.
- **Files/configs affected:** `owners.json`; deathban files read only.
- **Pass/fail criteria:** PASS if owner is never scheduled or banned; otherwise FAIL.

### DB-13 Multiple simultaneous deaths

- **Purpose:** Verify independent scheduling for several players.
- **Setup:** `P1`, `P2`, `P3` included; delay `3`; duration `30m`.
- **Exact steps:** 1. Kill all three in the same second. 2. Inspect pending list. 3. Wait 5 seconds. 4. Inspect active and vanilla lists.
- **Expected result:** Three distinct UUID entries schedule and activate without replacing one another.
- **Files/configs affected:** `deathban.json`, `banned-players.json`.
- **Pass/fail criteria:** PASS if all three bans activate exactly once; otherwise FAIL.

### DB-14 Hidden death messages do not disable deathban

- **Purpose:** Verify deathban is driven by death events, not message visibility.
- **Setup:** Set `/ashmare chat deaths false`; delay `2`; `P1` eligible.
- **Exact steps:** 1. Kill `P1`. 2. Confirm viewers see no death message. 3. Wait 4 seconds. 4. Check ban state.
- **Expected result:** Death message is hidden but deathban still activates.
- **Files/configs affected:** `chat.json`, `deathban.json`, `banned-players.json`.
- **Pass/fail criteria:** PASS if message is absent and ban is present; otherwise FAIL.

## Death Sound System

### DS-01 Default thunder sound

- **Purpose:** Verify every player death emits the default thunder sound.
- **Setup:** Fresh `sound.json`; normal `P1`, excluded `P2`, `OWNER`, and `VIEWER` within 10 blocks.
- **Exact steps:** 1. Confirm sound ID is `minecraft:entity.lightning_bolt.thunder` and radius `64`. 2. Kill `P1`, then `P2`, then `OWNER`, cleaning deathbans as needed. 3. Listen from `VIEWER`.
- **Expected result:** One thunder event plays at each death location for every player category.
- **Files/configs affected:** `sound.json` read only.
- **Pass/fail criteria:** PASS if in-range clients hear one thunder event for the death; otherwise FAIL.

### DS-02 Custom sound ID

- **Purpose:** Verify valid registered sounds apply immediately.
- **Setup:** `OP2`, `P1`, and `VIEWER` nearby.
- **Exact steps:** 1. Run `/ashmare sound set minecraft:block.note_block.bell`. 2. Inspect JSON. 3. Kill `P1`.
- **Expected result:** Command succeeds, config changes immediately, and bell replaces thunder.
- **Files/configs affected:** `sound.json`.
- **Pass/fail criteria:** PASS if saved and heard sound matches the selected ID; otherwise FAIL.

### DS-03 Invalid sound ID handling

- **Purpose:** Verify invalid IDs do not corrupt active settings.
- **Setup:** Known valid sound already configured.
- **Exact steps:** 1. Run `/ashmare sound set minecraft:not_a_real_sound`. 2. Try syntactically invalid input. 3. Inspect file. 4. Trigger test/death sound.
- **Expected result:** Unknown registered sound is rejected; malformed identifier fails parsing; previous valid setting remains active.
- **Files/configs affected:** `sound.json` should remain unchanged.
- **Pass/fail criteria:** PASS if invalid input cannot replace the valid setting; otherwise FAIL.

### DS-04 Radius configuration

- **Purpose:** Verify sound delivery uses configured distance.
- **Setup:** Players at 10, 40, and 70 blocks from test point.
- **Exact steps:** 1. Set radius `50`. 2. Run `/ashmare sound test` at test point. 3. Record listeners.
- **Expected result:** Players at 10 and 40 hear it; player at 70 does not; command reports the correct listener count.
- **Files/configs affected:** `sound.json`.
- **Pass/fail criteria:** PASS if delivery and count match Euclidean range; otherwise FAIL.

### DS-05 Radius edge cases

- **Purpose:** Verify zero, exact-boundary, large, and invalid radius behavior.
- **Setup:** One player at source, one exactly 64 blocks away, one slightly beyond.
- **Exact steps:** 1. Set radius `0` and test. 2. Set `64` and test boundary players. 3. Set `30000000`. 4. Try `-1` and `30000001`.
- **Expected result:** Radius zero reaches only a listener at the exact point; exact 64 is included; valid maximum saves; out-of-range inputs are rejected.
- **Files/configs affected:** `sound.json`.
- **Pass/fail criteria:** PASS if all boundaries match command constraints and distance comparison; otherwise FAIL.

### DS-06 Sound test command

- **Purpose:** Verify operators can preview sound without a death.
- **Setup:** `OP2` and two listeners inside configured radius.
- **Exact steps:** 1. Stand at known coordinates. 2. Run `/ashmare sound test`. 3. Verify source location and response count.
- **Expected result:** Configured sound plays at command source position and response states `2` listeners, adjusted for all players actually in range.
- **Files/configs affected:** `sound.json` read only.
- **Pass/fail criteria:** PASS if preview behavior and count are correct; otherwise FAIL.

## Chat Control System

### CC-01 Advancement toggle

- **Purpose:** Verify advancement announcements can be hidden and restored.
- **Setup:** `P1` and `VIEWER`; revoke `minecraft:story/root` from `P1`.
- **Exact steps:** 1. Set advancements false. 2. Grant the advancement. 3. Confirm no announcement. 4. Revoke it, set true, and grant again.
- **Expected result:** Announcement is hidden when off and visible when on.
- **Files/configs affected:** `chat.json`.
- **Pass/fail criteria:** PASS if both states apply immediately; otherwise FAIL.

### CC-02 Join message toggle

- **Purpose:** Verify join messages can be hidden and restored.
- **Setup:** `VIEWER` online; `P1` disconnected.
- **Exact steps:** 1. Set join/leave false. 2. Join `P1`. 3. Set true. 4. Reconnect `P1`.
- **Expected result:** First join has no broadcast; second join has one.
- **Files/configs affected:** `chat.json`.
- **Pass/fail criteria:** PASS if join broadcasts follow the toggle; otherwise FAIL.

### CC-03 Leave message toggle

- **Purpose:** Verify leave messages can be hidden and restored.
- **Setup:** `VIEWER` and `P1` online.
- **Exact steps:** 1. Set join/leave false. 2. Disconnect `P1`. 3. Rejoin, set true, then disconnect again.
- **Expected result:** First leave is hidden; second leave is visible.
- **Files/configs affected:** `chat.json`.
- **Pass/fail criteria:** PASS if leave broadcasts follow the shared toggle; otherwise FAIL.

### CC-04 Player chat toggle

- **Purpose:** Verify player chat delivery can be disabled server-wide.
- **Setup:** `P1` and `VIEWER` online.
- **Exact steps:** 1. Set playerchat false. 2. Have `P1` send a unique message. 3. Set true. 4. Send another.
- **Expected result:** Viewer receives only the message sent while enabled.
- **Files/configs affected:** `chat.json`.
- **Pass/fail criteria:** PASS if chat is suppressed/restored without reconnect; otherwise FAIL.

### CC-05 Command output toggle

- **Purpose:** Verify command feedback broadcast to other players can be controlled.
- **Setup:** `OP2` and another operator/viewer able to receive vanilla command broadcasts.
- **Exact steps:** 1. Set commandoutput false. 2. Execute `/time set day`. 3. Set true. 4. Execute `/time set night`.
- **Expected result:** Other eligible players do not receive the first command broadcast and do receive the second; executor feedback remains usable.
- **Files/configs affected:** `chat.json`.
- **Pass/fail criteria:** PASS if only cross-player command output is toggled; otherwise FAIL.

### CC-06 Death message toggle

- **Purpose:** Verify death messages can be hidden while death processing continues.
- **Setup:** Delay long enough to avoid immediate disconnect; `P1` and `VIEWER`.
- **Exact steps:** 1. Set deaths false. 2. Kill `P1`. 3. Set true. 4. Kill `P1` again after cleanup.
- **Expected result:** First death has no broadcast; second has one; death sound occurs both times.
- **Files/configs affected:** `chat.json`, possibly `deathban.json`.
- **Pass/fail criteria:** PASS if only display changes and death systems still execute; otherwise FAIL.

### CC-07 Instant updates

- **Purpose:** Verify toggles apply without server restart or reconnect.
- **Setup:** At least two online players.
- **Exact steps:** 1. Alternate each toggle false/true. 2. Trigger its event immediately after each command.
- **Expected result:** Every event reflects the latest value on the next occurrence.
- **Files/configs affected:** `chat.json`.
- **Pass/fail criteria:** PASS if no stale state is observed for any toggle; otherwise FAIL.

### CC-08 Persistence after restart

- **Purpose:** Verify all chat toggles persist.
- **Setup:** Set a mixed pattern, for example false/true/false/true/false.
- **Exact steps:** 1. Record `chat.json`. 2. Restart server. 3. Trigger one event for each toggle. 4. Reinspect file.
- **Expected result:** Mixed values and behavior are unchanged.
- **Files/configs affected:** `chat.json`.
- **Pass/fail criteria:** PASS if all five settings persist and remain active; otherwise FAIL.

## Chat GUI

### GUI-01 GUI opening

- **Purpose:** Verify `/ashmare chat` opens the chest interface.
- **Setup:** `OP2` in normal gameplay.
- **Exact steps:** 1. Run `/ashmare chat`. 2. Inspect title, size, and controls.
- **Expected result:** A 3-row chest titled `Ashmare Chat Controls` opens with five toggle items.
- **Files/configs affected:** `chat.json` read only until click.
- **Pass/fail criteria:** PASS if correct menu opens without item duplication or error; otherwise FAIL.

### GUI-02 GUI permissions

- **Purpose:** Verify GUI obeys root Ashmare permissions.
- **Setup:** `P1` non-owner/non-op, `OP2`, and non-op `OWNER`.
- **Exact steps:** 1. Each account runs `/ashmare chat`. 2. Record result.
- **Expected result:** `P1` denied; `OP2` and `OWNER` can open it.
- **Files/configs affected:** None unless toggled.
- **Pass/fail criteria:** PASS if access matches documented permission model; otherwise FAIL.

### GUI-03 Toggle interaction

- **Purpose:** Verify every clickable item changes its matching setting only.
- **Setup:** GUI open; record current JSON.
- **Exact steps:** 1. Click each of the five control slots once. 2. Compare JSON after each click. 3. Trigger representative events.
- **Expected result:** Exactly one corresponding boolean flips per click and behavior changes immediately.
- **Files/configs affected:** `chat.json`.
- **Pass/fail criteria:** PASS if all five controls map correctly with no inventory item movement; otherwise FAIL.

### GUI-04 Item state updates

- **Purpose:** Verify visual ON/OFF state refreshes after clicks.
- **Setup:** GUI open.
- **Exact steps:** 1. Click an ON lime dye. 2. Observe item. 3. Click again.
- **Expected result:** Item becomes red dye labeled `OFF` without reopening, then lime dye labeled `ON`; glint follows enabled state.
- **Files/configs affected:** `chat.json`.
- **Pass/fail criteria:** PASS if visual state updates after every click; otherwise FAIL.

### GUI-05 GUI persistence

- **Purpose:** Verify GUI changes persist through close, reopen, and restart.
- **Setup:** GUI access.
- **Exact steps:** 1. Turn two settings off. 2. Close/reopen GUI. 3. Restart server and reopen.
- **Expected result:** Both items remain OFF at both checkpoints and JSON retains values.
- **Files/configs affected:** `chat.json`.
- **Pass/fail criteria:** PASS if visual and stored state remain synchronized; otherwise FAIL.

### GUI-06 Death message warning label

- **Purpose:** Verify required explanatory text is visible.
- **Setup:** Open GUI.
- **Exact steps:** 1. Hover the `Death Messages` item while ON. 2. Toggle OFF and hover again.
- **Expected result:** Lore always includes `Required for deathban system.` and item clearly shows current state.
- **Files/configs affected:** None unless toggled.
- **Pass/fail criteria:** PASS if exact warning appears in both states; otherwise FAIL.

## Name Randomizer

### NR-01 Randomize all eligible players

- **Purpose:** Verify online eligible players receive persisted assignments.
- **Setup:** `P1`, `P2`, and `P3` online, included, non-owners.
- **Exact steps:** 1. Clear names. 2. Run `/ashmare names randomize`. 3. Compare command output and JSON.
- **Expected result:** Three UUID-keyed assignments with real and fake names are saved.
- **Files/configs affected:** `names.json`.
- **Pass/fail criteria:** PASS if every eligible online player gets exactly one assignment; otherwise FAIL.

### NR-02 Clear one player

- **Purpose:** Verify targeted clear restores a real name.
- **Setup:** `P1` and `P2` assigned.
- **Exact steps:** 1. Run `/ashmare names clear P1`. 2. Inspect JSON, tab, nametag, and chat. 3. Verify `P2`.
- **Expected result:** Only `P1` assignment is removed and visual identity refreshes to real name.
- **Files/configs affected:** `names.json`.
- **Pass/fail criteria:** PASS if target clears immediately without affecting others; otherwise FAIL.

### NR-03 Clear all

- **Purpose:** Verify global clear restores every assigned name.
- **Setup:** Multiple assigned players online.
- **Exact steps:** 1. Run `/ashmare names clear all`. 2. Inspect JSON and all visible name surfaces.
- **Expected result:** Assignments array is empty and real names return.
- **Files/configs affected:** `names.json`.
- **Pass/fail criteria:** PASS if all assignments and presentations are cleared; otherwise FAIL.

### NR-04 Name persistence after restart

- **Purpose:** Verify assignments do not reroll on reconnect/restart.
- **Setup:** Record assignments for `P1` and `P2`.
- **Exact steps:** 1. Restart server. 2. Reconnect players. 3. Compare JSON and visual names.
- **Expected result:** Exact fake strings remain associated with the same UUIDs.
- **Files/configs affected:** `names.json`.
- **Pass/fail criteria:** PASS if no value changes without a command; otherwise FAIL.

### NR-05 Duplicate prevention

- **Purpose:** Verify active fake names are unique case-insensitively.
- **Setup:** At least 20 eligible players or repeated preserved assignments.
- **Exact steps:** 1. Randomize. 2. Export all fake names. 3. Lowercase and compare count to distinct count. 4. Randomize again.
- **Expected result:** No duplicate active fake names in either run.
- **Files/configs affected:** `names.json`.
- **Pass/fail criteria:** PASS if every assignment is unique ignoring case; otherwise FAIL.

### NR-06 Excluded player behavior

- **Purpose:** Verify exclusion bypass.
- **Setup:** `P1` excluded, `P2` included.
- **Exact steps:** 1. Randomize names. 2. Inspect JSON and visual names.
- **Expected result:** Only `P2` receives a fake name.
- **Files/configs affected:** `exclusions.json`, `names.json`.
- **Pass/fail criteria:** PASS if `P1` remains real and unassigned; otherwise FAIL.

### NR-07 Owner behavior

- **Purpose:** Verify owner bypass.
- **Setup:** `OWNER` and `P1` online.
- **Exact steps:** 1. Randomize names. 2. Inspect JSON and displays.
- **Expected result:** `P1` is randomized; `OWNER` remains real and unassigned.
- **Files/configs affected:** `owners.json`, `names.json`.
- **Pass/fail criteria:** PASS if owner bypass is complete; otherwise FAIL.

### NR-08 Name generation quality

- **Purpose:** Verify generated strings meet allowed length and composition.
- **Setup:** Generate at least 100 assignments across repeated runs and save samples.
- **Exact steps:** 1. Collect names. 2. Check each is 6-14 characters. 3. Check characters are letters, digits, or at most one underscore. 4. Confirm recognizable configured fragments occur.
- **Expected result:** Every name is valid, readable, and generated from 2-3 distinct fragments plus optional modifications.
- **Files/configs affected:** `names.json`.
- **Pass/fail criteria:** PASS if all samples meet constraints; otherwise FAIL.

### NR-09 Underscore generation

- **Purpose:** Verify optional single underscore appears between fragments.
- **Setup:** Collect up to 200 generated names.
- **Exact steps:** 1. Repeatedly randomize and record names. 2. Stop when an underscore appears or sample reaches 200. 3. Inspect placement/count.
- **Expected result:** At least one sample contains exactly one internal underscore; no name contains multiple or edge underscores.
- **Files/configs affected:** `names.json`.
- **Pass/fail criteria:** PASS if valid underscore variation appears; FAIL if malformed; BLOCKED if chance produces none after 200 and no deterministic test hook exists.

### NR-10 Number substitution generation

- **Purpose:** Verify `o→0`, `a→4`, `e→3`, or `i→1` substitution variation.
- **Setup:** Collect up to 300 generated names.
- **Exact steps:** 1. Record names across randomizations. 2. Identify digits embedded inside fragment text rather than only at the suffix.
- **Expected result:** At least one plausible embedded substitution appears and name remains 6-14 characters.
- **Files/configs affected:** `names.json`.
- **Pass/fail criteria:** PASS if embedded substitution appears; BLOCKED after 300 chance-based samples without malformed output.

### NR-11 Number suffix generation

- **Purpose:** Verify optional 1-4 digit suffixes.
- **Setup:** Collect up to 200 generated names.
- **Exact steps:** 1. Record generated names. 2. Find names ending in digits. 3. Count trailing digits.
- **Expected result:** At least one has a 1-4 digit suffix; none has more than four suffix digits.
- **Files/configs affected:** `names.json`.
- **Pass/fail criteria:** PASS if valid suffix variation appears; FAIL for invalid length; BLOCKED if chance produces none after sample limit.

### NR-12 Casing variation generation

- **Purpose:** Verify lowercase, uppercase, sentence case, and mixed `MC`/`XD`/`TV` styles.
- **Setup:** Collect at least 200 generated names.
- **Exact steps:** 1. Categorize each name. 2. Record examples for all four styles.
- **Expected result:** Samples include all lowercase, all uppercase, initial-cap, and mixed suffix styles.
- **Files/configs affected:** `names.json`.
- **Pass/fail criteria:** PASS if all styles appear; FAIL for invalid casing output; BLOCKED if chance omits a style after 200 samples.

## Fake Name Application

### FN-01 Tab list replacement

- **Purpose:** Verify fake profiles appear in player list.
- **Setup:** `P1` assigned; `VIEWER` online.
- **Exact steps:** 1. Open tab as `VIEWER`. 2. Compare displayed value to `names.json`. 3. Clear `P1`.
- **Expected result:** Fake name appears before clear and real name after clear.
- **Files/configs affected:** `names.json`.
- **Pass/fail criteria:** PASS if tab updates immediately in both directions; otherwise FAIL.

### FN-02 Chat replacement

- **Purpose:** Verify normal chat does not expose assigned real username.
- **Setup:** Player chat enabled; `P1` assigned; `VIEWER` online.
- **Exact steps:** 1. Have `P1` send a unique chat message. 2. Inspect sender label on all clients.
- **Expected result:** Sender label uses fake name; message body is unchanged.
- **Files/configs affected:** `names.json`, `chat.json` read only.
- **Pass/fail criteria:** PASS if no real sender name appears in normal chat UI; otherwise FAIL.

### FN-03 Join message replacement

- **Purpose:** Verify reconnect announcement uses fake name without rename leakage.
- **Setup:** Join/leave enabled; persistent assignment for `P1`; `VIEWER` online.
- **Exact steps:** 1. Disconnect and reconnect `P1`. 2. Inspect join announcement.
- **Expected result:** Announcement uses fake name and does not show `real joined as fake` or the real username.
- **Files/configs affected:** `names.json`, `chat.json` read only.
- **Pass/fail criteria:** PASS if only fake identity is visible; otherwise FAIL.

### FN-04 Leave message replacement

- **Purpose:** Verify leave announcement uses fake name.
- **Setup:** Join/leave enabled; `P1` assigned.
- **Exact steps:** 1. Disconnect `P1`. 2. Inspect all clients.
- **Expected result:** Leave message displays fake name only.
- **Files/configs affected:** `names.json`, `chat.json` read only.
- **Pass/fail criteria:** PASS if real username is absent; otherwise FAIL.

### FN-05 Death message replacement

- **Purpose:** Verify enabled death messages use fake identity.
- **Setup:** Death messages enabled; long deathban delay; `P1` assigned.
- **Exact steps:** 1. Kill `P1` by environment. 2. Kill again using another player. 3. Inspect both message forms.
- **Expected result:** Victim name is fake in both; attacker fake name is used if attacker is assigned.
- **Files/configs affected:** `names.json`, `chat.json`, `deathban.json`.
- **Pass/fail criteria:** PASS if no assigned player's real username appears; otherwise FAIL.

### FN-06 Nametag replacement

- **Purpose:** Verify the above-head label changes for remote viewers.
- **Setup:** `P1` and `VIEWER` in render distance.
- **Exact steps:** 1. Assign names. 2. Observe `P1` from `VIEWER`. 3. Clear `P1`. 4. Observe again.
- **Expected result:** Nametag changes to fake and back without server restart; brief entity refresh is acceptable.
- **Files/configs affected:** `names.json`.
- **Pass/fail criteria:** PASS if remote nametag matches current assignment; otherwise FAIL.

### FN-07 Real username leakage audit

- **Purpose:** Search normal gameplay UI for unintended real-name exposure.
- **Setup:** Several assigned players; chat/join/death messages enabled.
- **Exact steps:** 1. Exercise tab, chat, join, leave, death, nametags, sleep messages, advancements, combat, and common vanilla broadcasts. 2. Search screenshots/client logs for real usernames.
- **Expected result:** Listed Ashmare-covered surfaces use fake names. Command suggestions, administrative output, scoreboard entry keys, server logs, and direct operator command results may retain real identities by design.
- **Files/configs affected:** `names.json`; client/server logs as evidence.
- **Pass/fail criteria:** PASS if no leak occurs on covered normal UI; document any out-of-scope administrative exposure separately.

### FN-08 Real username command compatibility

- **Purpose:** Verify server identity and command targeting remain real.
- **Setup:** `P1` assigned; `OP2`.
- **Exact steps:** 1. Run `/tp P1 <OP2>`, `/kill P1`, `/ashmare names clear P1`, and `/ashmare skins clear P1`. 2. Try fake name as target.
- **Expected result:** Real username continues to resolve; fake name is presentation only and should not replace vanilla command identity.
- **Files/configs affected:** `names.json`, possibly `skins.json` and `deathban.json`.
- **Pass/fail criteria:** PASS if real-name targeting works reliably and fake name does not hijack identity; otherwise FAIL.

## Skin Randomizer

### SR-01 skins.txt loading

- **Purpose:** Verify one trimmed username per line is loaded.
- **Setup:** Put three valid usernames in `skins.txt`, including blank surrounding lines and spaces.
- **Exact steps:** 1. Restart or run skin randomize. 2. Observe resolution summary. 3. Inspect cache entries.
- **Expected result:** Blank lines are ignored, valid names are trimmed, and each unique source is processed.
- **Files/configs affected:** `skins.txt`, `skins.json`.
- **Pass/fail criteria:** PASS if normalized valid entries resolve once each; otherwise FAIL.

### SR-02 Empty skin pool handling

- **Purpose:** Verify an empty source list fails gracefully.
- **Setup:** Empty `skins.txt`; at least one eligible player online.
- **Exact steps:** 1. Run `/ashmare skins randomize`. 2. Inspect response, log, and assignments.
- **Expected result:** Command reports no valid skin usernames; server stays healthy; existing assignments are not unintentionally erased.
- **Files/configs affected:** `skins.txt`; `skins.json` should not gain assignments.
- **Pass/fail criteria:** PASS if failure is clear and non-destructive; otherwise FAIL.

### SR-03 Invalid username handling

- **Purpose:** Verify malformed source lines are skipped.
- **Setup:** `skins.txt` contains `ab`, `name-with-dash`, a 17-character name, and one valid source.
- **Exact steps:** 1. Run randomize. 2. Inspect player feedback and logs. 3. Inspect cache.
- **Expected result:** Invalid local formats are reported/skipped; valid source still resolves and can be assigned.
- **Files/configs affected:** `skins.txt`, `skins.json`, `latest.log`.
- **Pass/fail criteria:** PASS if bad lines do not abort valid work; otherwise FAIL.

### SR-04 Mojang lookup success

- **Purpose:** Verify real signed texture data is fetched and cached.
- **Setup:** Online server; `skins.txt` contains a known valid premium account.
- **Exact steps:** 1. Clear cache in a backup copy of `skins.json`. 2. Run randomize. 3. Inspect cache record.
- **Expected result:** Cache contains source UUID, canonical username, signed texture value/signature, and fetch timestamp.
- **Files/configs affected:** `skins.txt`, `skins.json`.
- **Pass/fail criteria:** PASS if complete signed property is stored and applied; BLOCKED if Mojang is unavailable.

### SR-05 Mojang lookup failure

- **Purpose:** Verify unavailable or nonexistent profiles do not crash the server.
- **Setup:** Add a valid-format nonexistent username; optionally block Mojang endpoints temporarily.
- **Exact steps:** 1. Run randomize. 2. Wait past request timeout. 3. Inspect feedback/log. 4. Run another command to confirm server responsiveness.
- **Expected result:** Failure is reported per source; other valid/cached sources continue; server thread remains responsive.
- **Files/configs affected:** `skins.txt`, `skins.json`, `latest.log`.
- **Pass/fail criteria:** PASS if failure is bounded, reported, and non-fatal; otherwise FAIL.

### SR-06 Cache behavior

- **Purpose:** Verify fresh cache avoids repeated API dependency.
- **Setup:** Successfully cached valid skin less than 24 hours old.
- **Exact steps:** 1. Record cached timestamp/value. 2. Block Mojang endpoints. 3. Run randomize again. 4. Compare cache.
- **Expected result:** Randomization succeeds from fresh cache and timestamp/value remain unchanged.
- **Files/configs affected:** `skins.json`.
- **Pass/fail criteria:** PASS if fresh cache works offline; otherwise FAIL.

### SR-07 Randomize all eligible players

- **Purpose:** Verify assignment across all online eligible players.
- **Setup:** Three valid source skins; `P1`, `P2`, `P3` online and eligible.
- **Exact steps:** 1. Clear skins. 2. Run randomize. 3. Wait for completion. 4. Compare response and assignments.
- **Expected result:** Each player receives one persisted assignment; sources may repeat only when players outnumber resolved skins.
- **Files/configs affected:** `skins.txt`, `skins.json`.
- **Pass/fail criteria:** PASS if all eligible online UUIDs are assigned with valid textures; otherwise FAIL.

### SR-08 Clear one player

- **Purpose:** Verify targeted skin clearing restores the real texture.
- **Setup:** `P1` and `P2` assigned; `VIEWER` online.
- **Exact steps:** 1. Run `/ashmare skins clear P1`. 2. Inspect JSON and remote views. 3. Verify `P2`.
- **Expected result:** Only `P1` assignment is removed; remote viewers see real skin after refresh.
- **Files/configs affected:** `skins.json`.
- **Pass/fail criteria:** PASS if target clears without affecting other assignments; otherwise FAIL.

### SR-09 Clear all

- **Purpose:** Verify global skin clearing.
- **Setup:** Multiple assigned players online.
- **Exact steps:** 1. Run `/ashmare skins clear all`. 2. Inspect assignments and remote views.
- **Expected result:** Assignment list becomes empty and all players return to real skins; cache may remain.
- **Files/configs affected:** `skins.json`.
- **Pass/fail criteria:** PASS if assignments clear while reusable cache remains valid; otherwise FAIL.

### SR-10 Skin persistence after restart

- **Purpose:** Verify assignment and cached texture persistence.
- **Setup:** Record current `P1` assignment.
- **Exact steps:** 1. Restart server. 2. Reconnect `P1` and `VIEWER`. 3. Compare file and visual skin.
- **Expected result:** Same source UUID/texture remains assigned and visible.
- **Files/configs affected:** `skins.json`.
- **Pass/fail criteria:** PASS if assignment does not reroll or disappear; otherwise FAIL.

### SR-11 Excluded player behavior

- **Purpose:** Verify excluded players keep real skins.
- **Setup:** `P1` excluded, `P2` included, valid source pool.
- **Exact steps:** 1. Randomize. 2. Inspect JSON and remote views.
- **Expected result:** `P2` changes; `P1` is absent from assignments and remains real.
- **Files/configs affected:** `exclusions.json`, `skins.txt`, `skins.json`.
- **Pass/fail criteria:** PASS if excluded identity is untouched; otherwise FAIL.

### SR-12 Owner behavior

- **Purpose:** Verify owners keep real skins and stale assignments are cleared.
- **Setup:** Add an assignment to `P1`, then add `P1` as owner; valid source pool.
- **Exact steps:** 1. Confirm owner addition clears stale assignment. 2. Randomize skins again. 3. Inspect file and views.
- **Expected result:** Owner remains unassigned and real both immediately and in later runs.
- **Files/configs affected:** `owners.json`, `skins.json`.
- **Pass/fail criteria:** PASS if becoming owner and future randomization both enforce bypass; otherwise FAIL.

## Fake Skin Application

### FS-01 Skin replacement

- **Purpose:** Verify assigned signed texture is visible to remote clients.
- **Setup:** Known source skin, assigned `P1`, `VIEWER` nearby.
- **Exact steps:** 1. Record `P1` original skin. 2. Randomize. 3. Compare `VIEWER`'s view and tab head with source account.
- **Expected result:** Remote body and player-list skin match the assigned source.
- **Files/configs affected:** `skins.json`.
- **Pass/fail criteria:** PASS if remote visible skin changes to source texture; otherwise FAIL.

### FS-02 Skin clearing

- **Purpose:** Verify clearing restores original profile texture.
- **Setup:** `P1` visibly assigned; `VIEWER` online.
- **Exact steps:** 1. Clear `P1`. 2. Observe remote body and tab head. 3. Reconnect `P1` if self-view remains stale.
- **Expected result:** Remote clients immediately return to real skin; player's own local self-view may require reconnect.
- **Files/configs affected:** `skins.json`.
- **Pass/fail criteria:** PASS if remote views restore immediately and self-view restores by reconnect at latest; otherwise FAIL.

### FS-03 Reconnect behavior

- **Purpose:** Verify reconnect completes refresh for the assigned player's own client.
- **Setup:** `P1` assigned and able to view own skin in inventory/third person.
- **Exact steps:** 1. Check self-view immediately after assignment. 2. Disconnect/reconnect. 3. Check again. 4. Clear and repeat.
- **Expected result:** After reconnect, own client shows assigned skin; after clear plus reconnect, own real skin returns.
- **Files/configs affected:** `skins.json`.
- **Pass/fail criteria:** PASS if reconnect produces authoritative current state; otherwise FAIL.

### FS-04 Multiple players

- **Purpose:** Verify textures are associated with correct UUIDs under simultaneous refresh.
- **Setup:** `P1`, `P2`, `P3`, `VIEWER`; at least three distinctive source skins.
- **Exact steps:** 1. Randomize. 2. Record assignment map. 3. Have players move/relog. 4. Compare each visual skin to map.
- **Expected result:** No skin swaps between player UUIDs and no invisible/duplicate entity artifacts.
- **Files/configs affected:** `skins.json`.
- **Pass/fail criteria:** PASS if every visible texture matches its persisted assignment; otherwise FAIL.

### FS-05 Persistence after restart

- **Purpose:** Verify packet rewriting reapplies persisted skins at login.
- **Setup:** Multiple saved assignments.
- **Exact steps:** 1. Stop and restart server. 2. Join `VIEWER` first, then assigned players. 3. Inspect bodies and tab heads.
- **Expected result:** Saved assigned skins appear without rerunning randomization.
- **Files/configs affected:** `skins.json`.
- **Pass/fail criteria:** PASS if all assignments reappear correctly; otherwise FAIL.

## Stress Testing

### ST-01 20+ players online

- **Purpose:** Verify baseline stability and presentation at intended concurrency.
- **Setup:** At least 20 clients/bots, including owners, exclusions, and normal users.
- **Exact steps:** 1. Join all clients over 2 minutes. 2. Run name and skin randomization. 3. Exercise chat and tab for 15 minutes. 4. Review TPS, memory, disconnects, and logs.
- **Expected result:** No crash, watchdog, sustained severe TPS loss, profile corruption, or widespread disconnect.
- **Files/configs affected:** `names.json`, `skins.json`, logs.
- **Pass/fail criteria:** PASS if server remains stable and all identities remain correctly mapped; otherwise FAIL.

### ST-02 Multiple deaths simultaneously

- **Purpose:** Stress death event, sound, persistence, and scheduler concurrency.
- **Setup:** 20 eligible players; delay `3`; duration `30m`; listeners spread around deaths.
- **Exact steps:** 1. Kill all players within one tick/second using controlled commands. 2. Monitor sounds and messages. 3. Inspect pending/active counts. 4. Restart.
- **Expected result:** One sound and one independent deathban per death; no lost/duplicate entries; restart restores all active bans.
- **Files/configs affected:** `deathban.json`, `banned-players.json`, logs.
- **Pass/fail criteria:** PASS if counts equal eligible deaths and server remains responsive; otherwise FAIL.

### ST-03 Multiple randomizations

- **Purpose:** Stress repeated replacement and refresh behavior.
- **Setup:** 20 eligible players and at least 10 valid skin sources.
- **Exact steps:** 1. Run names randomize 25 times with short pauses. 2. Run skins randomize 10 times sequentially. 3. Attempt a second skin randomize while one is active. 4. Inspect files/logs.
- **Expected result:** Names remain unique; latest assignments win; concurrent skin run is rejected cleanly; no corrupt JSON or stuck running state.
- **Files/configs affected:** `names.json`, `skins.json`, logs.
- **Pass/fail criteria:** PASS if final state is valid and server remains stable; otherwise FAIL.

### ST-04 Repeated GUI usage

- **Purpose:** Stress menu lifecycle and config writes.
- **Setup:** Multiple authorized players.
- **Exact steps:** 1. Open/close GUI 100 times across clients. 2. Perform 500 total toggle clicks. 3. Attempt shift-clicks and inventory clicks. 4. Restart and inspect.
- **Expected result:** No item theft/duplication, ghost stacks, menu errors, lost writes, or client desync; final JSON matches visible states.
- **Files/configs affected:** `chat.json`, logs.
- **Pass/fail criteria:** PASS if menu and persisted state stay consistent; otherwise FAIL.

### ST-05 Repeated server restarts

- **Purpose:** Detect startup races, duplicate scheduling, and persistence degradation.
- **Setup:** Mixed configs, pending/active deathbans, names, skins, owners, and exclusions.
- **Exact steps:** 1. Perform 20 normal stop/start cycles. 2. On selected cycles join players and trigger one event. 3. Diff configs before/after. 4. Review all logs.
- **Expected result:** No unintended resets, duplicate entries, growing corruption, startup exception, or duplicate scheduled action.
- **Files/configs affected:** All Ashmare configs, `banned-players.json`, logs.
- **Pass/fail criteria:** PASS if state remains logically identical except expected timestamps/username refreshes; otherwise FAIL.

## Full Pass/Fail Matrix

Fill `Result`, `Tester/date`, and `Evidence/issue` after execution.

| ID | Test | Priority | Result | Tester/date | Evidence/issue |
|---|---|---:|---|---|---|
| PS-01 | Fresh server startup | P0 | NOT RUN | | |
| PS-02 | Empty repository build verification | P0 | PASS | 2026-06-09 | `gradlew.bat clean build`, build successful |
| PS-03 | Mod loading verification | P0 | NOT RUN | | |
| PS-04 | Config folder creation | P0 | NOT RUN | | |
| PS-05 | Config file auto-generation | P0 | NOT RUN | | |
| PS-06 | Missing config recovery | P1 | NOT RUN | | |
| PS-07 | Server restart persistence | P0 | NOT RUN | | |
| OW-01 | owners.json first-run generation | P0 | NOT RUN | | |
| OW-02 | Default Shyha owner | P0 | NOT RUN | | |
| OW-03 | Shyha UUID resolution | P0 | NOT RUN | | |
| OW-04 | Existing owners.json is not reseeded | P0 | NOT RUN | | |
| OW-05 | Owner permissions without operator status | P0 | NOT RUN | | |
| OW-06 | Owner bypasses deathban | P0 | NOT RUN | | |
| OW-07 | Owner bypasses name randomization | P0 | NOT RUN | | |
| OW-08 | Owner bypasses skin randomization | P0 | NOT RUN | | |
| OW-09 | Owner add command | P1 | NOT RUN | | |
| OW-10 | Owner remove command | P1 | NOT RUN | | |
| OW-11 | Owner list command | P1 | NOT RUN | | |
| OW-12 | Non-owners cannot modify owners | P0 | NOT RUN | | |
| OW-13 | Level-4 operators can modify owners | P0 | NOT RUN | | |
| OW-14 | UUID lookup failure and startup retry | P0 | NOT RUN | | |
| PM-01 | Non-operator command denial | P0 | NOT RUN | | |
| PM-02 | Level-2 operator command access | P0 | NOT RUN | | |
| PM-03 | Owner command access | P0 | NOT RUN | | |
| PM-04 | Permission edge cases | P1 | NOT RUN | | |
| EX-01 | Exclude player | P0 | NOT RUN | | |
| EX-02 | Include player | P0 | NOT RUN | | |
| EX-03 | Excluded list | P1 | NOT RUN | | |
| EX-04 | Exclusion persistence | P0 | NOT RUN | | |
| EX-05 | Exclusion interaction with names | P0 | NOT RUN | | |
| EX-06 | Exclusion interaction with skins | P0 | NOT RUN | | |
| EX-07 | Exclusion interaction with deathban | P0 | NOT RUN | | |
| DB-01 | Deathban trigger | P0 | NOT RUN | | |
| DB-02 | Delay countdown | P0 | NOT RUN | | |
| DB-03 | Custom delay values | P1 | NOT RUN | | |
| DB-04 | Temporary ban durations | P0 | NOT RUN | | |
| DB-05 | Permanent bans | P0 | NOT RUN | | |
| DB-06 | Ban expiration | P0 | NOT RUN | | |
| DB-07 | Unban command | P0 | NOT RUN | | |
| DB-08 | Deathban persistence | P0 | NOT RUN | | |
| DB-09 | Disconnect before ban fires | P0 | NOT RUN | | |
| DB-10 | Reconnect after ban | P0 | NOT RUN | | |
| DB-11 | Excluded player death | P0 | NOT RUN | | |
| DB-12 | Owner death | P0 | NOT RUN | | |
| DB-13 | Multiple simultaneous deaths | P1 | NOT RUN | | |
| DB-14 | Hidden death messages do not disable deathban | P0 | NOT RUN | | |
| DS-01 | Default thunder sound | P0 | NOT RUN | | |
| DS-02 | Custom sound ID | P1 | NOT RUN | | |
| DS-03 | Invalid sound ID handling | P1 | NOT RUN | | |
| DS-04 | Radius configuration | P1 | NOT RUN | | |
| DS-05 | Radius edge cases | P2 | NOT RUN | | |
| DS-06 | Sound test command | P1 | NOT RUN | | |
| CC-01 | Advancement toggle | P1 | NOT RUN | | |
| CC-02 | Join message toggle | P1 | NOT RUN | | |
| CC-03 | Leave message toggle | P1 | NOT RUN | | |
| CC-04 | Player chat toggle | P0 | NOT RUN | | |
| CC-05 | Command output toggle | P1 | NOT RUN | | |
| CC-06 | Death message toggle | P0 | NOT RUN | | |
| CC-07 | Instant updates | P0 | NOT RUN | | |
| CC-08 | Persistence after restart | P0 | NOT RUN | | |
| GUI-01 | GUI opening | P1 | NOT RUN | | |
| GUI-02 | GUI permissions | P0 | NOT RUN | | |
| GUI-03 | Toggle interaction | P1 | NOT RUN | | |
| GUI-04 | Item state updates | P2 | NOT RUN | | |
| GUI-05 | GUI persistence | P1 | NOT RUN | | |
| GUI-06 | Death message warning label | P1 | NOT RUN | | |
| NR-01 | Randomize all eligible players | P0 | NOT RUN | | |
| NR-02 | Clear one player | P0 | NOT RUN | | |
| NR-03 | Clear all | P0 | NOT RUN | | |
| NR-04 | Name persistence after restart | P0 | NOT RUN | | |
| NR-05 | Duplicate prevention | P0 | NOT RUN | | |
| NR-06 | Excluded player behavior | P0 | NOT RUN | | |
| NR-07 | Owner behavior | P0 | NOT RUN | | |
| NR-08 | Name generation quality | P1 | NOT RUN | | |
| NR-09 | Underscore generation | P2 | NOT RUN | | |
| NR-10 | Number substitution generation | P2 | NOT RUN | | |
| NR-11 | Number suffix generation | P2 | NOT RUN | | |
| NR-12 | Casing variation generation | P2 | NOT RUN | | |
| FN-01 | Tab list replacement | P0 | NOT RUN | | |
| FN-02 | Chat replacement | P0 | NOT RUN | | |
| FN-03 | Join message replacement | P0 | NOT RUN | | |
| FN-04 | Leave message replacement | P0 | NOT RUN | | |
| FN-05 | Death message replacement | P0 | NOT RUN | | |
| FN-06 | Nametag replacement | P0 | NOT RUN | | |
| FN-07 | Real username leakage audit | P0 | NOT RUN | | |
| FN-08 | Real username command compatibility | P0 | NOT RUN | | |
| SR-01 | skins.txt loading | P1 | NOT RUN | | |
| SR-02 | Empty skin pool handling | P1 | NOT RUN | | |
| SR-03 | Invalid username handling | P1 | NOT RUN | | |
| SR-04 | Mojang lookup success | P0 | NOT RUN | | |
| SR-05 | Mojang lookup failure | P1 | NOT RUN | | |
| SR-06 | Cache behavior | P0 | NOT RUN | | |
| SR-07 | Randomize all eligible players | P0 | NOT RUN | | |
| SR-08 | Clear one player | P0 | NOT RUN | | |
| SR-09 | Clear all | P0 | NOT RUN | | |
| SR-10 | Skin persistence after restart | P0 | NOT RUN | | |
| SR-11 | Excluded player behavior | P0 | NOT RUN | | |
| SR-12 | Owner behavior | P0 | NOT RUN | | |
| FS-01 | Skin replacement | P0 | NOT RUN | | |
| FS-02 | Skin clearing | P0 | NOT RUN | | |
| FS-03 | Reconnect behavior | P1 | NOT RUN | | |
| FS-04 | Multiple players | P0 | NOT RUN | | |
| FS-05 | Persistence after restart | P0 | NOT RUN | | |
| ST-01 | 20+ players online | P1 | NOT RUN | | |
| ST-02 | Multiple deaths simultaneously | P0 | NOT RUN | | |
| ST-03 | Multiple randomizations | P1 | NOT RUN | | |
| ST-04 | Repeated GUI usage | P2 | NOT RUN | | |
| ST-05 | Repeated server restarts | P1 | NOT RUN | | |

## Potential Edge Cases Not Covered

- Malformed, truncated, empty-root, or wrong-type JSON currently causes config load failure rather than automatic repair.
- Disk full, read-only config directory, antivirus locks, and interrupted atomic writes.
- Server crash or forced process termination during a config write.
- Mojang rate limiting, slow responses near timeout, partial outage, invalid signatures, and API schema changes.
- Username changes while a player is offline, especially unresolved username-only owner entries.
- Offline-mode servers, proxy UUID forwarding, mixed online/offline UUIDs, and profile spoofing.
- Removing the last owner when no level-4 operator is available.
- An unresolved owner username colliding case-insensitively with another offline-mode identity.
- Vanilla ban entries created by another plugin with the same player but different source/reason.
- Player death during shutdown, world save, dimension transfer, or immediately before restart.
- Repeated death while an earlier pending or active deathban exists for the same UUID.
- Clock jumps, daylight-saving changes, system time rollback, or extremely large epoch values.
- Sound behavior across dimensions and at chunk/render-distance boundaries.
- Sound resource packs that replace or remove expected client-side audio.
- Chat messages from other mods with custom translation keys not recognized by Ashmare.
- Signed-chat reporting indicators and client moderation UI when display names are rewritten.
- Spectator, vanished, sleeping, riding, disguised, or team-formatted players.
- Scoreboard/team entry keys intentionally retaining real usernames.
- Fake-name collisions with real usernames, reserved command literals, or names of offline players.
- Other mods rewriting player info, chat, nametags, skins, death messages, or the same mixin targets.
- Client mods that cache player profiles or replace tab/nametag rendering.
- Skin sources with default/Steve/Alex textures, cape properties, or model metadata differences.
- More players than generated-name search space or thousands of persisted assignments.
- Unicode or manually edited invalid usernames in JSON.
- Concurrent manual edits to configs while the server is running.
- Dedicated server console execution of GUI-only `/ashmare chat`.
- Multiple servers sharing the same config directory.

## Release Readiness Report

### Current assessment

**Status: NOT READY FOR RELEASE VALIDATION**

The source completed a clean Gradle build on June 9, 2026. That confirms
compilation, resource processing, remapping, and packaging. It does not validate
runtime networking, Mojang services, multiplayer presentation, permissions, or
deathban scheduling. All manual tests remain `NOT RUN` until evidence is added
to the matrix.

### Release gates

- All P0 tests must be `PASS`.
- No unresolved security/permission, data-loss, ban-enforcement, identity-leak,
  or server-crash defect may remain.
- P1 failures require documented risk acceptance and a tracked fix before a
  public release.
- P2 failures may be deferred only if they are cosmetic or low probability and
  are documented in release notes.
- Mojang-dependent tests may be `BLOCKED` during an external outage, but must be
  rerun successfully before final release.
- Stress tests must show no config corruption, scheduler duplication, watchdog,
  or sustained unacceptable server performance.

### Final sign-off template

- Build artifact tested:
- Git commit/tag:
- Minecraft/Fabric/Fabric API versions:
- Java runtime:
- Test server mode/proxy:
- P0: ___ PASS / ___ FAIL / ___ BLOCKED / ___ NOT RUN
- P1: ___ PASS / ___ FAIL / ___ BLOCKED / ___ NOT RUN
- P2: ___ PASS / ___ FAIL / ___ BLOCKED / ___ NOT RUN
- Open blocking issues:
- Accepted non-blocking issues:
- QA recommendation: `READY` / `READY WITH KNOWN ISSUES` / `NOT READY`
- QA lead/date:
- Release approver/date:

## Systems Still Requiring Integration Testing

The following cannot be established by compilation and require real server or
external-service testing:

1. First-run server lifecycle and all config generation on a dedicated server.
2. Mojang UUID lookup for `Shyha`, skin profile lookup, signed texture fetch,
   cache fallback, timeout, and rate-limit behavior.
3. Non-op owner permissions and level-4 owner administration with real account
   UUIDs.
4. Vanilla ban-list interaction, delayed offline bans, expiration, reconnect
   denial, and cleanup across restarts.
5. Fabric message-event behavior for advancement, join/leave, player chat,
   command feedback, and death message variants.
6. Chest GUI packet/menu behavior on unmodified and commonly modded clients.
7. Player-info packet rewriting for tab names and skins.
8. Remote nametag refresh and the documented own-client skin reconnect path.
9. Real-name leakage across every vanilla death, join, chat, team, and
   translatable-message variant.
10. Interoperability with proxy software, permissions mods, chat mods,
    scoreboard/team plugins, vanish/disguise mods, and client profile caches.
11. Sound radius and source position behavior with real clients and dimensions.
12. 20+ player concurrency, simultaneous deaths, repeated randomization,
    restart loops, memory use, and TPS impact.
