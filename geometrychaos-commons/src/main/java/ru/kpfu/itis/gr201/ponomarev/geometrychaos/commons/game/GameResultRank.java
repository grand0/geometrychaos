package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game;

import java.util.function.Predicate;

public enum GameResultRank {
    S(
            "S",
            player -> player.getTotalHits() == 0
    ),
    A(
            "A",
            player -> player.getTotalDeaths() == 0
    ),
    B(
            "B",
            player -> player.getTotalDeaths() == 1
    ),
    C(
            "C",
            player -> player.getTotalDeaths() > 1
    ),
    ;

    private final String displayString;
    private final Predicate<Player> isObtainedPredicate;

    GameResultRank(String displayString, Predicate<Player> isObtainedPredicate) {
        this.displayString = displayString;
        this.isObtainedPredicate = isObtainedPredicate;
    }

    public static GameResultRank rankPlayer(Player player) {
        for (GameResultRank rank : values()) {
            if (rank.isObtainedPredicate.test(player)) {
                return rank;
            }
        }
        return null;
    }

    public String getDisplayString() {
        return displayString;
    }

    @Override
    public String toString() {
        return displayString;
    }
}
