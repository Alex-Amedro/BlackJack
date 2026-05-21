// outils de base de React
import { createRoot } from 'react-dom/client'

// Import du css valable pour tout le site
import './index.css'

// Import du composant principal
import App from './App.jsx'

// Cherche la div 'root' dans 'index.html', crée le root React dessus et render
//  le composant 'App' à l'intérieur
// Note: StrictMode retiré car il double-monte les composants en dev,
//  ce qui casse les connexions WebSocket.
createRoot(document.getElementById('root')).render(
  <App />
)