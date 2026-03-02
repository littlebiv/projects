package com.whist.game.network;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.whist.game.core.model.Suit;
import com.whist.game.network.dto.GameStateDTO;

class GameRoomTest {

    @Test
    void roomTransitionsFromAuctionToTrickAndAdvancesTurnAfterPlay() {
        GameRoom room = new GameRoom(4, 2, 13);

        room.addOrReconnectPlayer("p1", "P1");
        room.addOrReconnectPlayer("p2", "P2");
        room.addOrReconnectPlayer("p3", "P3");
        room.addOrReconnectPlayer("p4", "P4");

        assertEquals("AUCTION", room.snapshotFor("p1").getPhase());
        assertEquals("p1", room.snapshotFor("p1").getTurnPlayerId());

        room.applyBid("p1", 1, Suit.CLUBS);
        room.applyPass("p2");
        room.applyPass("p3");
        room.applyPass("p4");

        GameStateDTO afterAuction = room.snapshotFor("p1");
        assertEquals("WAGER", afterAuction.getPhase());
        assertEquals("p1", afterAuction.getTurnPlayerId());

        room.applyWager("p1", 3);
        room.applyWager("p2", 3);
        room.applyWager("p3", 3);
        room.applyWager("p4", 3);

        GameStateDTO afterWagers = room.snapshotFor("p1");
        assertEquals("TRICK", afterWagers.getPhase());
        assertEquals(3, afterWagers.getDeclaredTricks().get("P1"));
        assertEquals(3, afterWagers.getDeclaredTricks().get("P2"));

        String p1Card = afterWagers.getYourHand().getFirst();
        room.applyPlayCard("p1", p1Card);

        GameStateDTO p2State = room.snapshotFor("p2");
        assertEquals("TRICK", p2State.getPhase());
        assertEquals("p2", p2State.getTurnPlayerId());
        assertFalse(p2State.getTableCards().isEmpty());

        String leadSuit = extractSuit(p2State.getTableCards().getFirst());
        String p2Card = choosePlayableCard(p2State.getYourHand(), leadSuit);
        room.applyPlayCard("p2", p2Card);

        assertEquals("p3", room.snapshotFor("p3").getTurnPlayerId());
    }

    @Test
    void lastWagerCannotMakeTotalEqualToThirteen() {
        GameRoom room = new GameRoom(4, 2, 13);

        room.addOrReconnectPlayer("p1", "P1");
        room.addOrReconnectPlayer("p2", "P2");
        room.addOrReconnectPlayer("p3", "P3");
        room.addOrReconnectPlayer("p4", "P4");

        room.applyBid("p1", 1, Suit.CLUBS);
        room.applyPass("p2");
        room.applyPass("p3");
        room.applyPass("p4");

        room.applyWager("p1", 4);
        room.applyWager("p2", 4);
        room.applyWager("p3", 4);

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> room.applyWager("p4", 1));
        assertEquals("Total declared tricks cannot equal 13. Choose another value.", error.getMessage());
    }

    @Test
    void threePlayerRoomStartsAfterThirdPlayerJoins() {
        GameRoom room = new GameRoom(3, 2, 13);

        room.addOrReconnectPlayer("p1", "P1");
        room.addOrReconnectPlayer("p2", "P2");

        GameStateDTO waiting = room.snapshotFor("p1");
        assertEquals("WAITING", waiting.getPhase());
        assertEquals(3, waiting.getRequiredPlayers());
        assertEquals(2, waiting.getJoinedPlayers());

        room.addOrReconnectPlayer("p3", "P3");

        GameStateDTO started = room.snapshotFor("p1");
        assertEquals("AUCTION", started.getPhase());
        assertEquals(3, started.getRequiredPlayers());
        assertEquals(3, started.getJoinedPlayers());
    }

    @Test
    void completedTrickStaysVisibleDuringPauseThenAdvances() {
        GameRoom room = new GameRoom(3, 2, 13);

        room.addOrReconnectPlayer("p1", "P1");
        room.addOrReconnectPlayer("p2", "P2");
        room.addOrReconnectPlayer("p3", "P3");

        room.applyBid("p1", 1, Suit.CLUBS);
        room.applyPass("p2");
        room.applyPass("p3");

        room.applyWager("p1", 4);
        room.applyWager("p2", 4);
        room.applyWager("p3", 3);

        GameStateDTO p1State = room.snapshotFor("p1");
        String p1Card = p1State.getYourHand().getFirst();
        room.applyPlayCard("p1", p1Card);

        GameStateDTO p2State = room.snapshotFor("p2");
        String p2LeadSuit = extractSuit(p2State.getTableCards().getFirst());
        room.applyPlayCard("p2", choosePlayableCard(p2State.getYourHand(), p2LeadSuit));

        GameStateDTO p3State = room.snapshotFor("p3");
        String p3LeadSuit = extractSuit(p3State.getTableCards().getFirst());
        room.applyPlayCard("p3", choosePlayableCard(p3State.getYourHand(), p3LeadSuit));

        GameStateDTO paused = room.snapshotFor("p1");
        assertEquals("TRICK_PAUSE", paused.getPhase());
        assertEquals(3, paused.getTableCards().size());

        int wonTotal = paused.getTricksWonThisRound().values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(1, wonTotal);

        room.advanceAfterTrickPause();
        GameStateDTO resumed = room.snapshotFor("p1");
        assertEquals("TRICK", resumed.getPhase());
        assertEquals(0, resumed.getTableCards().size());
    }

    private String extractSuit(String tableCard) {
        String cardText = tableCard.split(": ")[1];
        return cardText.split(" of ")[1];
    }

    private String choosePlayableCard(List<String> hand, String leadSuit) {
        return hand.stream()
                .filter(card -> card.endsWith(" of " + leadSuit))
                .findFirst()
                .orElse(hand.getFirst());
    }
}
