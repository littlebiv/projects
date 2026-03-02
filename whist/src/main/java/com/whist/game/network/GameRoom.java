package com.whist.game.network;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.whist.game.core.engine.AuctionManager;
import com.whist.game.core.engine.GameEngine;
import com.whist.game.core.engine.Round;
import com.whist.game.core.engine.Trick;
import com.whist.game.core.model.Bid;
import com.whist.game.core.model.Card;
import com.whist.game.core.model.Rank;
import com.whist.game.core.model.Suit;
import com.whist.game.core.state.PlayerState;
import com.whist.game.network.dto.GameStateDTO;

public class GameRoom {
    private enum Phase {
        WAITING,
        AUCTION,
        WAGER,
        TRICK,
        TRICK_PAUSE,
        ROUND_COMPLETE,
        GAME_OVER
    }

    private final String roomId = UUID.randomUUID().toString().substring(0, 8);
    private final List<PlayerState> players = new CopyOnWriteArrayList<>();
    private final int maxPlayers;
    private final int totalRounds;
    private final int cardsPerPlayer;

    private GameEngine gameEngine;
    private Phase phase = Phase.WAITING;
    private int currentPlayerIndex = 0;
    private int auctionWinnerIndex = 0;
    private int nextTrickLeaderIndex = 0;
    private boolean pauseEndsRound;
    private Trick currentTrick;
    private final Map<String, Integer> declaredTricks = new LinkedHashMap<>();
    private String lastMessage = "Waiting for players";

    public GameRoom(int maxPlayers) {
        this(maxPlayers, 10, 13);
    }

    public GameRoom(int maxPlayers, int totalRounds, int cardsPerPlayer) {
        this.maxPlayers = maxPlayers;
        this.totalRounds = totalRounds;
        this.cardsPerPlayer = cardsPerPlayer;
    }

    public String roomId() {
        return roomId;
    }

    public int maxPlayers() {
        return maxPlayers;
    }

    public synchronized PlayerState addOrReconnectPlayer(String playerId, String displayName) {
        PlayerState existing = findPlayer(playerId);
        if (existing != null) {
            return existing;
        }

        if (players.size() >= maxPlayers) {
            throw new IllegalStateException("Room is full.");
        }

        String resolvedName = displayName == null || displayName.isBlank() ? "Player" + (players.size() + 1) : displayName;
        PlayerState player = new PlayerState(playerId, resolvedName);
        players.add(player);

        if (players.size() == maxPlayers && gameEngine == null) {
            gameEngine = new GameEngine(players, totalRounds, cardsPerPlayer);
            phase = Phase.AUCTION;
            currentPlayerIndex = 0;
            lastMessage = "Auction started";
        }

        return player;
    }

    public synchronized void applyBid(String playerId, int bidAmount, Suit trump) {
        ensurePhase(Phase.AUCTION);
        PlayerState actor = requireTurnPlayer(playerId);

        Bid bid = new Bid(bidAmount, trump);
        boolean accepted = gameEngine.currentRound().submitBid(actor, bid);
        if (!accepted) {
            throw new IllegalStateException("Bid must be higher than current highest bid.");
        }

        lastMessage = actor.displayName() + " bids " + bid.amount() + " " + bid.trump();
        advanceTurn();
        tryFinalizeAuction();
    }

    public synchronized void applyPass(String playerId) {
        ensurePhase(Phase.AUCTION);
        PlayerState actor = requireTurnPlayer(playerId);

        gameEngine.currentRound().passBid();
        lastMessage = actor.displayName() + " passes";
        advanceTurn();
        tryFinalizeAuction();
    }

    public synchronized void applyPlayCard(String playerId, String cardText) {
        ensurePhase(Phase.TRICK);
        PlayerState actor = requireTurnPlayer(playerId);

        Card parsedCard = parseCard(cardText);
        if (parsedCard == null) {
            throw new IllegalStateException("Invalid card format.");
        }

        boolean played = currentTrick.playCard(actor, parsedCard);
        if (!played) {
            throw new IllegalStateException("Illegal card play. Follow suit when possible.");
        }

        lastMessage = actor.displayName() + " played " + parsedCard;

        if (currentTrick.size() == players.size()) {
            PlayerState winner = currentTrick.winner().orElseThrow();
            winner.incrementTricksWon();
            lastMessage = winner.displayName() + " won the trick";
            nextTrickLeaderIndex = players.indexOf(winner);
            currentPlayerIndex = nextTrickLeaderIndex;
            pauseEndsRound = roundCompleted();
            phase = Phase.TRICK_PAUSE;
            return;
        }

        advanceTurn();
    }

    public synchronized void applyWager(String playerId, int wagerAmount) {
        ensurePhase(Phase.WAGER);
        PlayerState actor = requireTurnPlayer(playerId);

        int maxTricks = gameEngine.currentRound().cardsPerPlayer();
        if (wagerAmount < 0 || wagerAmount > maxTricks) {
            throw new IllegalStateException("Wager must be between 0 and " + maxTricks + ".");
        }

        if (declaredTricks.containsKey(actor.playerId())) {
            throw new IllegalStateException("You already declared your wager.");
        }

        int currentTotal = declaredTricks.values().stream().mapToInt(Integer::intValue).sum();
        boolean lastDeclaration = declaredTricks.size() == players.size() - 1;
        if (lastDeclaration && currentTotal + wagerAmount == maxTricks) {
            throw new IllegalStateException("Total declared tricks cannot equal " + maxTricks + ". Choose another value.");
        }

        declaredTricks.put(actor.playerId(), wagerAmount);
        actor.setDeclaredTricks(wagerAmount);
        lastMessage = actor.displayName() + " declared " + wagerAmount + " tricks";

        if (declaredTricks.size() == players.size()) {
            currentPlayerIndex = auctionWinnerIndex;
            currentTrick = gameEngine.currentRound().startTrick();
            phase = Phase.TRICK;
            lastMessage = "All wagers locked. Trick play started.";
            return;
        }

        advanceTurn();
    }

    public synchronized List<GameStateDTO> snapshotsForAllPlayers() {
        List<GameStateDTO> snapshots = new ArrayList<>();
        for (PlayerState player : players) {
            snapshots.add(snapshotFor(player.playerId()));
        }
        return snapshots;
    }

    public synchronized GameStateDTO snapshotFor(String playerId) {
        GameStateDTO dto = new GameStateDTO();
        dto.setRoomId(roomId);
        dto.setYourPlayerId(playerId);
        dto.setPhase(phase.name());
        dto.setMessage(lastMessage);
        dto.setRequiredPlayers(maxPlayers);
        dto.setJoinedPlayers(players.size());

        dto.setPlayers(players.stream()
                .map(player -> player.playerId() + ":" + player.displayName())
                .toList());

        if (!players.isEmpty()) {
            dto.setTurnPlayerId(players.get(currentPlayerIndex).playerId());
        }

        if (gameEngine != null) {
            dto.setRoundNumber(gameEngine.currentRoundNumber());
            dto.setCumulativeScores(mapScoresByDisplayName(gameEngine.cumulativeScores()));
            dto.setTricksWonThisRound(mapTricksWonByDisplayName());

            Round round = gameEngine.currentRound();
            dto.setTricksPerRound(round.cardsPerPlayer());
            if (round.trump() != null) {
                dto.setTrumpSuit(round.trump().name());
            }

            AuctionManager auction = round.auctionManager();
            if (auction != null) {
                dto.setHighestBid(auction.highestBid().map(bid -> bid.amount() + " " + bid.trump()).orElse("-"));
                dto.setHighestBidder(auction.highestBidder().map(PlayerState::displayName).orElse("-"));
            }
        }

        dto.setDeclaredTricks(mapDeclaredTricksByDisplayName());

        if (currentTrick != null) {
            List<String> tableCards = currentTrick.playedCards().entrySet().stream()
                    .map(entry -> entry.getKey().displayName() + ": " + entry.getValue())
                    .toList();
            dto.setTableCards(tableCards);
        } else {
            dto.setTableCards(List.of());
        }

        PlayerState player = findPlayer(playerId);
        if (player != null) {
            List<String> hand = player.hand().stream()
                    .sorted(Comparator.comparing((Card card) -> card.suit().ordinal())
                            .thenComparing(card -> card.rank().strength()))
                    .map(Card::toString)
                    .toList();
            dto.setYourHand(hand);
        } else {
            dto.setYourHand(List.of());
        }

        return dto;
    }

    public synchronized boolean hasPlayer(String playerId) {
        return findPlayer(playerId) != null;
    }

    public synchronized boolean isTrickPauseActive() {
        return phase == Phase.TRICK_PAUSE;
    }

    public synchronized boolean advanceAfterTrickPause() {
        if (phase != Phase.TRICK_PAUSE) {
            return false;
        }

        if (pauseEndsRound) {
            gameEngine.finishRound();
            if (gameEngine.isGameOver()) {
                phase = Phase.GAME_OVER;
                String winnerName = gameEngine.winner().map(PlayerState::displayName).orElse("Unknown");
                lastMessage = "Game over. Winner: " + winnerName;
            } else {
                phase = Phase.AUCTION;
                currentTrick = null;
                declaredTricks.clear();
                lastMessage = "Round complete. New auction started";
            }
            pauseEndsRound = false;
            return true;
        }

        currentPlayerIndex = nextTrickLeaderIndex;
        currentTrick = gameEngine.currentRound().startTrick();
        phase = Phase.TRICK;
        lastMessage = "Next trick started";
        pauseEndsRound = false;
        return true;
    }

    private boolean roundCompleted() {
        if (players.isEmpty()) {
            return false;
        }
        return players.get(0).hand().isEmpty();
    }

    private void tryFinalizeAuction() {
        Round round = gameEngine.currentRound();
        if (!round.finalizeAuction()) {
            return;
        }

        PlayerState leader = round.auctionManager().highestBidder().orElse(players.get(0));
        auctionWinnerIndex = players.indexOf(leader);
        currentPlayerIndex = auctionWinnerIndex;
        currentTrick = null;
        declaredTricks.clear();
        phase = Phase.WAGER;
        lastMessage = "Auction complete. Trump is " + round.trump() + ". Declare tricks.";
    }

    private void ensurePhase(Phase expected) {
        if (phase != expected) {
            throw new IllegalStateException("Action not allowed in phase " + phase);
        }
    }

    private PlayerState requireTurnPlayer(String playerId) {
        if (players.isEmpty()) {
            throw new IllegalStateException("No players in room.");
        }
        PlayerState expected = players.get(currentPlayerIndex);
        if (!expected.playerId().equals(playerId)) {
            throw new IllegalStateException("Not your turn.");
        }
        return expected;
    }

    private void advanceTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    private PlayerState findPlayer(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            return null;
        }

        return players.stream()
                .filter(player -> player.playerId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    private Map<String, Integer> mapScoresByDisplayName(Map<String, Integer> scoresByPlayerId) {
        Map<String, Integer> scores = new LinkedHashMap<>();
        for (PlayerState player : players) {
            scores.put(player.displayName(), scoresByPlayerId.getOrDefault(player.playerId(), 0));
        }
        return scores;
    }

    private Map<String, Integer> mapDeclaredTricksByDisplayName() {
        Map<String, Integer> wagers = new LinkedHashMap<>();
        for (PlayerState player : players) {
            Integer value = declaredTricks.get(player.playerId());
            wagers.put(player.displayName(), value);
        }
        return wagers;
    }

    private Map<String, Integer> mapTricksWonByDisplayName() {
        Map<String, Integer> tricksWon = new LinkedHashMap<>();
        for (PlayerState player : players) {
            tricksWon.put(player.displayName(), player.tricksWon());
        }
        return tricksWon;
    }

    private Card parseCard(String cardText) {
        if (cardText == null || cardText.isBlank()) {
            return null;
        }

        String[] parts = cardText.split(" of ");
        if (parts.length != 2) {
            return null;
        }

        try {
            Rank rank = Rank.valueOf(parts[0].trim());
            Suit suit = Suit.valueOf(parts[1].trim());
            return new Card(suit, rank);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
