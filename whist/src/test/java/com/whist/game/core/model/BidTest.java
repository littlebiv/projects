package com.whist.game.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BidTest {

    @Test
    void higherSuitWinsWhenAmountIsEqual() {
        Bid clubsSeven = new Bid(7, Suit.CLUBS);
        Bid spadesSeven = new Bid(7, Suit.SPADES);

        assertTrue(spadesSeven.compareTo(clubsSeven) > 0);
    }

    @Test
    void higherAmountAlwaysWins() {
        Bid lowSpades = new Bid(5, Suit.SPADES);
        Bid highClubs = new Bid(6, Suit.CLUBS);

        assertTrue(highClubs.compareTo(lowSpades) > 0);
    }
}
