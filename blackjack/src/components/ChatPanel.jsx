import { useState, useEffect, useRef } from 'react';
import './ChatPanel.css';
import {
  getGlobalMessages,
  sendGlobalMessage,
  getPrivateMessages,
  sendPrivateMessage,
  getFriends,
} from '../services/api';

function ChatPanel({ user }) {
  const [open, setOpen] = useState(false);
  // 'global', 'friends', 'private'
  const [tab, setTab] = useState('global');
  const [globalMessages, setGlobalMessages] = useState([]);
  const [privateMessages, setPrivateMessages] = useState([]);
  const [friends, setFriends] = useState([]);
  const [selectedFriend, setSelectedFriend] = useState(null);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const messagesEndRef = useRef(null);

  // Scroll auto vers le bas
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  // Charger les messages globaux
  const loadGlobalMessages = async () => {
    try {
      const data = await getGlobalMessages();
      setGlobalMessages(data);
    } catch (err) {
      console.error('Erreur chat global:', err);
    }
  };

  // Charger les amis
  const loadFriends = async () => {
    try {
      const data = await getFriends(user.pseudo);
      setFriends(data);
    } catch (err) {
      console.error('Erreur chargement amis:', err);
    }
  };

  // Charger la conversation privée
  const loadPrivateMessages = async (friendPseudo) => {
    try {
      const data = await getPrivateMessages(user.pseudo, friendPseudo);
      setPrivateMessages(data);
    } catch (err) {
      console.error('Erreur chat privé:', err);
    }
  };

  useEffect(() => {
    if (!open) return;

    loadGlobalMessages();
    loadFriends();

    const interval = setInterval(() => {
      if (tab === 'global') loadGlobalMessages();
      if (tab === 'friends') loadFriends();
      if (tab === 'private' && selectedFriend) loadPrivateMessages(selectedFriend.pseudo);
    }, 3000);

    return () => clearInterval(interval);
  }, [open, tab, selectedFriend]);

  useEffect(() => {
    scrollToBottom();
  }, [globalMessages, privateMessages]);

  const handleSendGlobal = async (e) => {
    e.preventDefault();
    if (!input.trim() || sending) return;
    setSending(true);
    try {
      await sendGlobalMessage(user.pseudo, input.trim());
      setInput('');
      await loadGlobalMessages();
    } catch (err) {
      console.error('Erreur envoi:', err);
    } finally {
      setSending(false);
    }
  };

  const handleSendPrivate = async (e) => {
    e.preventDefault();
    if (!input.trim() || sending || !selectedFriend) return;
    setSending(true);
    try {
      await sendPrivateMessage(user.pseudo, selectedFriend.pseudo, input.trim());
      setInput('');
      await loadPrivateMessages(selectedFriend.pseudo);
    } catch (err) {
      console.error('Erreur envoi privé:', err);
    } finally {
      setSending(false);
    }
  };

  const openPrivateChat = (friend) => {
    setSelectedFriend(friend);
    setTab('private');
    setInput('');
    loadPrivateMessages(friend.pseudo);
  };

  const backToFriends = () => {
    setTab('friends');
    setSelectedFriend(null);
    setPrivateMessages([]);
    setInput('');
  };

  // Formater la date
  const formatTime = (dateStr) => {
    if (!dateStr) return '';
    try {
      const d = new Date(dateStr);
      return d.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
    } catch {
      return '';
    }
  };

  if (!open) {
    return (
      <button className="chat-bubble" onClick={() => setOpen(true)} title="Ouvrir le chat">
        💬
      </button>
    );
  }

  return (
    <div className="chat-panel">
      <div className="chat-header">
        <div className="chat-tabs">
          <button
            className={`chat-tab ${tab === 'global' ? 'active' : ''}`}
            onClick={() => { setTab('global'); setSelectedFriend(null); }}
          >
            🌍 Global
          </button>
          <button
            className={`chat-tab ${tab === 'friends' || tab === 'private' ? 'active' : ''}`}
            onClick={() => { setTab('friends'); setSelectedFriend(null); loadFriends(); }}
          >
            👥 Amis
          </button>
        </div>
        <button className="chat-close" onClick={() => setOpen(false)}>✕</button>
      </div>

      <div className="chat-body">
        {/* ─── Chat Global ─── */}
        {tab === 'global' && (
          <>
            <div className="chat-messages">
              {globalMessages.length === 0 ? (
                <p className="chat-empty">Aucun message. Soyez le premier !</p>
              ) : (
                globalMessages.map((msg) => (
                  <div
                    key={msg.id}
                    className={`chat-msg ${msg.pseudo === user.pseudo ? 'mine' : ''}`}
                  >
                    <span className="chat-msg-author">{msg.pseudo}</span>
                    <span className="chat-msg-text">{msg.contenu}</span>
                    <span className="chat-msg-time">{formatTime(msg.date)}</span>
                  </div>
                ))
              )}
              <div ref={messagesEndRef} />
            </div>
            <form className="chat-input-bar" onSubmit={handleSendGlobal}>
              <input
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                placeholder="Message global..."
                maxLength={200}
              />
              <button type="submit" disabled={sending || !input.trim()}>➤</button>
            </form>
          </>
        )}

        {/* ─── Liste d'amis ─── */}
        {tab === 'friends' && (
          <div className="chat-friends-list">
            {friends.length === 0 ? (
              <p className="chat-empty">Aucun ami. Ajoutez des amis via le panneau Amis !</p>
            ) : (
              friends.map((friend) => (
                <button
                  key={friend.id}
                  className="chat-friend-item"
                  onClick={() => openPrivateChat(friend)}
                >
                  <span className="friend-avatar">👤</span>
                  <span className="friend-name">{friend.pseudo}</span>
                  <span className="friend-arrow">💬</span>
                </button>
              ))
            )}
          </div>
        )}

        {/* ─── Chat Privé ─── */}
        {tab === 'private' && selectedFriend && (
          <>
            <div className="chat-private-header">
              <button className="chat-back-btn" onClick={backToFriends}>← Retour</button>
              <span className="chat-private-name">💬 {selectedFriend.pseudo}</span>
            </div>
            <div className="chat-messages">
              {privateMessages.length === 0 ? (
                <p className="chat-empty">Démarrez la conversation !</p>
              ) : (
                privateMessages.map((msg) => (
                  <div
                    key={msg.id}
                    className={`chat-msg ${msg.fromPseudo === user.pseudo ? 'mine' : ''}`}
                  >
                    <span className="chat-msg-author">{msg.fromPseudo}</span>
                    <span className="chat-msg-text">{msg.contenu}</span>
                    <span className="chat-msg-time">{formatTime(msg.date)}</span>
                  </div>
                ))
              )}
              <div ref={messagesEndRef} />
            </div>
            <form className="chat-input-bar" onSubmit={handleSendPrivate}>
              <input
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                placeholder={`Message à ${selectedFriend.pseudo}...`}
                maxLength={200}
              />
              <button type="submit" disabled={sending || !input.trim()}>➤</button>
            </form>
          </>
        )}
      </div>
    </div>
  );
}

export default ChatPanel;
