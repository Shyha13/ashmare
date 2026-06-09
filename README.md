# Ashmare

Ashmare is a server-side Fabric mod for the Ashmare SMP.

## Owner system

`config/ashmare/owners.json` is the only source of Ashmare owner privileges.
Configured owners can use `/ashmare` without vanilla operator status and bypass
deathbans, fake-name randomization, and fake-skin randomization. Vanilla
permission level 2 or higher can use normal Ashmare commands; configured owners
or vanilla permission level 4 can manage the owner list with:

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

## Testing

The complete manual release checklist is in
[`docs/MANUAL_QA_CHECKLIST.md`](docs/MANUAL_QA_CHECKLIST.md).
