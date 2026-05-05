let ws = null;
let onGameStateChange = null;
let messageBuffer = [];

export function connect(tableId, pseudo) {
  return new Promise((resolve, reject) => {
    try {
      ws = new WebSocket(`ws://localhost:8080/ws/blackjack/${tableId}/${pseudo}`);

      ws.onopen = () => {
        console.log('WebSocket connecté:', tableId, pseudo);
        // Envoyer les messages en buffer
        messageBuffer.forEach(msg => ws.send(msg));
        messageBuffer = [];
        resolve();
      };

      ws.onmessage = (event) => {
        try {
          const gameState = JSON.parse(event.data);
          console.log('Received game state:', gameState);
          if (onGameStateChange) {
            onGameStateChange(gameState);
          }
        } catch (err) {
          console.error("Invalid JSON from server", err, event.data);
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
    console.log('Sending action:', action);
    ws.send(action);
  } else {
    console.warn('WebSocket not ready, buffering action:', action);
    messageBuffer.push(action);
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
