// Import qui permet la maj auto des composants qd les variables sont modifiées
import { useState } from 'react'

// On importe nos autres pages/composants
import LoginPage from './components/LoginPage'
import WaitingForTablePage from './components/WaitingForTablePage'
import GameBoard from './components/GameBoard'

// Import du css pour l'app
import './App.css'

// Fonction de composant principal
function App() {
    // ---- STATES (mémoire de notre composant) ----
    
    // currentPage : page actuelle. Par défaut : 'login' (connexion)
    // setCurrentPage : fonction pour changer de page
    const [currentPage, setCurrentPage] = useState('login'); // 'login', 'waiting', 'playing'
    
    // user : infos du joueur connecté (null si personne n'est connecté)
    const [user, setUser] = useState(null);
    
    // selectedTable : table de blackjack choisie par le joueur
    const [selectedTable, setSelectedTable] = useState(null);


    // ---- FONCTIONS D'ACTION ----
    
    // Fonction appelée quand le joueur réussit à se connecter
    const handleLogin = (userData) => {
        setUser(userData); // On sauvegarde ses infos
        setCurrentPage('waiting'); // On l'envoie sur la page d'attente
    };

    // Fonction appelée quand le joueur rejoint une table
    const handleTableJoin = (table) => {
        setSelectedTable(table); // On sauvegarde la table qu'il a choisie
        setCurrentPage('playing'); // On l'envoie sur la page de jeu
    };

    // Fonction pour se déconnecter
    const handleLogout = () => {
        setUser(null); // On efface le joueur
        setSelectedTable(null); // On efface sa table
        setCurrentPage('login'); // On le renvoie sur l'écran de connexion
    };


    // ---- AFFICHAGE ----

    // étape 'login'
    if (currentPage === 'login') {
        // affichage du composant de login
        // 'handleLogin' pour prévenir quand la connexion réussit
        return <LoginPage onLogin={handleLogin} />;
    }

    // étape 'waiting'
    if (currentPage === 'waiting') {
        // affichage du composant d'attente
        return (
            <WaitingForTablePage 
                user={user} // On donne les infos du joueur à la page
                onTable={handleTableJoin} // fonction à lancer s'il choisit une table
                onLogout={handleLogout} // fonction à lancer s'il déco
            />
        );
    }

    // étape 'playing'
    if (currentPage === 'playing') {
        return (
            <GameBoard
                user={user}
                table={selectedTable}
                onLogout={handleLogout}
            />
        );
    }
}

// exporter le composant à la fin pour que main.jsx puisse l'utiliser
export default App;