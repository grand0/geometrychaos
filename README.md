# Geometry Chaos

Bullet-hell rhythm-based game, shamelessly copied from projects like "Just Shapes and Beats" and "Project Arrhytmia". Features multiplayer and level editor. Still under heavy construction.

Made with JavaFX.

## Build executables

```bash
./mvnw clean install
./mvnw -pl geometrychaos-editorapp,geometrychaos-gameapp javafx:jlink
```
Executing scripts for game and editor would be available at `geometrychaos-gameapp/target/gc-game/bin/gc-game` and `geometrychaos-editorapp/target/gc-editor/bin/gc-editor` respectively.  
Executable JAR for game server would be available at `geometrychaos-net/target/geometrychaos-net-1.0-SNAPSHOT-jar-with-dependencies.jar`
