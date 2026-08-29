/**
 * server.js - Express server for Campus Route-to-Art API
 */
const express = require('express');
const cors = require('cors');
require('dotenv').config();

const sessionController = require('./controllers/sessionController');

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// Health Check
app.get('/api/health', (req, res) => {
  res.json({
    status: 'OK',
    service: 'Campus Route-to-Art API',
    timestamp: new Date().toISOString()
  });
});

// API Routes
app.post('/api/sessions/upload', sessionController.uploadWalkSession);
app.get('/api/artworks/user/:userId', sessionController.getUserArtworks);
app.get('/api/leaderboard', sessionController.getLeaderboard);

// Start Server
if (process.env.NODE_ENV !== 'test') {
  app.listen(PORT, () => {
    console.log(`🎨 Campus Route-to-Art Backend Server running on http://localhost:${PORT}`);
  });
}

module.exports = app;
