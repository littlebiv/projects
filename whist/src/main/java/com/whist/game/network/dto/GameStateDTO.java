package com.whist.game.network.dto;

import java.util.List;
import java.util.Map;

public class GameStateDTO {
    private String roomId;
    private String yourPlayerId;
    private String phase;
    private String turnPlayerId;
    private Integer roundNumber;
    private String trumpSuit;
    private String highestBid;
    private String highestBidder;
    private List<String> players;
    private Map<String, Integer> declaredTricks;
    private Integer tricksPerRound;
    private Integer requiredPlayers;
    private Integer joinedPlayers;
    private Map<String, Integer> tricksWonThisRound;
    private Map<String, Integer> cumulativeScores;
    private List<String> tableCards;
    private List<String> yourHand;
    private String message;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getYourPlayerId() {
        return yourPlayerId;
    }

    public void setYourPlayerId(String yourPlayerId) {
        this.yourPlayerId = yourPlayerId;
    }

    public String getTurnPlayerId() {
        return turnPlayerId;
    }

    public void setTurnPlayerId(String turnPlayerId) {
        this.turnPlayerId = turnPlayerId;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }

    public String getTrumpSuit() {
        return trumpSuit;
    }

    public void setTrumpSuit(String trumpSuit) {
        this.trumpSuit = trumpSuit;
    }

    public String getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(String highestBid) {
        this.highestBid = highestBid;
    }

    public String getHighestBidder() {
        return highestBidder;
    }

    public void setHighestBidder(String highestBidder) {
        this.highestBidder = highestBidder;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public Map<String, Integer> getDeclaredTricks() {
        return declaredTricks;
    }

    public void setDeclaredTricks(Map<String, Integer> declaredTricks) {
        this.declaredTricks = declaredTricks;
    }

    public Integer getTricksPerRound() {
        return tricksPerRound;
    }

    public void setTricksPerRound(Integer tricksPerRound) {
        this.tricksPerRound = tricksPerRound;
    }

    public Integer getRequiredPlayers() {
        return requiredPlayers;
    }

    public void setRequiredPlayers(Integer requiredPlayers) {
        this.requiredPlayers = requiredPlayers;
    }

    public Integer getJoinedPlayers() {
        return joinedPlayers;
    }

    public void setJoinedPlayers(Integer joinedPlayers) {
        this.joinedPlayers = joinedPlayers;
    }

    public Map<String, Integer> getTricksWonThisRound() {
        return tricksWonThisRound;
    }

    public void setTricksWonThisRound(Map<String, Integer> tricksWonThisRound) {
        this.tricksWonThisRound = tricksWonThisRound;
    }

    public Map<String, Integer> getCumulativeScores() {
        return cumulativeScores;
    }

    public void setCumulativeScores(Map<String, Integer> cumulativeScores) {
        this.cumulativeScores = cumulativeScores;
    }

    public List<String> getTableCards() {
        return tableCards;
    }

    public void setTableCards(List<String> tableCards) {
        this.tableCards = tableCards;
    }

    public List<String> getYourHand() {
        return yourHand;
    }

    public void setYourHand(List<String> yourHand) {
        this.yourHand = yourHand;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
