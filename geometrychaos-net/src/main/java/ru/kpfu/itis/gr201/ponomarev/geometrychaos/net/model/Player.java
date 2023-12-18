package ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.model;

import ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.protocol.ShallowPlayer;

public class Player {

    private final int id;
    private final String username;
    private boolean downloadedMap;
    private boolean inGame;

    public Player(int id, String username) {
        this.id = id;
        this.username = username;
    }

    public ShallowPlayer toShallowPlayer() {
        return new ShallowPlayer(id, username);
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public boolean isDownloadedMap() {
        return downloadedMap;
    }

    public void setDownloadedMap(boolean downloadedMap) {
        this.downloadedMap = downloadedMap;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }
}
