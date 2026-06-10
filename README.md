# Ashmare

Ashmare is a server-side Fabric mod for the Ashmare SMP.

## Owner system

`config/ashmare/owners.json` is the only source of Ashmare owner privileges.
Configured owners can use `/ashmare` without vanilla operator status and always
bypass deathbans. Owners are affected by fake-name and fake-skin randomization
by default, but each owner can independently toggle personal bypasses with:

- `/ashmare bypass name`
- `/ashmare bypass skin`
- `/ashmare bypass list`

Enabling a bypass immediately restores the owner's real name or skin. Disabling
one makes the owner eligible for the next randomization. Bypass settings persist
inside `owners.json`. Vanilla permission level 2 or higher can use normal
Ashmare commands; configured owners or vanilla permission level 4 can manage
the owner list with:

- `/ashmare owners list`
- `/ashmare owners add <player>`
- `/ashmare owners remove <player>`

On the first startup only, a missing `owners.json` is generated with `Shyha` as
the default owner account for the Ashmare SMP. Ashmare resolves the account
through Mojang's profile repository and stores both its UUID and last known
username. If Mojang is unavailable, the entry remains with a `null` UUID and
the lookup is retried on later startups. An authenticated matching player join
also resolves the entry.

Existing `owners.json` files are loaded as written. Ashmare never re-adds the
default owner to an existing file, including an intentionally empty one.

## Skin source pool

Ashmare reads source skins from the server's
`config/ashmare/skins.txt`. Add real Minecraft Java usernames, one per line:

```text
Notch
Dream
Technoblade
Dinnerbone
```

Blank lines and lines beginning with `#` or `//` are ignored. UTF-8 files with
or without a byte-order mark are supported. Comma-, semicolon-, and
space-separated usernames are also accepted, although one username per line is
recommended. Duplicate usernames are ignored case-insensitively.

Ashmare `1.0.2` includes ten verified defaults. If `skins.txt` is missing,
empty, comment-only, or contains no usable usernames, Ashmare repairs it with
those defaults at startup or the next time `/ashmare skins randomize` runs.
Once the file contains at least one usable username, custom entries are
preserved.

Use `/ashmare skins randomize` to randomize every eligible online player. To
target one player, use:

```text
/ashmare skin randomize <player>
/ashmare skin randomize <player> <source_username>
```

Without `source_username`, Ashmare randomly chooses a resolved source from
`skins.txt`. With it, Ashmare fetches that Minecraft Java account's skin even
when the source is not listed in `skins.txt`. The target may be offline; the
assignment then appears when they next connect. Excluded players and owners
with skin bypass enabled cannot be targeted.

The file must be named exactly `skins.txt`, not `skins.txt.txt`. On a hosted
server, edit the file inside that server's `config/ashmare/` directory. Run
`/ashmare skins randomize` after saving it; Ashmare reloads the file for every
randomization, so a server restart is not required. Source accounts must exist
on Minecraft Java Edition and have a skin available through Mojang.

## Building the mod

Install Java 21, open a terminal in the Ashmare source directory, and run:

```powershell
.\gradlew.bat clean build
```

The distributable file is `build/libs/ashmare-1.0.4.jar`. Do not use the
`-sources.jar`. Place the distributable JAR and the matching Fabric API JAR in
the server's `mods/` directory, replacing the previous Ashmare JAR.

## Testing

The complete manual release checklist is in
[`docs/MANUAL_QA_CHECKLIST.md`](docs/MANUAL_QA_CHECKLIST.md).
