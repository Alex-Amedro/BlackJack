const API_BASE_URL = 'http://localhost:8080/api';

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

export function createPlayer(pseudo) {
  const params = new URLSearchParams({ pseudo });
  return request(`/joueurs?${params.toString()}`, { method: 'POST' });
}

export function createTable() {
  return request('/tables', { method: 'POST' });
}

export function joinTable(tableId, joueurId) {
  const params = new URLSearchParams({ joueurId: String(joueurId) });
  return request(`/tables/${tableId}/join?${params.toString()}`, { method: 'POST' });
}

export function listTables() {
  return request('/tables');
}

export function listPlayers() {
  return request('/joueurs');
}
