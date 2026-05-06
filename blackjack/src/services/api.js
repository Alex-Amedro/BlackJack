const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api';

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `HTTP ${response.status}`);
  }

  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    return response.json();
  }
  return response.text();
}

export function health() {
  return request('/health');
}

export function registerPlayer(pseudo, mdp = '') {
  return request('/joueurs/inscription', {
    method: 'POST',
    body: JSON.stringify({ pseudo, mdp }),
  });
}

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
