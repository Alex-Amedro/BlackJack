import { useEffect, useMemo, useState } from 'react';
import './GameBoard.css';
import { getTable, hit, placeBet, stand } from '../services/api';

function GameBoard({ user, table, onLogout }) {
  const [gameState, setGameState] = useState(null);
  const [betAmount, setBetAmount] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const tableId = table?.id;

  useEffect(() => {
    let active = true;

    const loadTable = async () => {
      if (!tableId) {
        setLoading(false);
        return;
      }

      setLoading(true);
      setError('');
      try {
        const snapshot = await getTable(tableId);
        if (active) {
          setGameState(snapshot);
        }
      } catch (err) {
        if (active) {
          setError(err.message || 'Unable to load table state');
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    loadTable();

    return () => {
      active = false;
    };
  }, [tableId]);

  const playerState = useMemo(() => {
    return gameState?.players?.[user.username] || null;
  }, [gameState, user.username]);

  const refreshState = async (nextState) => {
    setGameState(nextState);
  };

  const handlePlaceBet = async (amount) => {
    const parsedAmount = Number(amount);
    if (!parsedAmount || parsedAmount <= 0) {
      return;
    }

    try {
      const nextState = await placeBet(tableId, user.username, parsedAmount);
      await refreshState(nextState);
      setBetAmount('');
    } catch (err) {
      setError(err.message || 'Unable to place bet');
    }
  };

  const handleHit = async () => {
    try {
      const nextState = await hit(tableId, user.username);
      await refreshState(nextState);
    } catch (err) {
      setError(err.message || 'Unable to hit');
    }
  };

  const handleStand = async () => {
    try {
      const nextState = await stand(tableId, user.username);
      await refreshState(nextState);
    } catch (err) {
      setError(err.message || 'Unable to stand');
    }
  };

  const getCardImage = (cardName) => new URL(`../assets/card_deck/${cardName}.png`, import.meta.url).href;

  const CardImage = ({ cardName, hidden = false }) => {
    if (hidden) {
      return <img src={new URL('../assets/card_deck/pioche_bleue.png', import.meta.url).href} alt="Hidden card" className="card-image" />;
    }

    return <img src={getCardImage(cardName)} alt={cardName} className="card-image" />;
  };

  if (loading || !gameState) {
    return (
      <div className="game-container">
        <header className="game-header">
          <h1>♠ BlackJack ♠</h1>
          <div className="user-info">
            <span>
              Welcome, <strong>{user.username}</strong>!
            </span>
            <button onClick={onLogout} className="logout-btn">
              Logout
            </button>
          </div>
        </header>
        <main className="game-main">
          <p className="game-message">Loading table...</p>
        </main>
      </div>
    );
  }

  const currentPhase = gameState.phase || (gameState.resultats ? 'results' : 'playing');
  const otherPlayers = Object.entries(gameState.players || {}).filter(([pseudo]) => pseudo !== user.username);

  return (
    <div className="game-container">
      <header className="game-header">
        <h1>♠ BlackJack ♠</h1>
        <div className="user-info">
          <span>
            Welcome, <strong>{user.username}</strong>!
          </span>
          <span className="table-info-header">Table: {gameState.name || table?.name} | Round {gameState.roundNumber ?? 0}</span>
          <button onClick={onLogout} className="logout-btn">
            Logout
          </button>
        </div>
      </header>

      <main className="game-main">
        {error && <p className="game-message">{error}</p>}

        {currentPhase === 'waiting' && (
          <div className="betting-phase">
            <h2>Waiting for the round to start</h2>
            <p className="waiting-text">The table is waiting for more players.</p>
          </div>
        )}

        {currentPhase === 'playing' && (
          <>
            <div className="betting-phase">
              <h2>Place Your Bet</h2>
              <div className="balance-display">
                <p>
                  Balance: <strong>${playerState?.balance ?? 0}</strong>
                </p>
              </div>
              <div className="betting-controls">
                <div className="bet-input-group">
                  <label>Bet Amount:</label>
                  <input
                    type="number"
                    placeholder="Enter amount"
                    min="1"
                    value={betAmount}
                    onChange={(e) => setBetAmount(e.target.value)}
                  />
                </div>
                <div className="quick-bets">
                  <button className="quick-bet" onClick={() => handlePlaceBet(10)}>$10</button>
                  <button className="quick-bet" onClick={() => handlePlaceBet(50)}>$50</button>
                  <button className="quick-bet" onClick={() => handlePlaceBet(100)}>$100</button>
                </div>
                <button className="place-bet-btn" onClick={() => handlePlaceBet(betAmount)}>
                  Place Bet
                </button>
              </div>
            </div>

            <div className="dealer-zone">
              <h3>Dealer</h3>
              <div className="cards-display">
                {(gameState.dealerCards || []).map((card, idx) => (
                  <CardImage key={`${card}-${idx}`} cardName={card} hidden={idx === 1} />
                ))}
              </div>
              <p className="score">
                Score: <strong>{gameState.dealerScore ?? 0}</strong>
              </p>
            </div>

            <div className="table-separator"></div>

            <div className="other-players-zone">
              {otherPlayers.map(([pseudo, player]) => (
                <div key={pseudo} className="other-player-card">
                  <h4>{pseudo}</h4>
                  <div className="cards-display">
                    {(player.cards || []).map((card, idx) => (
                      <CardImage key={`${pseudo}-${card}-${idx}`} cardName={card} />
                    ))}
                  </div>
                  <div className="player-info">
                    <p>
                      Score: <strong>{player.score ?? 0}</strong>
                    </p>
                    <p>
                      Bet: <strong>${player.bet ?? 0}</strong>
                    </p>
                  </div>
                </div>
              ))}
            </div>

            <div className="player-zone">
              <div className="my-hand">
                <h3>Your Hand</h3>
                <div className="cards-display">
                  {(playerState?.cards || []).map((card, idx) => (
                    <CardImage key={`${card}-${idx}`} cardName={card} />
                  ))}
                </div>
                <div className="player-info">
                  <p>
                    Score: <strong>{playerState?.score ?? 0}</strong>
                  </p>
                  <p>
                    Balance: <strong>${playerState?.balance ?? 0}</strong>
                  </p>
                  <p>
                    Your Bet: <strong>${playerState?.bet ?? 0}</strong>
                  </p>
                </div>
              </div>

              <div className="action-buttons">
                <button className="action-btn hit-btn" onClick={handleHit}>
                  HIT
                </button>
                <button className="action-btn stand-btn" onClick={handleStand}>
                  STAND
                </button>
              </div>
            </div>
          </>
        )}

        {currentPhase === 'results' && (
          <div className="results-phase">
            <h2>Round Results</h2>
            <div className="results-display">
              <p className="result-message">{JSON.stringify(gameState.resultats || {})}</p>
              <p className="balance-update">
                Dealer score: <strong>{gameState.dealerScore ?? 0}</strong>
              </p>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default GameBoard;
