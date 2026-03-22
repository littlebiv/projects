package com.whist.game.core.engine;

import com.whist.game.core.model.Bid;
import com.whist.game.core.state.PlayerState;

import java.util.List;
import java.util.Optional;

public class AuctionManager {
    private final List<PlayerState> players;
    private Bid highestBid;
    private PlayerState highestBidder;
    private int consecutivePasses;

    public AuctionManager(List<PlayerState> players) {
        this.players = players;
    }

    public boolean placeBid(PlayerState player, Bid bid) {
        if (highestBid != null && bid.compareTo(highestBid) <= 0) {
            return false;
        }

        player.setActiveBid(bid);
        highestBid = bid;
        highestBidder = player;
        consecutivePasses = 0;
        return true;
    }

    public void pass() {
        consecutivePasses++;
    }

    public boolean isComplete() {
        int playerCount = players.size();
        return playerCount > 1 && consecutivePasses >= playerCount - 1 && highestBid != null;
    }

    public Optional<Bid> highestBid() {
        return Optional.ofNullable(highestBid);
    }

    public Optional<PlayerState> highestBidder() {
        return Optional.ofNullable(highestBidder);
    }
}
