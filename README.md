# Litematica+

A client-side Fabric mod that extends [Litematica](https://modrinth.com/mod/litematica).

## Features

- **Container preview** - open a chest, barrel, shulker box or other container that is part of
  your schematic and every slot is compared with the schematic:
  - blue - the item is missing (the expected item is drawn in the empty slot),
  - red - a wrong item is in the slot,
  - purple - an item is in a slot that should be empty.

  Hovering a slot shows the expected (schematic) and the actual (client) item in the top right corner.
- **Block replacement** - a "Replace" button in Litematica's material list opens a window where you
  replace one block with another in a placement (with block name suggestions). Changes are shown in
  the schematic render and in the material list, and can be removed one by one or with "Reset all".
- **Delete from the material list** - a "Delete" button next to every material list entry removes
  that block from the placement.
- **Commands** - `/lplus replace <from> <to>` and `/lplus clear` for the selected placement.

## Minecraft versions

Every folder is a separate Gradle project for one Minecraft version:

| Folder | Minecraft | Java |
|---|---|---|
| `litematica-plus-1.21.4` | 1.21.4 | 21 |
| `litematica-plus-1.21.11` | 1.21.11 | 21 |
| `litematica-plus-26.1.2` | 26.1.2 | 25 |
| `litematica-plus-26.2` | 26.2 | 25 |
| `litematica-plus-26.3` | 26.3 | 25 |

Requires Fabric Loader, Fabric API, Litematica and MaLiLib for the same Minecraft version.

## Building

Open a terminal in the folder of the version you want and run:

```
./gradlew build
```

(on Windows in PowerShell: `.\gradlew build`). The mod jar is created in `build/libs/`.
Litematica and MaLiLib are downloaded automatically from Modrinth - the version IDs are set in
each project's `gradle.properties`.

## License

LGPL-3.0-only - see [LICENSE](LICENSE). The LGPL builds on the GNU GPL, whose text is in [COPYING](COPYING).

## Support

Email: hubil.support@gmail.com
