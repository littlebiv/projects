package com.whist.game.persistence;

import java.util.Map;

public interface DataStore {
    void saveVictoryPoints(Map<String, Integer> points);

    Map<String, Integer> getLeaderboard();
}
