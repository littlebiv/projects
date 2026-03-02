class UIManager {
  setStatus(text) {
    document.getElementById('status').textContent = text;
  }

  setActionHint(text) {
    document.getElementById('actionHint').textContent = text;
  }

  updateMeta(gameState) {
    const bidText = gameState.highestBid ? `${gameState.highestBid} (${gameState.highestBidder || '-'})` : '-';
    document.getElementById('highestBid').textContent = bidText;
    document.getElementById('phaseText').textContent = gameState.phase || '-';
    document.getElementById('roundText').textContent = gameState.roundNumber || '-';
    document.getElementById('trumpText').textContent = gameState.trumpSuit || '-';
  }

  updateTurnBanner(gameState, playerId) {
    const turnBanner = document.getElementById('turnBanner');
    if (!gameState.turnPlayerId) {
      turnBanner.classList.remove('active');
      turnBanner.classList.add('waiting');
      turnBanner.textContent = 'Waiting for all players to join...';
      return;
    }

    const isYourTurn = gameState.turnPlayerId === playerId;

    if (isYourTurn) {
      turnBanner.classList.remove('waiting');
      turnBanner.classList.add('active');
      turnBanner.textContent = `Your turn (${gameState.phase})`;
    } else {
      turnBanner.classList.remove('active');
      turnBanner.classList.add('waiting');
      turnBanner.textContent = `Waiting for opponent (${gameState.phase})`;
    }
  }

  updatePlayers(players = []) {
    const container = document.getElementById('playersList');
    container.innerHTML = '';

    if (!players.length) {
      container.textContent = 'No players yet.';
      return;
    }

    players.forEach((playerText, index) => {
      const parts = playerText.split(':');
      const name = parts.length > 1 ? parts.slice(1).join(':') : playerText;
      const row = document.createElement('div');
      row.className = 'player-item';
      row.textContent = `Player ${index + 1}: ${name}`;
      container.appendChild(row);
    });
  }

  setAuctionControlsEnabled(enabled) {
    document.getElementById('bidValue').disabled = !enabled;
    document.getElementById('trumpSuit').disabled = !enabled;
    document.getElementById('placeBidBtn').disabled = !enabled;
    document.getElementById('passBtn').disabled = !enabled;
  }

  setWagerControlsEnabled(enabled) {
    document.getElementById('wagerValue').disabled = !enabled;
    document.getElementById('submitWagerBtn').disabled = !enabled;
  }

  updateWagerBoard(players = [], declaredTricks = {}, tricksPerRound = 13) {
    const board = document.getElementById('wagerBoard');
    board.innerHTML = '';

    let declaredTotal = 0;
    players.forEach(playerText => {
      const parts = playerText.split(':');
      const displayName = parts.length > 1 ? parts.slice(1).join(':') : playerText;
      const value = declaredTricks[displayName];
      if (Number.isInteger(value)) {
        declaredTotal += value;
      }

      const row = document.createElement('div');
      row.className = 'wager-item';
      row.textContent = `${displayName}: ${Number.isInteger(value) ? value : '-'}`;
      board.appendChild(row);
    });

    document.getElementById('declaredTotal').textContent = `${declaredTotal}/${tricksPerRound}`;
  }

  updateScores(scores = {}) {
    const scoreBoard = document.getElementById('scoreBoard');
    scoreBoard.innerHTML = '';
    Object.entries(scores).forEach(([name, score]) => {
      const row = document.createElement('div');
      row.textContent = `${name}: ${score}`;
      scoreBoard.appendChild(row);
    });
  }

  updateTrickBoard(tricksWon = {}) {
    const board = document.getElementById('trickBoard');
    board.innerHTML = '';
    Object.entries(tricksWon).forEach(([name, value]) => {
      const row = document.createElement('div');
      row.className = 'wager-item';
      row.textContent = `${name}: ${value}`;
      board.appendChild(row);
    });
  }

  setRoomId(roomId) {
    document.getElementById('roomId').value = roomId;
  }

  setPhaseVisibility(phase) {
    const controlsPanel = document.getElementById('controlsPanel');
    const playersPanel = document.getElementById('playersPanel');
    const auctionPanel = document.getElementById('auctionModal');
    const wagerPanel = document.getElementById('wagerPanel');
    const tablePanel = document.getElementById('tablePanel');
    const handPanel = document.getElementById('handPanel');
    const gameInfoPanel = document.getElementById('gameInfoPanel');

    const show = (element, visible) => {
      if (!element) {
        return;
      }
      element.classList.toggle('hidden', !visible);
    };

    show(controlsPanel, phase === 'WAITING' || phase === 'GAME_OVER');
    show(playersPanel, true);

    show(auctionPanel, phase === 'AUCTION');
    show(wagerPanel, phase === 'WAGER');

    show(tablePanel, phase === 'TRICK' || phase === 'TRICK_PAUSE' || phase === 'GAME_OVER');
    show(handPanel, phase === 'TRICK' || phase === 'TRICK_PAUSE' || phase === 'AUCTION' || phase === 'WAGER');

    show(gameInfoPanel, phase !== 'WAITING');
  }

  renderTableCards(cards = []) {
    const table = document.getElementById('tableCards');
    table.innerHTML = '';
    cards.forEach(card => table.appendChild(this.createCard(card, false, null, true)));
  }

  renderHand(cards = [], canPlay = false, onCardClick = null) {
    const hand = document.getElementById('handView');
    hand.innerHTML = '';
    cards.forEach(card => hand.appendChild(this.createCard(card, canPlay, onCardClick, false)));
  }

  createCard(text, canPlay = false, onCardClick = null, isTableCard = false) {
    const { ownerLabel, cardText } = this.extractCardParts(text, isTableCard);

    const div = document.createElement('div');
    div.className = 'card';

    const image = document.createElement('img');
    image.className = 'card-face';
    image.src = this.buildCardImage(cardText);
    image.alt = cardText;
    image.draggable = false;
    div.appendChild(image);

    if (ownerLabel) {
      const owner = document.createElement('div');
      owner.className = 'card-owner';
      owner.textContent = ownerLabel;
      div.appendChild(owner);
    }

    if (canPlay && onCardClick) {
      div.classList.add('playable');
      div.addEventListener('click', () => onCardClick(cardText));
    } else {
      div.classList.add('disabled');
    }
    return div;
  }

  extractCardParts(text, isTableCard) {
    if (!isTableCard || !text.includes(': ')) {
      return { ownerLabel: null, cardText: text };
    }

    const parts = text.split(': ');
    if (parts.length < 2) {
      return { ownerLabel: null, cardText: text };
    }

    const ownerLabel = parts[0].trim();
    const cardText = parts.slice(1).join(': ').trim();
    return { ownerLabel, cardText };
  }

  buildCardImage(cardText) {
    const parsed = this.parseCard(cardText);
    if (!parsed) {
      return this.buildFallbackCardImage(cardText);
    }

    const rankMap = {
      ACE: 'A',
      KING: 'K',
      QUEEN: 'Q',
      JACK: 'J',
      TEN: '10',
      NINE: '9',
      EIGHT: '8',
      SEVEN: '7',
      SIX: '6',
      FIVE: '5',
      FOUR: '4',
      THREE: '3',
      TWO: '2'
    };

    const suitMap = {
      SPADES: '♠',
      HEARTS: '♥',
      DIAMONDS: '♦',
      CLUBS: '♣'
    };

    const isRed = parsed.suit === 'HEARTS' || parsed.suit === 'DIAMONDS';
    const color = isRed ? '#c62828' : '#1d1d1d';
    const rank = rankMap[parsed.rank] || parsed.rank;
    const suit = suitMap[parsed.suit] || '?';

    const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="140" height="196" viewBox="0 0 140 196">
  <rect x="2" y="2" width="136" height="192" rx="12" fill="#ffffff" stroke="#d5dbe3" stroke-width="3"/>
  <text x="14" y="30" font-family="Arial, sans-serif" font-size="24" font-weight="700" fill="${color}">${rank}</text>
  <text x="14" y="52" font-family="Arial, sans-serif" font-size="22" fill="${color}">${suit}</text>
  <text x="70" y="116" text-anchor="middle" font-family="Arial, sans-serif" font-size="64" fill="${color}">${suit}</text>
  <g transform="translate(140,196) rotate(180)">
    <text x="14" y="30" font-family="Arial, sans-serif" font-size="24" font-weight="700" fill="${color}">${rank}</text>
    <text x="14" y="52" font-family="Arial, sans-serif" font-size="22" fill="${color}">${suit}</text>
  </g>
</svg>`;

    return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`;
  }

  buildFallbackCardImage(label) {
    const escaped = String(label || '').slice(0, 18).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="140" height="196" viewBox="0 0 140 196">
  <rect x="2" y="2" width="136" height="192" rx="12" fill="#ffffff" stroke="#d5dbe3" stroke-width="3"/>
  <text x="70" y="102" text-anchor="middle" font-family="Arial, sans-serif" font-size="16" fill="#333">${escaped}</text>
</svg>`;
    return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`;
  }

  parseCard(cardText) {
    if (!cardText || !cardText.includes(' of ')) {
      return null;
    }

    const [rank, suit] = cardText.split(' of ').map(part => part.trim());
    if (!rank || !suit) {
      return null;
    }

    return { rank, suit };
  }
}
