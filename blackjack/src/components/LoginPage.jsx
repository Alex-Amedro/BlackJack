import { useState } from 'react';
import './LoginPage.css';

function LoginPage({ onLogin }) {
  const [playerName, setPlayerName] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    setError('');

    if (!playerName.trim()) {
      setError('Please enter your username');
      return;
    }

    console.log('Game started with player:', playerName);

    // Call the onLogin callback
    onLogin({
      username: playerName.trim(),
    });
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <h1>♠ BlackJack ♠</h1>
          <p>Enter Your Username</p>
        </div>

        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <label htmlFor="playerName">Username</label>
            <input
              type="text"
              id="playerName"
              value={playerName}
              onChange={(e) => setPlayerName(e.target.value)}
              placeholder="Enter your username"
              autoFocus
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <button type="submit" className="login-button">
            Start Game
          </button>
        </form>
      </div>
    </div>
  );
}

export default LoginPage;
