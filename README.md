# Geometry Chaos

> Check out [Releases](https://github.com/grand0/geometrychaos/releases) page for installers and portable versions of the game.

Bullet-hell rhythm-based game, idea is shamelessly copied from projects like [Just Shapes & Beats](https://store.steampowered.com/app/531510/Just_Shapes__Beats/) and [Project Arrhythmia](https://store.steampowered.com/app/440310/Project_Arrhythmia/). Features multiplayer and level editor. Still under heavy construction.

Made with pure JavaFX.

## About maps

Levels for this game are called **maps** and are files that have an extension `.gcmap`. To create a map one can use the level editor. Map includes a level and a music file.  
In multiplayer when one client chooses a map it automatically downloads on all other clients, so everyone is guaranteed to play the same map.

## Build executables

```bash
./mvnw clean install
./mvnw -pl geometrychaos-editorapp,geometrychaos-gameapp javafx:jlink
```
Executing scripts for game and editor would be available at `geometrychaos-gameapp/target/gc-game/bin/gc-game` and `geometrychaos-editorapp/target/gc-editor/bin/gc-editor` respectively.  
Executable JAR for game server would be available at `geometrychaos-net/target/geometrychaos-net-<version>-jar-with-dependencies.jar`

## Sample video

Sample video of playing a map created in the editor.  
Music: Sharks - Field of View

https://github.com/grand0/geometrychaos/assets/53438383/2dd538c8-b11d-4d9f-8e98-45c813d0725c
