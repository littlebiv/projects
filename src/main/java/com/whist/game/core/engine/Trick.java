package com.whist.game.core.engine;

import com.whist.game.core.model.Card;
import com.whist.game.core.model.Suit;
import com.whist.game.core.state.PlayerState;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class Trick {
    private final Suit trump;
    private final Map<PlayerState, Card> playedCards = new LinkedHashMap<>();
    private Suit leadSuit;

    public Trick(Suit trump) {
        this.trump = trump;
    }

    public boolean playCard(PlayerState player, Card card) {
        if (playedCards.containsKey(player)) {
            return false;
        }

        if (!isLegalPlay(player.hand(), card)) {
            return false;
        }

        if (leadSuit == null) {
            leadSuit = card.suit();
        }

        playedCards.put(player, card);
        player.hand().remove(card);
        return true;
    }

    public boolean isLegalPlay(List<Card> playerHand, Card card) {
        if (!playerHand.contains(card)) {
            return false;
        }

        if (leadSuit == null) {
            return true;
        }

        if (card.suit() == leadSuit) {
            return true;
        }

        boolean hasLeadSuit = playerHand.stream().anyMatch(handCard -> handCard.suit() == leadSuit);
        return !hasLeadSuit;
    }

    public Optional<PlayerState> winner() {
        if (playedCards.isEmpty()) {
            return Optional.empty();
        }

        PlayerState currentWinner = null;
        Card winningCard = null;

        for (Map.Entry<PlayerState, Card> entry : playedCards.entrySet()) {
            Card candidate = entry.getValue();

            if (winningCard == null) {
                currentWinner = entry.getKey();
                winningCard = candidate;
                continue;
            }

            if (isHigher(candidate, winningCard)) {
                currentWinner = entry.getKey();
                winningCard = candidate;
            }
        }

        return Optional.ofNullable(currentWinner);
    }

    private boolean isHigher(Card candidate, Card current) {
        boolean candidateTrump = candidate.suit() == trump;
        boolean currentTrump = current.suit() == trump;

        if (candidateTrump && !currentTrump) {
            return true;
        }

        if (!candidateTrump && currentTrump) {
            return false;
        }

        if (candidate.suit() == current.suit()) {
            return candidate.rank().strength() > current.rank().strength();
        }

        return candidate.suit() == leadSuit && current.suit() != leadSuit;
    }

    public Map<PlayerState, Card> playedCards() {
        return Map.copyOf(playedCards);
    }

    public int size() {
        return playedCards.size();
    }

    public Suit leadSuit() {
        return leadSuit;
    }

    public Suit trump() {
        return trump;
    }
}
