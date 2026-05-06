import { useState } from 'react';
import './LoginPage.css';
import { registerPlayer } from '../services/api';

function LoginPage({ onLogin }) {
  const [playerName, setPlayerName] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!playerName.trim()) {
      setError('Please enter your username');
      return;
    }

    setSubmitting(true);
    try {
      const player = await registerPlayer(playerName.trim());
      onLogin({
        id: player.id,
        username: player.pseudo ?? playerName.trim(),
        pseudo: player.pseudo ?? playerName.trim(),
      });
    } catch (err) {
      setError(err.message || 'Unable to create player');
    } finally {
      setSubmitting(false);
    }
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

          <button type="submit" className="login-button" disabled={submitting}>
            {submitting ? 'Starting...' : 'Start Game'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default LoginPage;
