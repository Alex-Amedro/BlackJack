import { useState, useEffect } from 'react';
import './FriendsPanel.css';
import {
  sendFriendRequest,
  getPendingRequests,
  acceptFriendRequest,
  rejectFriendRequest,
  getFriends,
} from '../services/api';

function FriendsPanel({ user }) {
  const [open, setOpen] = useState(false);
  const [tab, setTab] = useState('list'); // 'list', 'pending', 'add'
  const [friends, setFriends] = useState([]);
  const [pending, setPending] = useState([]);
  const [searchPseudo, setSearchPseudo] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const loadFriends = async () => {
    try {
      const data = await getFriends(user.pseudo);
      setFriends(data);
    } catch (err) {
      console.error('Erreur amis:', err);
    }
  };

  const loadPending = async () => {
    try {
      const data = await getPendingRequests(user.pseudo);
      setPending(data);
    } catch (err) {
      console.error('Erreur pending:', err);
    }
  };

  useEffect(() => {
    if (!open) return;
    loadFriends();
    loadPending();

    const interval = setInterval(() => {
      loadPending();
      loadFriends();
    }, 5000);

    return () => clearInterval(interval);
  }, [open]);

  const handleSendRequest = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');

    if (!searchPseudo.trim()) {
      setError('Entrez un pseudo.');
      return;
    }

    setLoading(true);
    try {
      const result = await sendFriendRequest(user.pseudo, searchPseudo.trim());
      setMessage(result.message || 'Demande envoyée !');
      setSearchPseudo('');
    } catch (err) {
      setError(err.message || 'Erreur lors de l\'envoi.');
    } finally {
      setLoading(false);
    }
  };

  const handleAccept = async (requestId) => {
    try {
      await acceptFriendRequest(requestId);
      loadPending();
      loadFriends();
    } catch (err) {
      console.error('Erreur acceptation:', err);
    }
  };

  const handleReject = async (requestId) => {
    try {
      await rejectFriendRequest(requestId);
      loadPending();
    } catch (err) {
      console.error('Erreur refus:', err);
    }
  };

  if (!open) {
    return (
      <button className="friends-bubble" onClick={() => setOpen(true)} title="Amis">
        👥
        {pending.length > 0 && <span className="friends-badge">{pending.length}</span>}
      </button>
    );
  }

  return (
    <div className="friends-panel">
      <div className="friends-header">
        <div className="friends-tabs">
          <button
            className={`friends-tab ${tab === 'list' ? 'active' : ''}`}
            onClick={() => setTab('list')}
          >
            Amis ({friends.length})
          </button>
          <button
            className={`friends-tab ${tab === 'pending' ? 'active' : ''}`}
            onClick={() => { setTab('pending'); loadPending(); }}
          >
            Demandes {pending.length > 0 && `(${pending.length})`}
          </button>
          <button
            className={`friends-tab ${tab === 'add' ? 'active' : ''}`}
            onClick={() => setTab('add')}
          >
            + Ajouter
          </button>
        </div>
        <button className="friends-close" onClick={() => setOpen(false)}>✕</button>
      </div>

      <div className="friends-body">
        {/* ─── Liste d'amis ─── */}
        {tab === 'list' && (
          <div className="friends-list">
            {friends.length === 0 ? (
              <p className="friends-empty">Aucun ami pour le moment.</p>
            ) : (
              friends.map((friend) => (
                <div key={friend.id} className="friends-entry">
                  <span className="friends-entry-avatar">👤</span>
                  <div className="friends-entry-info">
                    <span className="friends-entry-name">{friend.pseudo}</span>
                    <span className="friends-entry-solde">{friend.solde.toLocaleString()} $</span>
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {/* ─── Demandes en attente ─── */}
        {tab === 'pending' && (
          <div className="friends-list">
            {pending.length === 0 ? (
              <p className="friends-empty">Aucune demande en attente.</p>
            ) : (
              pending.map((req) => (
                <div key={req.id} className="friends-request">
                  <div className="friends-request-info">
                    <span className="friends-request-name">👤 {req.fromPseudo}</span>
                    <span className="friends-request-date">{req.date ? new Date(req.date).toLocaleDateString('fr-FR') : ''}</span>
                  </div>
                  <div className="friends-request-actions">
                    <button className="friends-accept" onClick={() => handleAccept(req.id)}>✓</button>
                    <button className="friends-reject" onClick={() => handleReject(req.id)}>✗</button>
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {/* ─── Ajouter un ami ─── */}
        {tab === 'add' && (
          <div className="friends-add">
            <p className="friends-add-desc">Entrez le pseudo du joueur à ajouter :</p>
            <form onSubmit={handleSendRequest} className="friends-add-form">
              <input
                type="text"
                value={searchPseudo}
                onChange={(e) => setSearchPseudo(e.target.value)}
                placeholder="Pseudo du joueur..."
                autoFocus
              />
              <button type="submit" disabled={loading}>
                {loading ? '...' : 'Envoyer'}
              </button>
            </form>
            {error && <div className="friends-error">{error}</div>}
            {message && <div className="friends-success">{message}</div>}
          </div>
        )}
      </div>
    </div>
  );
}

export default FriendsPanel;
