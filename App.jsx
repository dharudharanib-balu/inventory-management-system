import React, { useState } from 'react';
import Sidebar from './components/Sidebar';
import Navbar from './components/Navbar';
import Toast from './components/Toast';
import Dashboard from './pages/Dashboard';
import Products from './pages/Products';
import Suppliers from './pages/Suppliers';
import Customers from './pages/Customers';
import Sales from './pages/Sales';
import './App.css';

export default function App() {
  const [activePage, setActivePage] = useState('dashboard');
  const [toasts, setToasts] = useState([]);

  const showToast = (message, type = 'success') => {
    const id = Date.now() + Math.random();
    setToasts((prev) => [...prev, { id, message, type }]);
  };

  const removeToast = (id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  const renderPage = () => {
    switch (activePage) {
      case 'dashboard':
        return <Dashboard setActivePage={setActivePage} showToast={showToast} />;
      case 'products':
        return <Products showToast={showToast} />;
      case 'suppliers':
        return <Suppliers showToast={showToast} />;
      case 'customers':
        return <Customers showToast={showToast} />;
      case 'sales':
        return <Sales showToast={showToast} />;
      default:
        return <Dashboard setActivePage={setActivePage} showToast={showToast} />;
    }
  };

  return (
    <div className="app-container">
      <Sidebar activePage={activePage} setActivePage={setActivePage} />
      
      <div className="main-wrapper">
        <Navbar activePage={activePage} />
        <main className="content-body">
          {renderPage()}
        </main>
      </div>

      <Toast toasts={toasts} removeToast={removeToast} />
    </div>
  );
}
