import { useState, useEffect } from 'react';
import './WaitingForTablePage.css';
import { createTable, joinTable, listTables, getRanking } from '../services/api';

function WaitingForTablePage({ user, onTable, onLogout }) {
  const [tables, setTables] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedTable, setSelectedTable] = useState(null);
  const [ranking, setRanking] = useState([]);
  const [rankingLoading, setRankingLoading] = useState(true);

  const loadTables = async () => {
    setLoading(true);
    try {
      const data = await listTables();
      setTables(data);
    } catch (err) {
      console.error('Erreur chargement tables:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadRanking = async () => {
    setRankingLoading(true);
    try {
      const data = await getRanking();
      setRanking(data);
    } catch (err) {
      console.error('Erreur chargement classement:', err);
    } finally {
      setRankingLoading(false);
    }
  };

  useEffect(() => {
    loadTables();
    loadRanking();

    // Rafraîchissement automatique toutes les 3 secondes
    const interval = setInterval(() => {
      loadTables();
      loadRanking();
    }, 5000);

    return () => clearInterval(interval);
  }, []);

  const handleCreateTable = async () => {
    try {
      const table = await createTable();
      setTables((current) => [table, ...current]);
    } catch (err) {
      console.error('Erreur creation table:', err);
    }
  };

  const handleJoinTable = async (table) => {
    setSelectedTable(table.id);
    try {
      const joinedTable = await joinTable(table.id, user.username);
      onTable(joinedTable);
    } catch (err) {
      console.error('Erreur join table:', err);
    }
  };

  // Médaille pour le top 3
  const getMedal = (rank) => {
    if (rank === 1) return '🥇';
    if (rank === 2) return '🥈';
    if (rank === 3) return '🥉';
    return `#${rank}`;
  };

  return (
    <div className="game-container">
      <header className="game-header">
        <h1>♠ BlackJack ♠</h1>
        <div className="user-info">
          <span>Bienvenue, <strong>{user.username}</strong> !</span>
          <button onClick={onLogout} className="logout-btn">
            Déconnexion
          </button>
        </div>
      </header>

      <main className="waiting-main">
        <div className="waiting-layout">
          {/* Colonne gauche : Tables */}
          <div className="waiting-content">
            <h2>Trouver une Table</h2>
            <button onClick={handleCreateTable} className="join-btn" style={{ marginBottom: '1rem' }}>
              Créer une Table
            </button>

            {loading ? (
              <div className="loading-state">
                <div className="spinner"></div>
                <p>Chargement des tables...</p>
              </div>
            ) : (
              <div className="tables-list">
                {tables.length > 0 ? (
                  tables.map((table) => (
                    <div
                      key={table.id}
                      className={`table-card ${
                        selectedTable === table.id ? 'joining' : ''
                      }`}
                    >
                      <div className="table-info">
                        <h3>{table.name}</h3>
                        <p className="players-count">
                          {table.playerCount} / {table.maxPlayers} joueurs
                        </p>
                        <div className="player-slots">
                          {Array.from({ length: table.maxPlayers }).map(
                            (_, i) => (
                              <div
                                key={i}
                                className={`slot ${
                                  i < table.playerCount ? 'filled' : 'empty'
                                }`}
                              />
                            )
                          )}
                        </div>
                      </div>
                      <button
                        onClick={() => handleJoinTable(table)}
                        disabled={selectedTable === table.id}
                        className={`join-btn ${
                          selectedTable === table.id ? 'joining' : ''
                        }`}
                      >
                        {selectedTable === table.id ? 'Connexion...' : 'Rejoindre'}
                      </button>
                    </div>
                  ))
                ) : (
                  <p className="no-tables">Aucune table disponible. Créez-en une !</p>
                )}
              </div>
            )}
          </div>

          {/* Colonne droite : Classement */}
          <div className="ranking-panel">
            <h2>🏆 Classement</h2>
            {rankingLoading ? (
              <div className="loading-state">
                <div className="spinner"></div>
                <p>Chargement...</p>
              </div>
            ) : ranking.length > 0 ? (
              <div className="ranking-list">
                {ranking.map((entry, idx) => (
                  <div
                    key={idx}
                    className={`ranking-entry ${entry.pseudo === user.username ? 'is-me' : ''} ${entry.rank <= 3 ? `top-${entry.rank}` : ''}`}
                  >
                    <span className="ranking-position">{getMedal(entry.rank)}</span>
                    <span className="ranking-pseudo">{entry.pseudo}</span>
                    <span className="ranking-solde">{entry.solde.toLocaleString()} $</span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="no-tables">Aucun joueur inscrit.</p>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}

export default WaitingForTablePage;
