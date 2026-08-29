/**
 * Campus Route-to-Art Database Seeder
 * Populates 10 competitive campus creators & runners, their associated walk sessions,
 * and generative SVG artworks into MongoDB.
 */
const mongoose = require('mongoose');
const dotenv = require('dotenv');
const path = require('path');

dotenv.config({ path: path.join(__dirname, '../../.env') });

const User = require('../models/User');
const WalkSession = require('../models/WalkSession');
const Artwork = require('../models/Artwork');
const { generateArtwork } = require('../services/artService');

const SEED_USERS = [
  {
    userId: 'marathon_maya',
    username: 'Maya Lin (Design Major)',
    totalDistance: 34250,
    currentStreak: 14,
    longestStreak: 16,
    lastSessionDate: new Date(Date.now() - 3600000 * 20),
    totalSessions: 22,
    avatarEmoji: '⚡',
    campusBadge: 'Legendary Navigator'
  },
  {
    userId: 'quad_runner_leo',
    username: 'Leo Rodriguez (Robotics)',
    totalDistance: 28500,
    currentStreak: 10,
    longestStreak: 12,
    lastSessionDate: new Date(Date.now() - 3600000 * 26),
    totalSessions: 18,
    avatarEmoji: '🚀',
    campusBadge: 'Trail Pioneer'
  },
  {
    userId: 'sculpture_sam',
    username: 'Samira Patel (Architecture)',
    totalDistance: 23100,
    currentStreak: 8,
    longestStreak: 10,
    lastSessionDate: new Date(Date.now() - 3600000 * 30),
    totalSessions: 15,
    avatarEmoji: '🎨',
    campusBadge: 'Artisan Strider'
  },
  {
    userId: 'tech_trekker',
    username: 'Marcus Chen (AI & Data)',
    totalDistance: 19450,
    currentStreak: 7,
    longestStreak: 9,
    lastSessionDate: new Date(Date.now() - 3600000 * 18),
    totalSessions: 13,
    avatarEmoji: '💻',
    campusBadge: 'Circuit Explorer'
  },
  {
    userId: 'campus_art_fan',
    username: 'Elena Rostova (Visual Arts)',
    totalDistance: 16800,
    currentStreak: 6,
    longestStreak: 8,
    lastSessionDate: new Date(Date.now() - 3600000 * 22),
    totalSessions: 11,
    avatarEmoji: '✨',
    campusBadge: 'Gallery Curator'
  },
  {
    userId: 'speedy_sarah',
    username: 'Sarah Jenkins (Varsity Track)',
    totalDistance: 14200,
    currentStreak: 5,
    longestStreak: 7,
    lastSessionDate: new Date(Date.now() - 3600000 * 12),
    totalSessions: 9,
    avatarEmoji: '🏃‍♀️',
    campusBadge: 'Pace Setter'
  },
  {
    userId: 'bio_runner_alex',
    username: 'Alex Rivera (Bio-Engineering)',
    totalDistance: 11500,
    currentStreak: 4,
    longestStreak: 6,
    lastSessionDate: new Date(Date.now() - 3600000 * 25),
    totalSessions: 8,
    avatarEmoji: '🧬',
    campusBadge: 'Eco Navigator'
  },
  {
    userId: 'media_jordan',
    username: 'Jordan Taylor (Digital Media)',
    totalDistance: 9800,
    currentStreak: 3,
    longestStreak: 5,
    lastSessionDate: new Date(Date.now() - 3600000 * 32),
    totalSessions: 7,
    avatarEmoji: '📸',
    campusBadge: 'Creative Nomad'
  },
  {
    userId: 'urban_kai',
    username: 'Kai Tanaka (Urban Planning)',
    totalDistance: 7500,
    currentStreak: 2,
    longestStreak: 4,
    lastSessionDate: new Date(Date.now() - 3600000 * 28),
    totalSessions: 6,
    avatarEmoji: '🧭',
    campusBadge: 'City Grid Master'
  },
  {
    userId: 'student_creator',
    username: 'You (Campus Creator)',
    totalDistance: 5200,
    currentStreak: 2,
    longestStreak: 4,
    lastSessionDate: new Date(Date.now() - 3600000 * 25),
    totalSessions: 5,
    avatarEmoji: '🌟',
    campusBadge: 'Rising Artist'
  }
];

// Helper to create synthetic campus GPS walk paths
function generateMockRoute(centerLat = 12.9716, centerLng = 77.5945, pointCount = 12, radius = 0.003) {
  const points = [];
  for (let i = 0; i < pointCount; i++) {
    const angle = (i / pointCount) * Math.PI * 2;
    const r = radius * (0.8 + 0.4 * Math.sin(angle * 3));
    points.push({
      latitude: centerLat + r * Math.cos(angle),
      longitude: centerLng + r * Math.sin(angle) * 1.2,
      timestamp: Date.now() - (pointCount - i) * 60000
    });
  }
  return points;
}

async function seedDatabase() {
  const MONGO_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/campus_art_db';
  console.log('🌱 Starting Campus Route-to-Art Seeder...');

  let isConnected = false;
  try {
    await mongoose.connect(MONGO_URI, { serverSelectionTimeoutMS: 2500 });
    isConnected = true;
    console.log('✅ Connected to MongoDB at:', MONGO_URI);
  } catch (err) {
    console.log(`ℹ️ MongoDB connection not available (${err.message}).`);
    console.log('ℹ️ Seeding in-memory datasets for offline & demo execution.');
  }

  try {
    if (isConnected) {
      console.log('📦 Injecting 10 competitive campus creators and walk sessions...');

      let userCount = 0;
      let sessionCount = 0;
      let artworkCount = 0;

      for (let i = 0; i < SEED_USERS.length; i++) {
        const userData = SEED_USERS[i];

        // 1. Seed or update User document
        const userDoc = await User.findOneAndUpdate(
          { userId: userData.userId },
          { $set: userData },
          { upsert: true, new: true }
        );
        userCount++;

        // 2. Seed associated WalkSession document
        const mockCoords = generateMockRoute(
          12.9716 + (i * 0.001 - 0.005),
          77.5945 + (i * 0.001 - 0.005),
          12 + i * 2,
          0.002 + (userData.totalDistance / 1000000)
        );

        const walkSessionDoc = new WalkSession({
          userId: userData.userId,
          startTime: new Date(Date.now() - 3600000 * (i + 1) * 4),
          endTime: new Date(Date.now() - 3600000 * (i + 1) * 4 + 1800000),
          routeData: mockCoords,
          status: 'completed',
          notes: `Campus Exploration Walk by ${userData.username}`
        });

        const savedSession = await walkSessionDoc.save();
        sessionCount++;

        // 3. Generate and seed Artwork document
        const artPayload = generateArtwork({
          routeData: mockCoords,
          userId: userData.userId,
          sessionId: savedSession._id,
          durationSeconds: 1800
        });

        const artworkDoc = new Artwork(artPayload);
        await artworkDoc.save();
        artworkCount++;
      }

      console.log(`\n🎉 SEEDING COMPLETE!`);
      console.log(`   ✅ Users injected:       ${userCount}`);
      console.log(`   ✅ WalkSessions created: ${sessionCount}`);
      console.log(`   ✅ Artworks minted:      ${artworkCount}`);
    } else {
      console.log(`\n🎉 IN-MEMORY SEEDING COMPLETE!`);
      console.log(`   ✅ 10 Mock Users ready in leaderboard dataset.`);
      console.log(`   Top Leaderboard Leader: ${SEED_USERS[0].username} (${SEED_USERS[0].totalDistance / 1000} km)`);
    }
  } catch (error) {
    console.error('❌ Seeder execution error:', error);
  } finally {
    if (mongoose.connection.readyState === 1) {
      await mongoose.disconnect();
      console.log('🔒 MongoDB disconnected cleanly.');
    }
  }
}

if (require.main === module) {
  seedDatabase();
}

module.exports = {
  SEED_USERS,
  seedDatabase
};
