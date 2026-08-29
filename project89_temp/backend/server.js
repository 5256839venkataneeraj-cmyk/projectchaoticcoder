const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const dotenv = require('dotenv');

// Load environment variables
dotenv.config();

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Root route
app.get('/', (req, res) => {
  res.json({
    message: 'Welcome to the Campus Route-to-Art Backend API',
    endpoints: {
      test: '/api/test',
      sessions: '/api/v1/sessions',
      gallery: '/api/v1/artworks/gallery/:userId',
      artworks: '/api/v1/artworks',
      leaderboard: '/api/v1/users/leaderboard',
      users: '/api/v1/users'
    }
  });
});

// Mount Routes
const testRoutes = require('./src/routes/testRoutes');
const sessionRoutes = require('./src/routes/sessionRoutes');
const artRoutes = require('./src/routes/artRoutes');
const userRoutes = require('./src/routes/userRoutes');

app.use('/api', testRoutes);
app.use('/api/v1', testRoutes);
app.use('/api/v1/sessions', sessionRoutes);
app.use('/api/sessions', sessionRoutes);
app.use('/api/v1/artworks', artRoutes);
app.use('/api/artworks', artRoutes);
app.use('/api/v1/users', userRoutes);
app.use('/api/users', userRoutes);

// MongoDB connection (graceful fallback if not connected)
const MONGO_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/campus_art_db';

if (process.env.MONGODB_URI) {
  mongoose
    .connect(MONGO_URI)
    .then(() => console.log(' Connected to MongoDB successfully'))
    .catch((err) => console.warn(' MongoDB connection warning:', err.message));
} else {
  console.log('ℹ️ MONGODB_URI not configured in .env; running with in-memory / unlinked DB mode.');
}

// Start Server
if (require.main === module) {
  app.listen(PORT, () => {
    console.log(`🚀 Campus Route-to-Art Server running on http://localhost:${PORT}`);
    console.log(` Test endpoint: http://localhost:${PORT}/api/test`);
    console.log(` Gallery endpoint: http://localhost:${PORT}/api/v1/artworks/gallery/:userId`);
    console.log(` Leaderboard endpoint: http://localhost:${PORT}/api/v1/users/leaderboard`);
  });
}

module.exports = app;
