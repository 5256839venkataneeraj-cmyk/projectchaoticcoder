/**
 * db.js - Firestore Database initialization & Schema Helpers
 */
const admin = require('firebase-admin');
require('dotenv').config();

// Initialize Firebase Admin SDK with credentials or placeholder configuration
if (!admin.apps.length) {
  try {
    if (process.env.FIREBASE_SERVICE_ACCOUNT_KEY) {
      const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT_KEY);
      admin.initializeApp({
        credential: admin.credential.cert(serviceAccount),
        storageBucket: process.env.FIREBASE_STORAGE_BUCKET || 'campus-route-art.appspot.com'
      });
    } else {
      // Fallback placeholder configuration for local development / testing
      admin.initializeApp({
        projectId: process.env.FIREBASE_PROJECT_ID || 'campus-route-art-demo',
        storageBucket: 'campus-route-art-demo.appspot.com'
      });
    }
  } catch (error) {
    console.warn('Firebase Admin initialization warning:', error.message);
  }
}

const db = admin.firestore();
const bucket = admin.storage().bucket();

// Collection References
const Collections = {
  USERS: 'users',
  WALK_SESSIONS: 'walk_sessions',
  ARTWORKS: 'artworks'
};

/**
 * Firestore Schema Definitions:
 * 
 * Users Schema:
 * {
 *   uid: string,
 *   name: string,
 *   avatar: string,
 *   department: string,
 *   dorm: string,
 *   totalDistance: number, // in km
 *   totalSteps: number,
 *   currentStreak: number, // in consecutive days
 *   lastWalkDate: string (ISO),
 *   createdAt: timestamp
 * }
 * 
 * WalkSessions Schema:
 * {
 *   id: string,
 *   userId: string,
 *   coordinates: [{ lat: number, lon: number, timestamp: number }],
 *   distanceKm: number,
 *   stepCount: number,
 *   durationSeconds: number,
 *   createdAt: timestamp
 * }
 * 
 * Artworks Schema:
 * {
 *   id: string,
 *   userId: string,
 *   sessionId: string,
 *   title: string,
 *   svgString: string,
 *   svgUrl: string,
 *   streakTier: string, // "STANDARD" | "NEON_CYBER" | "CELESTIAL_GOLD"
 *   stats: { distanceKm: number, steps: number, durationMinutes: number },
 *   createdAt: timestamp
 * }
 */

module.exports = {
  admin,
  db,
  bucket,
  Collections
};
