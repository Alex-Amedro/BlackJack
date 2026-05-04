import { useState, useEffect } from 'react';
import './WaitingForTablePage.css';

function WaitingForTablePage({ user, onTable, onLogout }) {
  const [tables, setTables] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedTable, setSelectedTable] = useState(null);

  useEffect(() => {
    // Fetch vraies tables depuis le backend
    fetch('http://localhost:8080/api/tables')
      .then(res => res.json())
      .then(data => {
        setTables(data);
        setLoading(false);
      })
      .catch(err => {
        console.error('Erreur chargement tables:', err);
        setLoading(false);
      });
  }, []);

  const handleJoinTable = (table) => {
    setSelectedTable(table.id);
    // Appeler l'API pour rejoindre la table
    fetch(`http://localhost:8080/api/tables/${table.id}/join?pseudo=${user.username}`, {
      method: 'POST'
    })
      .then(() => {
        setTimeout(() => {
          onTable({ ...table, tableId: table.id });
        }, 500);
      })
      .catch(err => console.error('Erreur join table:', err));
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
                        {table.players} / {table.maxPlayers} players
                      </p>
                      <div className="player-slots">
                        {Array.from({ length: table.maxPlayers }).map(
                          (_, i) => (
                            <div
                              key={i}
                              className={`slot ${
                                i < table.players ? 'filled' : 'empty'
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
