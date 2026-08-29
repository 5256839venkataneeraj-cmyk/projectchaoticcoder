const User = require('../models/User');
const mongoose = require('mongoose');
const { SEED_USERS } = require('../utils/seed');

// Seed campus users for immediate, vibrant in-memory leaderboard
const inMemoryUsers = SEED_USERS.map((user) => ({
  ...user,
  lastSessionDate: user.lastSessionDate.toISOString
    ? user.lastSessionDate.toISOString()
    : new Date(user.lastSessionDate).toISOString()
}));

/**
 * Updates user gamification stats (Streak and Total Distance)
 * Core Gamification Engine:
 * - If distance > 500 meters and session occurs within 24-48 hours of last session: increment currentStreak by 1.
 * - If distance > 500 meters and first session: currentStreak = 1.
 * - If distance > 500 meters and <= 24 hours: retain streak (multiple sessions on same day).
 * - If distance > 500 meters and > 48 hours: streak broken, reset to 1.
 * - If distance <= 500 meters: reset currentStreak to 1 (or 0 if new).
 * - Always add session distance to totalDistance.
 */
async function updateUserGamification(userId, sessionDistance, sessionDate = new Date()) {
  const distance = Math.max(0, Number(sessionDistance) || 0);
  const now = new Date(sessionDate);

  if (mongoose.connection.readyState === 1) {
    let user = await User.findOne({ userId });
    if (!user) {
      user = new User({
        userId,
        username: userId,
        totalDistance: 0,
        currentStreak: 0,
        longestStreak: 0,
        lastSessionDate: null,
        totalSessions: 0
      });
    }

    if (distance > 500) {
      if (user.lastSessionDate) {
        const hoursDiff = (now.getTime() - new Date(user.lastSessionDate).getTime()) / (1000 * 60 * 60);
        if (hoursDiff >= 24 && hoursDiff <= 48) {
          user.currentStreak += 1;
        } else if (hoursDiff < 24) {
          // Already walked within the last 24h: retain streak
          user.currentStreak = Math.max(user.currentStreak, 1);
        } else {
          // Broken streak (>48h)
          user.currentStreak = 1;
        }
      } else {
        // First qualifying session
        user.currentStreak = 1;
      }
      user.lastSessionDate = now;
    } else {
      // Walk did not exceed 500m
      user.currentStreak = 1;
      if (!user.lastSessionDate) user.lastSessionDate = now;
    }

    user.totalDistance = Math.round((user.totalDistance + distance) * 100) / 100;
    user.totalSessions = (user.totalSessions || 0) + 1;
    user.longestStreak = Math.max(user.longestStreak || 0, user.currentStreak);

    await user.save();
    return user;
  } else {
    // In-memory fallback
    let user = inMemoryUsers.find((u) => u.userId === userId);
    if (!user) {
      user = {
        userId,
        username: userId,
        totalDistance: 0,
        currentStreak: 0,
        longestStreak: 0,
        lastSessionDate: null,
        totalSessions: 0,
        avatarEmoji: '🏃',
        campusBadge: 'Campus Explorer'
      };
      inMemoryUsers.push(user);
    }

    if (distance > 500) {
      if (user.lastSessionDate) {
        const hoursDiff = (now.getTime() - new Date(user.lastSessionDate).getTime()) / (1000 * 60 * 60);
        if (hoursDiff >= 24 && hoursDiff <= 48) {
          user.currentStreak += 1;
        } else if (hoursDiff < 24) {
          user.currentStreak = Math.max(user.currentStreak, 1);
        } else {
          user.currentStreak = 1;
        }
      } else {
        user.currentStreak = 1;
      }
      user.lastSessionDate = now.toISOString();
    } else {
      user.currentStreak = 1;
      if (!user.lastSessionDate) user.lastSessionDate = now.toISOString();
    }

    user.totalDistance = Math.round((user.totalDistance + distance) * 100) / 100;
    user.totalSessions = (user.totalSessions || 0) + 1;
    user.longestStreak = Math.max(user.longestStreak || 0, user.currentStreak);

    return user;
  }
}

/**
 * Controller: GET /api/v1/users/leaderboard
 * Returns top 20 users sorted by totalDistance descending
 */
exports.getLeaderboard = async (req, res) => {
  try {
    let topUsers = [];

    if (mongoose.connection.readyState === 1) {
      topUsers = await User.find()
        .sort({ totalDistance: -1 })
        .limit(20)
        .lean();
    } else {
      // Sort in-memory users by totalDistance descending
      topUsers = [...inMemoryUsers]
        .sort((a, b) => b.totalDistance - a.totalDistance)
        .slice(0, 20);
    }

    // Attach rank and display formatting
    const rankedUsers = topUsers.map((user, index) => ({
      rank: index + 1,
      userId: user.userId,
      username: user.username || user.userId,
      totalDistance: user.totalDistance || 0,
      totalDistanceKm: ((user.totalDistance || 0) / 1000).toFixed(2),
      currentStreak: user.currentStreak || 0,
      longestStreak: user.longestStreak || 0,
      totalSessions: user.totalSessions || 0,
      avatarEmoji: user.avatarEmoji || '🏃',
      campusBadge: user.campusBadge || 'Campus Explorer',
      lastSessionDate: user.lastSessionDate
    }));

    return res.status(200).json({
      success: true,
      count: rankedUsers.length,
      data: rankedUsers
    });
  } catch (error) {
    console.error('Error fetching leaderboard:', error);
    return res.status(500).json({
      success: false,
      error: 'Failed to retrieve leaderboard',
      details: error.message
    });
  }
};

/**
 * Controller: GET /api/v1/users/profile/:userId
 */
exports.getUserProfile = async (req, res) => {
  try {
    const { userId } = req.params;
    if (!userId) {
      return res.status(400).json({ success: false, error: 'userId is required' });
    }

    let user;
    if (mongoose.connection.readyState === 1) {
      user = await User.findOne({ userId });
    } else {
      user = inMemoryUsers.find((u) => u.userId === userId);
    }

    if (!user) {
      return res.status(200).json({
        success: true,
        data: {
          userId,
          username: userId,
          totalDistance: 0,
          currentStreak: 0,
          longestStreak: 0,
          totalSessions: 0
        }
      });
    }

    return res.status(200).json({
      success: true,
      data: user
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      error: 'Failed to fetch user profile',
      details: error.message
    });
  }
};

module.exports = {
  ...exports,
  updateUserGamification,
  inMemoryUsers
};
