import { useState } from 'react';
import './LoginPage.css';
import { registerPlayer, loginPlayer } from '../services/api';

function LoginPage({ onLogin }) {
  // 'login' ou 'register'
  const [mode, setMode] = useState('login');
  const [pseudo, setPseudo] = useState('');
  const [mdp, setMdp] = useState('');
  const [confirmMdp, setConfirmMdp] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const switchMode = (newMode) => {
    setMode(newMode);
    setError('');
    setSuccess('');
    setPseudo('');
    setMdp('');
    setConfirmMdp('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!pseudo.trim()) {
      setError('Veuillez entrer un pseudo.');
      return;
    }
    if (!mdp.trim()) {
      setError('Veuillez entrer un mot de passe.');
      return;
    }

    if (mode === 'register') {
      if (!confirmMdp.trim()) {
        setError('Veuillez confirmer votre mot de passe.');
        return;
      }
      if (mdp !== confirmMdp) {
        setError('Les mots de passe ne correspondent pas.');
        return;
      }
    }

    setSubmitting(true);
    try {
      if (mode === 'register') {
        const player = await registerPlayer(pseudo.trim(), mdp, confirmMdp);
        setSuccess('Compte créé avec succès ! Connectez-vous.');
        // Passer automatiquement en mode login
        setMode('login');
        setMdp('');
        setConfirmMdp('');
      } else {
        const player = await loginPlayer(pseudo.trim(), mdp);
        onLogin({
          id: player.id,
          username: player.pseudo,
          pseudo: player.pseudo,
          solde: player.solde,
        });
      }
    } catch (err) {
      setError(err.message || 'Une erreur est survenue.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <h1>♠ BlackJack ♠</h1>
          <p>{mode === 'login' ? 'Connexion' : 'Créer un compte'}</p>
        </div>

        {/* Onglets Login / Register */}
        <div className="auth-tabs">
          <button
            className={`auth-tab ${mode === 'login' ? 'active' : ''}`}
            onClick={() => switchMode('login')}
            type="button"
          >
            Connexion
          </button>
          <button
            className={`auth-tab ${mode === 'register' ? 'active' : ''}`}
            onClick={() => switchMode('register')}
            type="button"
          >
            Inscription
          </button>
        </div>

        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <label htmlFor="pseudo">Pseudo</label>
            <input
              type="text"
              id="pseudo"
              value={pseudo}
              onChange={(e) => setPseudo(e.target.value)}
              placeholder="Entrez votre pseudo"
              autoFocus
            />
          </div>

          <div className="form-group">
            <label htmlFor="mdp">Mot de passe</label>
            <input
              type="password"
              id="mdp"
              value={mdp}
              onChange={(e) => setMdp(e.target.value)}
              placeholder="Entrez votre mot de passe"
            />
          </div>

          {mode === 'register' && (
            <div className="form-group">
              <label htmlFor="confirmMdp">Confirmer le mot de passe</label>
              <input
                type="password"
                id="confirmMdp"
                value={confirmMdp}
                onChange={(e) => setConfirmMdp(e.target.value)}
                placeholder="Confirmez votre mot de passe"
              />
            </div>
          )}

          {error && <div className="error-message">{error}</div>}
          {success && <div className="success-message">{success}</div>}

          <button type="submit" className="login-button" disabled={submitting}>
            {submitting
              ? 'Chargement...'
              : mode === 'login'
              ? 'Se connecter'
              : "S'inscrire"}
          </button>
        </form>
      </div>
    </div>
  );
}

export default LoginPage;
