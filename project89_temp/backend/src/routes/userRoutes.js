const express = require('express');
const router = express.Router();
const {
  getLeaderboard,
  getUserProfile
} = require('../controllers/userController');

/**
 * Route: GET /api/v1/users/leaderboard
 * Description: Fetch top 20 users sorted by totalDistance descending
 */
router.get('/leaderboard', getLeaderboard);

/**
 * Route: GET /api/v1/users/profile/:userId
 * Description: Fetch user profile with gamification stats (streak, distance, rank)
 */
router.get('/profile/:userId', getUserProfile);

module.exports = router;
