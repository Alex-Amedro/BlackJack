//fait par ia
import { useState, useEffect } from 'react';
import '../assets/card_deck/2_coeur.png';
import '../assets/card_deck/pioche_bleue.png';
import './GameBoard.css';
import * as websocket from '../services/websocket';

function GameBoard({ user, table, onLogout }) {
  // État du jeu (recevra les données du WebSocket/Backend)
  const [gameState, setGameState] = useState({
    phase: 'betting', // 'betting' ou 'playing' ou 'results'

    dealerCards: [], // Cartes du croupier
    dealerScore: 0,

    playerCards: [], // Mes cartes
    playerScore: 0,
    playerBalance: 1000,
    playerBet: 0,

    allPlayers: {}, // { pseudo: { cards: [], score: 0, bet: 0 } }
    roundNumber: 1,
    message: 'Place your bet to start'
  });

  // Gestion du formulaire de mise
  const [betAmount, setBetAmount] = useState('');
  const [betError, setBetError] = useState('');
  const [betMessage, setBetMessage] = useState('');

  const handlePlaceBet = (amount) => {
    amount = Number(amount);
    if (isNaN(amount) || amount <= 0) {
      setBetError('Enter a valid amount');
      setBetMessage('');
      return;
    }
    if (amount > gameState.playerBalance) {
      setBetError('Insufficient balance');
      setBetMessage('');
      return;
    }
    setBetError('');
    setBetMessage(`Bet placed: $${amount}`);
    websocket.sendAction(`BET:${amount}`);
    setBetAmount('');
    setTimeout(() => setBetMessage(''), 2000);
  };

  const handleHit = () => {
    websocket.sendAction('HIT');
  };

  const handleStand = () => {
    websocket.sendAction('STAND');
  };

  const handleStartGame = () => {
    websocket.sendAction('START');
  };

  // Connexion WebSocket au montage
  useEffect(() => {
    // Enregistrer le callback AVANT de se connecter
    websocket.onStateChange((newState) => {
      console.log('Updating game state:', newState);
      setGameState(prev => ({
        ...prev,
        ...newState
      }));
    });

    websocket.connect(table.id, user.username)
      .catch(err => console.error('Erreur connexion WebSocket:', err));

    return () => websocket.disconnect();
  }, [table.id, user.username]);

  // Fonction pour obtenir le chemin de l'image d'une carte
  const getCardImage = (cardName) => {
    return new URL(`../assets/card_deck/${cardName}.png`, import.meta.url).href;
  };

  // Affiche une seule carte
  const CardImage = ({ cardName, hidden = false }) => {
    if (hidden) {
      return (
        <img
          src={new URL('../assets/card_deck/pioche_bleue.png', import.meta.url).href}
          alt="Hidden card"
          className="card-image"
        />
      );
    }
    return (
      <img
        src={getCardImage(cardName)}
        alt={cardName}
        className="card-image"
      />
    );
  };

    // ========== AFFICHAGE PHASE DE PARIS ==========
  if (gameState.phase === 'betting') {
    return (
      <div className="game-container">
        <header className="game-header">
          <h1>♠ BlackJack ♠</h1>
          <div className="user-info">
            <span>Welcome, <strong>{user.username}</strong>!</span>
            <span className="table-info-header">Table: {table?.name}</span>
            <button onClick={onLogout} className="logout-btn">Logout</button>
          </div>
        </header>

        <main className="game-main">
          <div className="betting-phase">
            <h2>Place Your Bet</h2>
            <div className="balance-display">
              <p>Balance: <strong>${gameState.playerBalance}</strong></p>
              <p>Current Bet: <strong>${gameState.playerBet}</strong></p>
            </div>
            <div className="betting-controls">
              <div className="bet-input-group">
                <label>Bet Amount:</label>
                <input
                  type="number"
                  placeholder="Enter amount"
                  min="1"
                  max={gameState.playerBalance}
                  value={betAmount}
                  onChange={(e) => setBetAmount(Number(e.target.value))}
                />
                {betError && <p style={{ color: 'red', fontSize: '12px' }}>{betError}</p>}
                {betMessage && <p style={{ color: 'green', fontSize: '12px' }}>{betMessage}</p>}
              </div>
              <div className="quick-bets">
                <button className="quick-bet" onClick={() => handlePlaceBet(10)}>$10</button>
                <button className="quick-bet" onClick={() => handlePlaceBet(50)}>$50</button>
                <button className="quick-bet" onClick={() => handlePlaceBet(100)}>$100</button>
              </div>
              <button className="place-bet-btn" onClick={() => handlePlaceBet(betAmount)}>Place Bet</button>
              <button className="place-bet-btn" onClick={handleStartGame} style={{ marginTop: '10px', background: 'linear-gradient(135deg, #28a745 0%, #1e7e34 100%)' }}>Start Game</button>
            </div>
            
            {/* SÉPARATEUR */}
            <div className="table-separator"></div>

            {/* ZONE AUTRES JOUEURS */}
            <div className="other-players-zone">
              <h3>Other Players at Table</h3>
              {Object.keys(gameState.allPlayers).length > 0 ? (
                Object.entries(gameState.allPlayers).map(([pseudo, player]) => (
                  <div key={pseudo} className="other-player-card">
                    <h4>{pseudo}</h4>
                    <p>Bet: <strong>${player.bet}</strong></p>
                  </div>
                ))
              ) : (
                <p className="waiting-text">⏳ Waiting for other players...</p>
              )}
            </div>

          </div>
        </main>
      </div>
    );
  }

  // ========== AFFICHAGE PHASE DE JEU ==========
  if (gameState.phase === 'playing') {
    return (
      <div className="game-container">
        <header className="game-header">
          <h1>♠ BlackJack ♠</h1>
          <div className="user-info">
            <span>Welcome, <strong>{user.username}</strong>!</span>
            <span className="table-info-header">Table: {table?.name} | Round {gameState.roundNumber}</span>
            <button onClick={onLogout} className="logout-btn">Logout</button>
          </div>
        </header>

        <main className="game-main">
          {/* ZONE CROUPIER */}
          <div className="dealer-zone">
            <h3>Dealer</h3>
            <div className="cards-display">
              {gameState.dealerCards.map((card, idx) => (
                <CardImage
                  key={idx}
                  cardName={card}
                  hidden={idx === 1} // 2ème carte du croupier cachée
                />
              ))}
            </div>
            <p className="score">Score: <strong>{gameState.dealerScore}</strong></p>
          </div>

          {/* SÉPARATEUR */}
          <div className="table-separator"></div>

          {/* ZONE AUTRES JOUEURS */}
          <div className="other-players-zone">
            {Object.entries(gameState.allPlayers).map(([pseudo, player]) => (
              <div key={pseudo} className="other-player-card">
                <h4>{pseudo}</h4>
                <div className="cards-display">
                  {player.cards.map((card, idx) => (
                    <CardImage key={idx} cardName={card} />
                  ))}
                </div>
                <div className="player-info">
                  <p>Score: <strong>{player.score}</strong></p>
                  <p>Bet: <strong>${player.bet}</strong></p>
                </div>
              </div>
            ))}
          </div>

          {/* ZONE MOI (JOUEUR ACTUEL) */}
          <div className="player-zone">
            <div className="my-hand">
              <h3>Your Hand</h3>
              <div className="cards-display">
                {gameState.playerCards.map((card, idx) => (
                  <CardImage key={idx} cardName={card} />
                ))}
              </div>
              <div className="player-info">
                <p>Score: <strong>{gameState.playerScore}</strong></p>
                <p>Balance: <strong>${gameState.playerBalance}</strong></p>
                <p>Your Bet: <strong>${gameState.playerBet}</strong></p>
              </div>
            </div>

            {/* BOUTONS D'ACTION */}
            <div className="action-buttons">
              <button className="action-btn hit-btn" onClick={handleHit}>HIT</button>
              <button className="action-btn stand-btn" onClick={handleStand}>STAND</button>
            </div>

            {/* MESSAGE */}
            <p className="game-message">{gameState.message}</p>
          </div>
        </main>
      </div>
    );
  }

  // ========== AFFICHAGE RÉSULTATS ==========
  if (gameState.phase === 'results') {
    return (
      <div className="game-container">
        <header className="game-header">
          <h1>♠ BlackJack ♠</h1>
          <div className="user-info">
            <span>Welcome, <strong>{user.username}</strong>!</span>
            <button onClick={onLogout} className="logout-btn">Logout</button>
          </div>
        </header>

        <main className="game-main">
          <div className="results-phase">
            <h2>Round Results</h2>
            <div className="results-display">
              <p className="result-message">{gameState.message}</p>
              <p className="balance-update">New Balance: <strong>${gameState.playerBalance}</strong></p>
            </div>
            <button className="next-round-btn">Next Round</button>
          </div>
        </main>
      </div>
    );
  }
}

export default GameBoard;
