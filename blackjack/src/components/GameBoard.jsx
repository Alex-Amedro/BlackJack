import { useEffect, useRef, useState, useCallback } from 'react';
import './GameBoard.css';

function GameBoard({ user, table, onLogout }) {
  const [gameState, setGameState] = useState(null);
  const [betAmount, setBetAmount] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const wsRef = useRef(null);

  const tableId = table?.id;

  const sendAction = useCallback((action) => {
    const ws = wsRef.current;
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(action);
    }
  }, []);

  useEffect(() => {
    if (!tableId) { setLoading(false); return; }

    const ws = new WebSocket(`ws://localhost:8080/ws/blackjack/${tableId}/${user.username}`);
    wsRef.current = ws;

    ws.onopen = () => { setLoading(false); ws.send('REFRESH'); };
    ws.onmessage = (e) => {
      try { setGameState(JSON.parse(e.data)); setLoading(false); }
      catch (err) { console.error('WS parse error:', err); }
    };
    ws.onerror = () => setError('Erreur de connexion WebSocket');
    ws.onclose = () => console.log('WS fermé');

    return () => ws.close();
  }, [tableId, user.username]);

  // Actions
  const handlePlaceBet = (amount) => {
    const n = Number(amount);
    if (!n || n <= 0) return;
    sendAction(`BET:${n}`);
    setBetAmount('');
  };
  const handleHit = () => sendAction('HIT');
  const handleStand = () => sendAction('STAND');
  const handleNewRound = () => sendAction('START');

  // Card image helpers
  const getCardImg = (name) => new URL(`../assets/card_deck/${name}.png`, import.meta.url).href;
  const backImg = new URL('../assets/card_deck/pioche_bleue.png', import.meta.url).href;

  const Card = ({ name, hidden }) => (
    <img
      src={hidden ? backImg : getCardImg(name)}
      alt={hidden ? 'Hidden' : name}
      className="card-img"
    />
  );

  // Loading
  if (loading || !gameState) {
    return (
      <div className="gb-root">
        <div className="gb-loading"><div className="gb-spinner" /><p>Connexion à la table...</p></div>
      </div>
    );
  }

  const phase = gameState.phase || 'waiting';
  const allPlayers = gameState.players || {};
  const me = allPlayers[user.username] || {};
  const others = Object.entries(allPlayers).filter(([p]) => p !== user.username);
  const resultats = gameState.resultats || {};
  const myResult = resultats[user.username];

  return (
    <div className="gb-root">
      {/* Header */}
      <header className="gb-header">
        <h1>♠ BlackJack ♠</h1>
        <div className="gb-header-info">
          <span className="gb-badge">{gameState.name}</span>
          <span className="gb-badge">Manche {gameState.roundNumber || '-'}</span>
          <span className="gb-badge gb-balance">${me.balance ?? 0}</span>
        </div>
        <button onClick={onLogout} className="gb-logout">Quitter</button>
      </header>

      {error && <div className="gb-error">{error}</div>}

      {/* Table de jeu */}
      <div className="gb-table">
        <div className="gb-felt">

          {/* PHASE: WAITING */}
          {phase === 'waiting' && (
            <div className="gb-center-msg">

              <h2>En attente de joueurs...</h2>
              <p>La partie commence dès qu'un 2ème joueur rejoint.</p>
              <p className="gb-player-count">{Object.keys(allPlayers).length} / 7 joueurs</p>
            </div>
          )}

          {/* PHASE: BETTING */}
          {phase === 'betting' && (
            <div className="gb-betting">
              <div className="gb-betting-header">
                <h2>Tour de mise - Manche {gameState.roundNumber}</h2>
              </div>

              <div className="gb-other-bets">
                {others.map(([pseudo, p]) => (
                  <div key={pseudo} className={`gb-other-bet ${p.hasBet ? 'has-bet' : ''}`}>
                    <span className="gb-other-name">{pseudo}</span>
                    <span className="gb-other-status">
                      {p.hasBet ? `Misé $${p.bet}` : 'En attente...'}
                    </span>
                  </div>
                ))}
              </div>

              {!me.hasBet ? (
                <div className="gb-my-bet">
                  <p className="gb-my-balance">Votre solde : <strong>${me.balance ?? 0}</strong></p>
                  <div className="gb-chips">
                    {[10, 25, 50, 100, 250].map((v) => (
                      <button key={v} className="gb-chip" onClick={() => handlePlaceBet(v)}
                        disabled={(me.balance ?? 0) < v}>
                        ${v}
                      </button>
                    ))}
                  </div>
                  <div className="gb-bet-custom">
                    <input type="number" placeholder="Montant..." min="1"
                      value={betAmount} onChange={(e) => setBetAmount(e.target.value)} />
                    <button className="gb-bet-btn" onClick={() => handlePlaceBet(betAmount)}>Miser</button>
                  </div>
                </div>
              ) : (
                <div className="gb-bet-waiting">
                  <p>Votre mise : <strong>${me.bet}</strong></p>
                  <p className="gb-dim">En attente des autres joueurs...</p>
                </div>
              )}
            </div>
          )}

          {/* PHASE: PLAYING */}
          {phase === 'playing' && (
            <>
              {/* Dealer */}
              <div className="gb-dealer">
                <h3 className="gb-zone-title">Croupier</h3>
                <div className="gb-cards">
                  {(gameState.dealerCards || []).map((c, i) => (
                    <Card key={`d-${i}`} name={c} hidden={i === 1} />
                  ))}
                </div>
              </div>

              {/* Separator */}
              <div className="gb-separator" />

              {/* Other players */}
              {others.length > 0 && (
                <div className="gb-others">
                  {others.map(([pseudo, p]) => (
                    <div key={pseudo} className={`gb-player-card ${p.finished ? 'finished' : ''}`}>
                      <h4>{pseudo}</h4>
                      <div className="gb-cards">
                        {(p.cards || []).map((c, i) => <Card key={`${pseudo}-${i}`} name={c} />)}
                      </div>
                      <div className="gb-player-stats">
                        <span>Score: {p.score}</span>
                        <span>Mise: ${p.bet}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {/* My hand */}
              <div className="gb-me">
                <h3 className="gb-zone-title">Votre main</h3>
                <div className="gb-cards gb-my-cards">
                  {(me.cards || []).map((c, i) => <Card key={`me-${i}`} name={c} />)}
                </div>
                <div className="gb-my-stats">
                  <span className="gb-stat">Score: <strong>{me.score ?? 0}</strong></span>
                  <span className="gb-stat">Mise: <strong>${me.bet ?? 0}</strong></span>
                  <span className="gb-stat">Solde: <strong>${me.balance ?? 0}</strong></span>
                </div>
                {!me.finished ? (
                  <div className="gb-actions">
                    <button className="gb-action gb-hit" onClick={handleHit}>TIRER</button>
                    <button className="gb-action gb-stand" onClick={handleStand}>RESTER</button>
                  </div>
                ) : (
                  <p className="gb-finished-msg">
                    {me.score > 21 ? 'Bust !' : 'Vous avez terminé.'}
                  </p>
                )}
              </div>
            </>
          )}

          {/* PHASE: RESULTS */}
          {phase === 'results' && (
            <div className="gb-results">
              <h2>Résultats - Manche {gameState.roundNumber}</h2>

              {/* Dealer final */}
              <div className="gb-dealer-result">
                <h3>Croupier - Score: {gameState.dealerScore}</h3>
                <div className="gb-cards">
                  {(gameState.dealerCards || []).map((c, i) => <Card key={`dr-${i}`} name={c} />)}
                </div>
              </div>

              {/* All results */}
              <div className="gb-results-list">
                {Object.entries(allPlayers).map(([pseudo, p]) => {
                  const res = resultats[pseudo] || '';
                  const isMe = pseudo === user.username;
                  return (
                    <div key={pseudo} className={`gb-result-card ${res.toLowerCase()} ${isMe ? 'is-me' : ''}`}>
                      <h4>{isMe ? 'Vous' : pseudo}</h4>
                      <div className="gb-cards gb-small-cards">
                        {(p.cards || []).map((c, i) => <Card key={`r-${pseudo}-${i}`} name={c} />)}
                      </div>
                      <div className="gb-result-info">
                        <span>Score: {p.score}</span>
                        <span>Mise: ${p.bet}</span>
                        <span className={`gb-result-badge ${res.toLowerCase()}`}>
                          {res === 'WIN' ? 'Gagné' : res === 'LOSE' ? 'Perdu' : 'Égalité'}
                        </span>
                      </div>
                    </div>
                  );
                })}
              </div>

              <button className="gb-new-round" onClick={handleNewRound}>
                Nouvelle manche
              </button>
            </div>
          )}

        </div>
      </div>
    </div>
  );
}

export default GameBoard;
