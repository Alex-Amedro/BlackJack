const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api';

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  });

  const contentType = response.headers.get('content-type') || '';
  let data;
  if (contentType.includes('application/json')) {
    data = await response.json();
  } else {
    data = await response.text();
  }

  if (!response.ok) {
    // Le backend renvoie { "error": "..." } en cas d'erreur
    const errorMsg = (data && data.error) ? data.error : (typeof data === 'string' ? data : `HTTP ${response.status}`);
    throw new Error(errorMsg);
  }

  return data;
}

// ─── Joueurs ─────────────────────────────────────

export function registerPlayer(pseudo, mdp, confirmMdp) {
  return request('/joueurs/inscription', {
    method: 'POST',
    body: JSON.stringify({ pseudo, mdp, confirmMdp }),
  });
}

export function loginPlayer(pseudo, mdp) {
  return request('/joueurs/login', {
    method: 'POST',
    body: JSON.stringify({ pseudo, mdp }),
  });
}

export function getRanking() {
  return request('/joueurs/ranking');
}

// ─── Tables ──────────────────────────────────────

export function createTable() {
  return request('/tables', { method: 'POST' });
}

export function listTables() {
  return request('/tables');
}

export function getTable(tableId) {
  return request(`/tables/${tableId}`);
}

export function joinTable(tableId, pseudo) {
  const params = new URLSearchParams({ pseudo });
  return request(`/tables/${tableId}/join?${params.toString()}`, { method: 'POST' });
}

export function placeBet(tableId, pseudo, amount) {
  const params = new URLSearchParams({ pseudo, amount: String(amount) });
  return request(`/tables/${tableId}/bet?${params.toString()}`, { method: 'POST' });
}

export function hit(tableId, pseudo) {
  const params = new URLSearchParams({ pseudo });
  return request(`/tables/${tableId}/hit?${params.toString()}`, { method: 'POST' });
}

export function stand(tableId, pseudo) {
  const params = new URLSearchParams({ pseudo });
  return request(`/tables/${tableId}/stand?${params.toString()}`, { method: 'POST' });
}
