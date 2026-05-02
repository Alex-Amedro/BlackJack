//fait par ia 
import { useState, useEffect } from 'react';
import '../assets/card_deck/2_coeur.png';
import '../assets/card_deck/pioche_bleue.png';
import './GameBoard.css';

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

  const handlePlaceBet = (amount) => {
    if (amount > 0 && amount <= gameState.playerBalance) {
      // Envoyer au backend via API
      fetch(`/api/tables/${table.id}/bet?pseudo=${user.username}&amount=${amount}`, {
        method: 'POST'
      });
      // Le backend répondra via WebSocket avec le nouvel état
      setBetAmount('');
    }
  };

  const handleHit = () => {
    fetch(`/api/tables/${table.id}/hit?pseudo=${user.username}`, {
      method: 'POST'
    });
  };

  const handleStand = () => {
    fetch(`/api/tables/${table.id}/stand?pseudo=${user.username}`, {
      method: 'POST'
    });
  };

  // Simule la réception de données (à remplacer par WebSocket)
  useEffect(() => {
    // Pour la démo: charger des données simulées
    const mockState = {
      phase: 'playing',
      dealerCards: ['as_pique', '2_coeur'],
      dealerScore: 13,
      playerCards: ['king_diamant', '8_trefle'],
      playerScore: 18,
      playerBalance: 950,
      playerBet: 50,
      allPlayers: {
        'Joueur2': {
          cards: ['5_coeur', '6_diamant'],
          score: 11,
          bet: 100
        }
      },
      roundNumber: 1,
      message: 'Your turn - Hit or Stand?'
    };

    // Décommenter pour tester:
    // setGameState(mockState);
  }, []);

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
              </div>
              <div className="quick-bets">
                <button className="quick-bet" onClick={() => handlePlaceBet(10)}>$10</button>
                <button className="quick-bet" onClick={() => handlePlaceBet(50)}>$50</button>
                <button className="quick-bet" onClick={() => handlePlaceBet(100)}>$100</button>
              </div>
              <button className="place-bet-btn" onClick={() => handlePlaceBet(betAmount)}>Place Bet</button>
            </div>
            <p className="waiting-text">⏳ Waiting for other players...</p>
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
