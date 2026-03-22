class NetworkClient {
  constructor() {
    this.stompClient = null;
    this.onStateUpdate = null;
    this.onError = null;
    this.onJoined = null;
  }

  connect(onConnected, onStateUpdate, onError, onJoined) {
    const socket = new SockJS('/ws');
    this.stompClient = Stomp.over(socket);
    this.stompClient.connect({}, () => {
      this.stompClient.subscribe('/topic/lobby', message => onConnected(message.body));
      this.stompClient.subscribe('/topic/errors', message => onError(message.body));
      onConnected('CONNECTED');
    });

    this.onStateUpdate = onStateUpdate;
    this.onError = onError;
    this.onJoined = onJoined;
  }

  subscribeToRoom(roomId, playerId) {
    this.stompClient.subscribe(`/topic/room/${roomId}/player/${playerId}`, message => {
      this.onStateUpdate(JSON.parse(message.body));
    });

    this.stompClient.subscribe(`/topic/room/${roomId}/errors/${playerId}`, message => {
      this.onError(message.body);
    });

    this.stompClient.subscribe(`/topic/room/${roomId}/join`, message => {
      if (this.onJoined) {
        this.onJoined(message.body);
      }
    });
  }

  createRoom(playerName, maxPlayers = 4) {
    this.stompClient.send('/app/room.create', {}, JSON.stringify({ action: 'CREATE_ROOM', playerName, maxPlayers }));
  }

  joinRoom(roomId, playerId, playerName) {
    this.stompClient.send('/app/room.join', {}, JSON.stringify({ action: 'JOIN_ROOM', roomId, playerId, playerName }));
  }

  sendBid(roomId, playerId, bidAmount, trumpSuit) {
    this.stompClient.send('/app/room.action', {}, JSON.stringify({ action: 'BID', roomId, playerId, bidAmount, trumpSuit }));
  }

  sendPass(roomId, playerId) {
    this.stompClient.send('/app/room.action', {}, JSON.stringify({ action: 'PASS', roomId, playerId }));
  }

  sendWager(roomId, playerId, wagerAmount) {
    this.stompClient.send('/app/room.action', {}, JSON.stringify({ action: 'WAGER', roomId, playerId, wagerAmount }));
  }

  playCard(roomId, playerId, card) {
    this.stompClient.send('/app/room.action', {}, JSON.stringify({ action: 'PLAY_CARD', roomId, playerId, card }));
  }
}
