package com.whist.game.core.engine;

import com.whist.game.core.state.PlayerState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GameEngine {
    private final List<PlayerState> players;
    private final int totalRounds;
    private final Map<String, Integer> cumulativeScores = new HashMap<>();
    private int currentRoundNumber;
    private Round currentRound;

    public GameEngine(List<PlayerState> players, int totalRounds, int cardsPerPlayer) {
        this.players = players;
        this.totalRounds = totalRounds;

        for (PlayerState player : players) {
            cumulativeScores.put(player.playerId(), 0);
        }

        this.currentRoundNumber = 1;
        this.currentRound = new Round(players, cardsPerPlayer);
        this.currentRound.start();
    }

    public Round currentRound() {
        return currentRound;
    }

    public int currentRoundNumber() {
        return currentRoundNumber;
    }

    public boolean isGameOver() {
        return currentRoundNumber > totalRounds;
    }

    public void finishRound() {
        for (PlayerState player : currentRound.completeRound()) {
            cumulativeScores.computeIfPresent(player.playerId(), (k, v) -> v + player.roundScore());
        }

        currentRoundNumber++;
        if (!isGameOver()) {
            currentRound = new Round(players, 13);
            currentRound.start();
        }
    }

    public Map<String, Integer> cumulativeScores() {
        return Map.copyOf(cumulativeScores);
    }

    public Optional<PlayerState> winner() {
        if (!isGameOver()) {
            return Optional.empty();
        }

        return players.stream()
                .max((left, right) -> Integer.compare(
                        cumulativeScores.get(left.playerId()),
                        cumulativeScores.get(right.playerId())
                ));
    }
}
