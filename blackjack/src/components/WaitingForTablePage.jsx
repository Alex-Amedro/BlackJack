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
    // On n'appelle plus l'API REST qui crée des problèmes de désynchronisation
    // On passe directement sur la GameBoard qui ouvrira le WebSocket
    setTimeout(() => {
      onTable({ ...table, tableId: table.id });
    }, 500);
  };

  const handleCreateTable = () => {
    fetch('http://localhost:8080/api/tables', {
      method: 'POST'
    })
      .then(res => res.json())
      .then(newTable => {
        setTables([...tables, newTable]);
      })
      .catch(err => console.error('Erreur création table:', err));
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
          <button onClick={handleCreateTable} style={{ marginBottom: '20px', padding: '10px 20px', background: '#28a745', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: '600' }}>
            + Create New Table
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
