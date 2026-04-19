// outils de base de React
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

// Import du css valable pour tout le site
import './index.css'

// Import du composant principal
import App from './App.jsx'

// Cherche la div 'root' dans 'index.html', crée le root React dessus et render
//  le composant 'App' à l'intérieur
createRoot(document.getElementById('root')).render(
  // StrictMode pour vérifier qu'on fait pas d'erreurs 
  // en cours de développement, ça part tout seul en prod.
  <StrictMode> 
    <App /> 
  </StrictMode>
)