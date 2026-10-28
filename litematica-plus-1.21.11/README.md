# Litematica+ for Minecraft 1.21.11

Mod version: 1.0+1.21.11

Built against: Litematica 0.26.16 + MaLiLib 0.27.20

## Building

1. Litematica and MaLiLib are downloaded from Modrinth (version IDs in `gradle.properties`).
   To use other versions, put their jars for Minecraft 1.21.11 into the `libs` folder.
2. The Gradle wrapper is included (`gradlew`, `gradlew.bat`). On Windows (PowerShell) use `.\gradlew` instead of `./gradlew`.
3. Run `./gradlew build` - the mod jar is created in `build/libs/`.
4. Run `./gradlew runClient` to test it in the game.

Java 21 is required.

## License

LGPL-3.0-only - see `LICENSE` in the main folder of the repository.

Support: Email hubil.support@gmail.com
