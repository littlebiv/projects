package com.whist.game.core.state;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.whist.game.core.model.Bid;
import com.whist.game.core.model.Card;

public class PlayerState {
    private final String playerId;
    private final String displayName;
    private final List<Card> hand = new ArrayList<>();
    private Bid activeBid;
    private Integer declaredTricks;
    private int tricksWon;
    private int roundScore;

    public PlayerState(String displayName) {
        this.playerId = UUID.randomUUID().toString();
        this.displayName = displayName;
    }

    public PlayerState(String playerId, String displayName) {
        this.playerId = playerId;
        this.displayName = displayName;
    }

    public String playerId() {
        return playerId;
    }

    public String displayName() {
        return displayName;
    }

    public List<Card> hand() {
        return hand;
    }

    public Bid activeBid() {
        return activeBid;
    }

    public void setActiveBid(Bid activeBid) {
        this.activeBid = activeBid;
    }

    public int tricksWon() {
        return tricksWon;
    }

    public Integer declaredTricks() {
        return declaredTricks;
    }

    public void setDeclaredTricks(Integer declaredTricks) {
        this.declaredTricks = declaredTricks;
    }

    public void incrementTricksWon() {
        this.tricksWon++;
    }

    public int roundScore() {
        return roundScore;
    }

    public void setRoundScore(int roundScore) {
        this.roundScore = roundScore;
    }

    public void resetForNewRound() {
        hand.clear();
        activeBid = null;
        declaredTricks = null;
        tricksWon = 0;
        roundScore = 0;
    }
}
