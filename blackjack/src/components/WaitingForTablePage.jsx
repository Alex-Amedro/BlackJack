import { useState, useEffect } from 'react';
import './WaitingForTablePage.css';
import { createTable, joinTable, listTables } from '../services/api';

function WaitingForTablePage({ user, onTable, onLogout }) {
  const [tables, setTables] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedTable, setSelectedTable] = useState(null);

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

  useEffect(() => {
    loadTables();

    // Rafraîchissement automatique toutes les 3 secondes
    const interval = setInterval(() => {
      loadTables();
    }, 3000);

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

  return (
    <div className="game-container">
      <header className="game-header">
        <h1>♠ BlackJack ♠</h1>
        <div className="user-info">
          <span>Welcome, <strong>{user.username}</strong>!</span>
          <button onClick={onLogout} className="logout-btn">
            Logout
          </button>
        </div>
      </header>

      <main className="waiting-main">
        <div className="waiting-content">
          <h2>Find a Table</h2>
          <button onClick={handleCreateTable} className="join-btn" style={{ marginBottom: '1rem' }}>
            Create Table
          </button>

          {loading ? (
            <div className="loading-state">
              <div className="spinner"></div>
              <p>Loading available tables...</p>
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
                        {table.playerCount} / {table.maxPlayers} players
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
                      {selectedTable === table.id ? 'Joining...' : 'Join'}
                    </button>
                  </div>
                ))
              ) : (
                <p className="no-tables">No tables available right now.</p>
              )}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

export default WaitingForTablePage;
