import React from 'react';
import { HashRouter as Router, Routes, Route } from 'react-router-dom'; // Changed here
import MainPage from './components/MainPage';
import Login from './components/Login';
import Signup from './components/Signup';
import ProfilePage from './components/ProfilePage.js';
import './App.css'

function App() {
    return (
        <Router basename="/Web/thapargpt-frontend">
            <Routes>
                <Route path="/" element={<MainPage />} />
                <Route path="/login" element={<Login />} />
                <Route path="/signup" element={<Signup />} />
                <Route path="/profile" element={<ProfilePage />} />
            </Routes>
        </Router>
    );
}

export default App;
