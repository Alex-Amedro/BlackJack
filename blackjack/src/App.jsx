import { useState } from 'react'
import LoginPage from './components/LoginPage'
import WaitingForTablePage from './components/WaitingForTablePage'
import './App.css'

function App() {
    const [currentPage, setCurrentPage] = useState('login'); // 'login', 'waiting', 'playing'
    const [user, setUser] = useState(null);
    const [selectedTable, setSelectedTable] = useState(null);

    const handleLogin = (userData) => {
        setUser(userData);
        setCurrentPage('waiting');
    };

    const handleTableJoin = (table) => {
        setSelectedTable(table);
        setCurrentPage('playing');
    };

    const handleLogout = () => {
        setUser(null);
        setSelectedTable(null);
        setCurrentPage('login');
    };

    if (currentPage === 'login') {
        return <LoginPage onLogin={handleLogin} />;
    }

    if (currentPage === 'waiting') {
        return (
            <WaitingForTablePage 
                user={user} 
                onTable={handleTableJoin} 
                onLogout={handleLogout}
            />
        );
    }

    if (currentPage === 'playing') {
        return (
            <div className="game-container">
                <header className="game-header">
                    <h1>♠ BlackJack ♠</h1>
                    <div className="user-info">
                        <span>Welcome, <strong>{user.username}</strong>!</span>
                        <span className="table-info-header">Table: {selectedTable?.name}</span>
                        <button onClick={handleLogout} className="logout-btn">Logout</button>
                    </div>
                </header>
                <main>
                    <p>Game content coming soon...</p>
                </main>
            </div>
        );
    }
}

export default App;