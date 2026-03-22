package com.whist.game.core.engine;

import java.util.ArrayList;
import java.util.List;

import com.whist.game.core.model.Bid;
import com.whist.game.core.model.Card;
import com.whist.game.core.model.Deck;
import com.whist.game.core.model.Suit;
import com.whist.game.core.state.PlayerState;

public class Round {
    private final List<PlayerState> players;
    private final int cardsPerPlayer;
    private AuctionManager auctionManager;
    private Suit trump;

    public Round(List<PlayerState> players, int cardsPerPlayer) {
        this.players = players;
        this.cardsPerPlayer = cardsPerPlayer;
    }

    public void start() {
        Deck deck = Deck.create(players.size());
        deck.shuffle();
        List<List<Card>> hands = deck.deal(players.size(), cardsPerPlayer);

        for (int i = 0; i < players.size(); i++) {
            PlayerState player = players.get(i);
            player.resetForNewRound();
            player.hand().addAll(hands.get(i));
        }

        auctionManager = new AuctionManager(players);
    }

    public boolean submitBid(PlayerState player, Bid bid) {
        return auctionManager.placeBid(player, bid);
    }

    public void passBid() {
        auctionManager.pass();
    }

    public boolean finalizeAuction() {
        if (!auctionManager.isComplete()) {
            return false;
        }

        trump = auctionManager.highestBid().map(Bid::trump).orElse(null);
        return trump != null;
    }

    public Trick startTrick() {
        if (trump == null) {
            throw new IllegalStateException("Auction must complete before starting trick.");
        }
        return new Trick(trump);
    }

    public AuctionManager auctionManager() {
        return auctionManager;
    }

    public Suit trump() {
        return trump;
    }

    public int cardsPerPlayer() {
        return cardsPerPlayer;
    }

    @SuppressWarnings("UnnecessaryUnboxing")
    public List<PlayerState> completeRound() {
        for (PlayerState player : players) {
            int bidAmount;
            Integer declaredTricks = player.declaredTricks();
            if (declaredTricks != null) {
                bidAmount = declaredTricks;
            } else if (player.activeBid() != null) {
                bidAmount = player.activeBid().amount();
            } else {
                bidAmount = 0;
            }
            int won = player.tricksWon();
            int score;

            if (bidAmount == 0) {
                score = won == 0 ? 50 : -10 * won;
            } else if (won >= bidAmount) {
                score = bidAmount * bidAmount + (won - bidAmount);
            } else {
                score = -1 * (bidAmount - won) * (bidAmount - won);
            }

            player.setRoundScore(score);
        }

        return new ArrayList<>(players);
    }
}
