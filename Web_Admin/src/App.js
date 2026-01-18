import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import CreateStory from './pages/CreateStory';
import ManageStories from './pages/ManageStories';
import EditStory from './pages/EditStory';
import ManageUsers from './pages/ManageUsers';

function App() {
  const isAuthenticated = !!localStorage.getItem('accessToken');

  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route 
          path="/dashboard" 
          element={isAuthenticated ? <Dashboard /> : <Navigate to="/login" />} 
        />
        <Route 
          path="/stories/new" 
          element={isAuthenticated ? <CreateStory /> : <Navigate to="/login" />} 
        />
        <Route 
          path="/stories/edit/:id" 
          element={isAuthenticated ? <EditStory /> : <Navigate to="/login" />} 
        />
        <Route 
          path="/stories" 
          element={isAuthenticated ? <ManageStories /> : <Navigate to="/login" />} 
        />
        <Route 
          path="/users" 
          element={isAuthenticated ? <ManageUsers /> : <Navigate to="/login" />} 
        />
        <Route path="/" element={<Navigate to="/login" />} />
      </Routes>
    </Router>
  );
}

export default App;
