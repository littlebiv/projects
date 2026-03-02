package com.whist.game.tournament;

import com.whist.game.persistence.DataStore;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TournamentManager {
    private static final int GAMES_PER_SERIES = 10;
    private final DataStore dataStore;

    public TournamentManager(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public int gamesPerSeries() {
        return GAMES_PER_SERIES;
    }

    public void awardVictoryPoint(String winnerName) {
        Map<String, Integer> leaderboard = dataStore.getLeaderboard();
        leaderboard.merge(winnerName, 1, Integer::sum);
        dataStore.saveVictoryPoints(leaderboard);
    }

    public Map<String, Integer> leaderboard() {
        return dataStore.getLeaderboard();
    }
}
