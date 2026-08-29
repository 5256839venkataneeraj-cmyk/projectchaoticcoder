/**
 * sessionController.js - Handles GPS uploads, artwork creation, and leaderboard
 */
const { db, Collections } = require('../db');
const artService = require('../services/artService');

/**
 * POST /api/sessions/upload
 * Process GPS coordinates, generate SVG art, update user stats & streaks
 */
async function uploadWalkSession(req, res) {
  try {
    const {
      userId = 'demo_student_user',
      coordinates = [],
      distanceKm = 0,
      stepCount = 0,
      durationSeconds = 0,
      title = 'Campus Route Stride'
    } = req.body;

    if (!Array.isArray(coordinates) || coordinates.length === 0) {
      return res.status(400).json({
        success: false,
        error: 'coordinates array is required and must not be empty'
      });
    }

    // 1. Fetch user to check streak and increment stats
    let userStreak = 1;
    let userDocRef = db.collection(Collections.USERS).doc(userId);
    let userDoc = null;

    try {
      userDoc = await userDocRef.get();
      if (userDoc.exists) {
        const userData = userDoc.data();
        const lastWalk = userData.lastWalkDate ? new Date(userData.lastWalkDate) : null;
        const now = new Date();

        if (lastWalk) {
          const diffDays = Math.floor((now - lastWalk) / (1000 * 60 * 60 * 24));
          if (diffDays === 1) {
            userStreak = (userData.currentStreak || 0) + 1;
          } else if (diffDays === 0) {
            userStreak = userData.currentStreak || 1;
          } else {
            userStreak = 1; // Streak reset
          }
        }
      }
    } catch (e) {
      console.warn('User lookup warning:', e.message);
    }

    // 2. Generate generative SVG Art using Bezier smoothing & streak styling
    const artResult = artService.generateRouteSvg(coordinates, userStreak, title);
    const artworkId = `art_${Date.now()}_${Math.random().toString(36).substr(2, 6)}`;
    const sessionId = `walk_${Date.now()}`;

    const svgUrl = await artService.uploadSvgArt(artResult.svgString, artworkId);

    const artworkRecord = {
      id: artworkId,
      userId,
      sessionId,
      title: `${title} (${artResult.visuals.titleSuffix})`,
      svgString: artResult.svgString,
      svgUrl: svgUrl || '',
      streakTier: artResult.visuals.tier,
      streakCount: userStreak,
      stats: {
        distanceKm: Number(distanceKm) || 0,
        steps: Number(stepCount) || 0,
        durationMinutes: Math.round(durationSeconds / 60) || 1
      },
      createdAt: new Date().toISOString()
    };

    const sessionRecord = {
      id: sessionId,
      userId,
      coordinatesCount: coordinates.length,
      distanceKm: Number(distanceKm),
      stepCount: Number(stepCount),
      durationSeconds: Number(durationSeconds),
      artworkId,
      createdAt: new Date().toISOString()
    };

    // 3. Persist to Firestore if available
    try {
      await db.collection(Collections.WALK_SESSIONS).doc(sessionId).set(sessionRecord);
      await db.collection(Collections.ARTWORKS).doc(artworkId).set(artworkRecord);

      const updatedData = {
        uid: userId,
        lastWalkDate: new Date().toISOString(),
        currentStreak: userStreak
      };

      if (userDoc && userDoc.exists) {
        const currentData = userDoc.data();
        updatedData.totalDistance = Number(((currentData.totalDistance || 0) + Number(distanceKm)).toFixed(2));
        updatedData.totalSteps = (currentData.totalSteps || 0) + Number(stepCount);
      } else {
        updatedData.name = 'Campus Explorer';
        updatedData.avatar = '🧑‍🎨';
        updatedData.department = 'Computer Science';
        updatedData.dorm = 'Block D Dragons';
        updatedData.totalDistance = Number(distanceKm) || 0;
        updatedData.totalSteps = Number(stepCount) || 0;
      }

      await userDocRef.set(updatedData, { merge: true });
    } catch (e) {
      console.warn('Firestore write fallback:', e.message);
    }

    return res.status(200).json({
      success: true,
      message: 'Walk session processed and generative artwork created!',
      artwork: artworkRecord,
      session: sessionRecord
    });
  } catch (error) {
    console.error('Error in uploadWalkSession:', error);
    return res.status(500).json({
      success: false,
      error: error.message || 'Internal server error while generating artwork'
    });
  }
}

/**
 * GET /api/artworks/user/:userId
 */
async function getUserArtworks(req, res) {
  try {
    const { userId } = req.params;
    let artworks = [];

    try {
      const snapshot = await db.collection(Collections.ARTWORKS)
        .where('userId', '==', userId)
        .orderBy('createdAt', 'desc')
        .get();

      artworks = snapshot.docs.map(doc => doc.data());
    } catch (e) {
      console.warn('Firestore query fallback:', e.message);
    }

    // Fallback seed demo art if empty
    if (artworks.length === 0) {
      const sampleCoords = [
        { lat: 12.9692, lon: 79.1559, timestamp: Date.now() - 3600000 },
        { lat: 12.9710, lon: 79.1585, timestamp: Date.now() - 2700000 },
        { lat: 12.9735, lon: 79.1601, timestamp: Date.now() - 1800000 },
        { lat: 12.9720, lon: 79.1570, timestamp: Date.now() - 900000 },
        { lat: 12.9692, lon: 79.1559, timestamp: Date.now() }
      ];
      const demoArt = artService.generateRouteSvg(sampleCoords, 5, "Today's Campus Bloom");
      artworks.push({
        id: 'art_demo_sample_1',
        userId,
        title: "Today's Campus Bloom (Cyber Neon)",
        svgString: demoArt.svgString,
        svgUrl: 'https://storage.googleapis.com/campus-route-art.appspot.com/artworks/demo1.svg',
        streakTier: 'NEON_CYBER',
        streakCount: 5,
        stats: { distanceKm: 5.6, steps: 7842, durationMinutes: 48 },
        createdAt: new Date().toISOString()
      });
    }

    return res.status(200).json({
      success: true,
      count: artworks.length,
      artworks
    });
  } catch (error) {
    return res.status(500).json({ success: false, error: error.message });
  }
}

/**
 * GET /api/leaderboard
 */
async function getLeaderboard(req, res) {
  try {
    const { metric = 'totalDistance' } = req.query;
    let users = [];

    try {
      const snapshot = await db.collection(Collections.USERS)
        .orderBy(metric, 'desc')
        .limit(20)
        .get();

      users = snapshot.docs.map((doc, idx) => ({
        rank: idx + 1,
        ...doc.data()
      }));
    } catch (e) {
      console.warn('Firestore query fallback:', e.message);
    }

    // Default high-performance list if empty or local
    if (users.length === 0) {
      users = [
        { rank: 1, name: 'Block D Dragons', dorm: 'Hostel Wing', department: '412 Artists', totalDistance: 148.9, totalSteps: 198400, currentStreak: 14, avatar: '🐉' },
        { rank: 2, name: 'Diya Patel', dorm: 'Ladies Hostel', department: 'Design Dept', totalDistance: 84.2, totalSteps: 112300, currentStreak: 9, avatar: '👩‍🎨' },
        { rank: 3, name: 'Block A Titans', dorm: 'Hostel Wing', department: '380 Artists', totalDistance: 78.5, totalSteps: 104200, currentStreak: 8, avatar: '⚡' },
        { rank: 4, name: 'Aarav Sharma', dorm: 'Block B', department: 'CS Dept', totalDistance: 64.8, totalSteps: 86400, currentStreak: 5, avatar: '🧑‍💻' },
        { rank: 5, name: 'Rohan Verma', dorm: 'Block C', department: 'Mech Dept', totalDistance: 52.1, totalSteps: 69500, currentStreak: 3, avatar: '🏃' }
      ];
    }

    return res.status(200).json({
      success: true,
      leaderboard: users
    });
  } catch (error) {
    return res.status(500).json({ success: false, error: error.message });
  }
}

module.exports = {
  uploadWalkSession,
  getUserArtworks,
  getLeaderboard
};
