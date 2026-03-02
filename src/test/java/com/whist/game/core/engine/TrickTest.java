package com.whist.game.core.engine;

import com.whist.game.core.model.Card;
import com.whist.game.core.model.Rank;
import com.whist.game.core.model.Suit;
import com.whist.game.core.state.PlayerState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrickTest {

    @Test
    void playerMustFollowSuitWhenPossible() {
        PlayerState first = new PlayerState("p1", "P1");
        PlayerState second = new PlayerState("p2", "P2");

        Card leadCard = new Card(Suit.HEARTS, Rank.TEN);
        Card offSuitCard = new Card(Suit.CLUBS, Rank.ACE);
        Card followSuitCard = new Card(Suit.HEARTS, Rank.TWO);

        first.hand().add(leadCard);
        second.hand().add(offSuitCard);
        second.hand().add(followSuitCard);

        Trick trick = new Trick(Suit.SPADES);
        assertTrue(trick.playCard(first, leadCard));
        assertFalse(trick.playCard(second, offSuitCard));
        assertTrue(trick.playCard(second, followSuitCard));
    }
}
