package com.whist.game.network.dto;

public class ClientAction {
    private String action;
    private String roomId;
    private String playerId;
    private String playerName;
    private Integer maxPlayers;
    private Integer bidAmount;
    private Integer wagerAmount;
    private String trumpSuit;
    private String card;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Integer getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(Integer bidAmount) {
        this.bidAmount = bidAmount;
    }

    public Integer getWagerAmount() {
        return wagerAmount;
    }

    public void setWagerAmount(Integer wagerAmount) {
        this.wagerAmount = wagerAmount;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public String getTrumpSuit() {
        return trumpSuit;
    }

    public void setTrumpSuit(String trumpSuit) {
        this.trumpSuit = trumpSuit;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }
}
