import { useState, useEffect } from 'react';
import './WaitingForTablePage.css';

function WaitingForTablePage({ user, onTable, onLogout }) {
  const [tables, setTables] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedTable, setSelectedTable] = useState(null);

  useEffect(() => {
    // Simulate fetching available tables
    const timer = setTimeout(() => {
      setTables([
        { id: 1, name: 'Table 1', players: 1, maxPlayers: 6 },
        { id: 2, name: 'Table 2', players: 2, maxPlayers: 6 },
        { id: 3, name: 'Table 3', players: 0, maxPlayers: 6 },
      ]);
      setLoading(false);
    }, 1500);

    return () => clearTimeout(timer);
  }, []);

  const handleJoinTable = (table) => {
    setSelectedTable(table.id);
    setTimeout(() => {
      onTable(table);
    }, 1000);
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
