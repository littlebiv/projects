package com.whist.game.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final List<Card> cards;

    private Deck(List<Card> cards) {
        this.cards = cards;
    }

    public static Deck create(int playerCount) {
        if (playerCount != 3 && playerCount != 4) {
            throw new IllegalArgumentException("Whist supports 3 or 4 players.");
        }

        List<Card> generated = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                generated.add(new Card(suit, rank));
            }
        }

        if (playerCount == 3) {
            generated.removeIf(card ->
                    card.rank() == Rank.TWO
                            || card.rank() == Rank.THREE
                            || card.rank() == Rank.FOUR
                            || (card.rank() == Rank.FIVE && card.suit() == Suit.SPADES));
        }

        return new Deck(generated);
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public List<List<Card>> deal(int playerCount, int cardsPerPlayer) {
        int required = playerCount * cardsPerPlayer;
        if (cards.size() < required) {
            throw new IllegalStateException("Not enough cards to deal.");
        }

        List<List<Card>> hands = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            hands.add(new ArrayList<>());
        }

        for (int i = 0; i < required; i++) {
            hands.get(i % playerCount).add(cards.get(i));
        }

        return hands;
    }
}
