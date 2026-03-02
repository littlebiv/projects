package com.whist.game.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.whist.game.core.model.Suit;
import com.whist.game.core.state.PlayerState;
import com.whist.game.network.dto.ClientAction;
import com.whist.game.network.dto.GameStateDTO;

@Controller
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/room.create")
    @SuppressWarnings("Unboxing")
    public void createRoom(@Payload ClientAction action) {
        Integer requestedMaxPlayers = action.getMaxPlayers();
        int maxPlayers = requestedMaxPlayers != null ? requestedMaxPlayers : 4;
        if (maxPlayers != 3 && maxPlayers != 4) {
            maxPlayers = 4;
        }
        GameRoom room = new GameRoom(maxPlayers);
        rooms.put(room.roomId(), room);
        messagingTemplate.convertAndSend("/topic/lobby", "ROOM_CREATED:" + room.roomId());
    }

    @MessageMapping("/room.join")
    public void joinRoom(@Payload ClientAction action) {
        GameRoom room = getRoom(action.getRoomId());
        if (room == null) {
            broadcastError(action.getRoomId(), action.getPlayerId(), "Room not found");
            return;
        }

        try {
            PlayerState player = room.addOrReconnectPlayer(action.getPlayerId(), action.getPlayerName());
            broadcastRoomState(room);
            messagingTemplate.convertAndSend("/topic/room/" + room.roomId() + "/join", "JOINED:" + player.playerId());
        } catch (IllegalStateException ex) {
            broadcastError(action.getRoomId(), action.getPlayerId(), ex.getMessage());
        }
    }

    @MessageMapping("/room.action")
    public void roomAction(@Payload ClientAction action) {
        GameRoom room = getRoom(action.getRoomId());
        if (room == null) {
            broadcastError(action.getRoomId(), action.getPlayerId(), "Room not found");
            return;
        }

        if (!room.hasPlayer(action.getPlayerId())) {
            broadcastError(action.getRoomId(), action.getPlayerId(), "Join room first");
            return;
        }

        try {
            String actionType = action.getAction() == null ? "" : action.getAction().trim().toUpperCase();
            switch (actionType) {
                case "BID" -> {
                    if (action.getBidAmount() == null || action.getTrumpSuit() == null) {
                        throw new IllegalStateException("Bid amount and trump suit are required.");
                    }
                    room.applyBid(action.getPlayerId(), action.getBidAmount(), Suit.valueOf(action.getTrumpSuit()));
                }
                case "WAGER" -> {
                    if (action.getWagerAmount() == null) {
                        throw new IllegalStateException("Wager amount is required.");
                    }
                    room.applyWager(action.getPlayerId(), action.getWagerAmount());
                }
                case "PASS" -> room.applyPass(action.getPlayerId());
                case "PLAY_CARD" -> room.applyPlayCard(action.getPlayerId(), action.getCard());
                default -> throw new IllegalStateException("Unsupported action: " + actionType);
            }

            broadcastRoomState(room);
            if ("PLAY_CARD".equals(actionType) && room.isTrickPauseActive()) {
                scheduler.schedule(() -> {
                    if (room.advanceAfterTrickPause()) {
                        broadcastRoomState(room);
                    }
                }, 3, TimeUnit.SECONDS);
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            broadcastError(action.getRoomId(), action.getPlayerId(), ex.getMessage());
            GameStateDTO snapshot = room.snapshotFor(action.getPlayerId());
            messagingTemplate.convertAndSend("/topic/room/" + room.roomId() + "/player/" + action.getPlayerId(), snapshot);
        }
    }

    private void broadcastRoomState(GameRoom room) {
        for (GameStateDTO snapshot : room.snapshotsForAllPlayers()) {
            messagingTemplate.convertAndSend("/topic/room/" + room.roomId() + "/player/" + snapshot.getYourPlayerId(), snapshot);
        }
    }

    private void broadcastError(String roomId, String playerId, String message) {
        if (roomId != null && playerId != null) {
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/errors/" + playerId, message);
            return;
        }
        messagingTemplate.convertAndSend("/topic/errors", message);
    }

    private GameRoom getRoom(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return null;
        }
        return rooms.get(roomId);
    }
}
