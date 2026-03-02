const networkClient = new NetworkClient();
const ui = new UIManager();

let currentRoomId = null;
let playerId = localStorage.getItem('whist-player-id') || crypto.randomUUID();
let currentGameState = null;

localStorage.setItem('whist-player-id', playerId);
ui.setPhaseVisibility('WAITING');

networkClient.connect(
  lobbyMessage => {
    ui.setStatus('Connected');
    if (lobbyMessage.startsWith('ROOM_CREATED:')) {
      currentRoomId = lobbyMessage.split(':')[1];
      ui.setRoomId(currentRoomId);
      networkClient.subscribeToRoom(currentRoomId, playerId);
      const playerName = document.getElementById('playerName').value || 'Player';
      networkClient.joinRoom(currentRoomId, playerId, playerName);
    }
  },
  gameState => {
    currentGameState = gameState;
    ui.setPhaseVisibility(gameState.phase);
    ui.setStatus(`${gameState.phase} - ${gameState.message}`);
    ui.updateMeta(gameState);
    ui.updateScores(gameState.cumulativeScores || {});
    ui.updateTrickBoard(gameState.tricksWonThisRound || {});
    ui.updateWagerBoard(gameState.players || [], gameState.declaredTricks || {}, gameState.tricksPerRound || 13);
    ui.updatePlayers(gameState.players || []);
    ui.updateTurnBanner(gameState, playerId);
    ui.renderTableCards(gameState.tableCards || []);

    const isAuctionTurn = gameState.phase === 'AUCTION' && gameState.turnPlayerId === playerId;
    const isWagerTurn = gameState.phase === 'WAGER' && gameState.turnPlayerId === playerId;
    const canPlay = gameState.phase === 'TRICK' && gameState.turnPlayerId === playerId;
    ui.setAuctionControlsEnabled(isAuctionTurn);
    ui.setWagerControlsEnabled(isWagerTurn);

    if (isAuctionTurn) {
      ui.setActionHint('Your auction turn: place a higher bid or pass.');
    } else if (isWagerTurn) {
      ui.setActionHint('Your wager turn: declare predicted tricks. Total wagers cannot be 13.');
    } else if (canPlay) {
      ui.setActionHint('Your trick turn: click a valid card to play.');
    } else if (gameState.phase === 'TRICK_PAUSE') {
      ui.setActionHint('Trick complete. Showing table cards for 3 seconds...');
    } else if (gameState.phase === 'WAITING') {
      const required = gameState.requiredPlayers || 4;
      ui.setActionHint(`Waiting for ${required} players to join this room.`);
    } else {
      ui.setActionHint('Waiting for another player action.');
    }

    ui.renderHand(gameState.yourHand || [], canPlay, card => {
      if (!currentRoomId) {
        return;
      }
      networkClient.playCard(currentRoomId, playerId, card);
    });
  },
  errorMessage => ui.setStatus(`Error: ${errorMessage}`),
  joinedMessage => {
    if (joinedMessage.startsWith('JOINED:')) {
      ui.setStatus('Joined room');
    }
  }
);

document.getElementById('createRoomBtn').addEventListener('click', () => {
  const playerName = document.getElementById('playerName').value || 'Player';
  const roomSize = Number(document.getElementById('roomSize').value || '4');
  networkClient.createRoom(playerName, roomSize);
});

document.getElementById('joinRoomBtn').addEventListener('click', () => {
  const roomId = document.getElementById('roomId').value;
  const playerName = document.getElementById('playerName').value || 'Player';
  if (!roomId) {
    ui.setStatus('Enter Room ID first');
    return;
  }
  currentRoomId = roomId;
  networkClient.subscribeToRoom(currentRoomId, playerId);
  networkClient.joinRoom(roomId, playerId, playerName);
});

document.getElementById('placeBidBtn').addEventListener('click', () => {
  if (!currentRoomId) {
    ui.setStatus('Join a room first');
    return;
  }

  if (!currentGameState || currentGameState.phase !== 'AUCTION' || currentGameState.turnPlayerId !== playerId) {
    ui.setStatus('Not your auction turn');
    return;
  }

  const bidValue = Number(document.getElementById('bidValue').value);
  const trumpSuit = document.getElementById('trumpSuit').value;
  networkClient.sendBid(currentRoomId, playerId, bidValue, trumpSuit);
  ui.setActionHint('Bid submitted. Waiting for next action...');
});

document.getElementById('passBtn').addEventListener('click', () => {
  if (!currentRoomId) {
    ui.setStatus('Join a room first');
    return;
  }

  if (!currentGameState || currentGameState.phase !== 'AUCTION' || currentGameState.turnPlayerId !== playerId) {
    ui.setStatus('Not your auction turn');
    return;
  }

  networkClient.sendPass(currentRoomId, playerId);
  ui.setActionHint('Pass submitted. Waiting for next action...');
});

document.getElementById('submitWagerBtn').addEventListener('click', () => {
  if (!currentRoomId) {
    ui.setStatus('Join a room first');
    return;
  }

  if (!currentGameState || currentGameState.phase !== 'WAGER' || currentGameState.turnPlayerId !== playerId) {
    ui.setStatus('Not your wager turn');
    return;
  }

  const wagerValue = Number(document.getElementById('wagerValue').value);
  networkClient.sendWager(currentRoomId, playerId, wagerValue);
  ui.setActionHint('Wager submitted. Waiting for next action...');
});
