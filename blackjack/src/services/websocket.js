let ws = null;
let onGameStateChange = null;

export function connect(tableId, pseudo) {
  return new Promise((resolve, reject) => {
    try {
      const host = window.location.hostname;
      ws = new WebSocket(`ws://${host}:8080/ws/blackjack/${tableId}/${pseudo}`);

      ws.onopen = () => {
        console.log('WebSocket connecté:', tableId, pseudo);
        resolve();
      };

      ws.onmessage = (event) => {
        const gameState = JSON.parse(event.data);
        if (onGameStateChange) {
          onGameStateChange(gameState);
        }
      };

      ws.onerror = (error) => {
        console.error('WebSocket erreur:', error);
        reject(error);
      };

      ws.onclose = () => {
        console.log('WebSocket fermé');
      };
    } catch (err) {
      reject(err);
    }
  });
}

export function sendAction(action) {
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(action);
  }
}

export function onStateChange(callback) {
  onGameStateChange = callback;
}

export function disconnect() {
  if (ws) {
    ws.close();
  }
}
