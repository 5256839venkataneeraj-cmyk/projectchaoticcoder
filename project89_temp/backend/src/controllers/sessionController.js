const WalkSession = require('../models/WalkSession');
const mongoose = require('mongoose');
const { generateArtwork, calculateRouteDistance } = require('../services/artService');
const { saveArtworkRecord } = require('./artController');
const { updateUserGamification } = require('./userController');

// In-memory fallback storage in case MongoDB is not currently connected in local dev
const inMemorySessions = [];

/**
 * Controller to upload and save a completed walk session,
 * automatically mint generative SVG artwork from the route,
 * and update user gamification streak and distance.
 * POST /api/v1/sessions/upload
 */
exports.uploadSession = async (req, res) => {
  try {
    const { userId, startTime, endTime, routeData, notes, rarityOverride } = req.body;

    // Validation
    if (!routeData || !Array.isArray(routeData)) {
      return res.status(400).json({
        success: false,
        error: 'Invalid payload: routeData must be an array of GPS coordinate objects'
      });
    }

    const start = startTime ? new Date(startTime) : new Date();
    const end = endTime ? new Date(endTime) : new Date();
    const durationSeconds = Math.max(0, Math.round((end.getTime() - start.getTime()) / 1000));

    const sessionPayload = {
      userId: userId || 'user_anonymous',
      startTime: start,
      endTime: end,
      routeData: routeData.map((pt) => ({
        latitude: pt.latitude,
        longitude: pt.longitude,
        timestamp: typeof pt.timestamp === 'number' ? pt.timestamp : Date.now(),
        altitude: pt.altitude || null,
        accuracy: pt.accuracy || null,
        speed: pt.speed || null,
        heading: pt.heading || null
      })),
      notes: notes || ''
    };

    let savedSession;

    // Check if MongoDB connection is open and active
    if (mongoose.connection.readyState === 1) {
      const walkSessionDoc = new WalkSession(sessionPayload);
      savedSession = await walkSessionDoc.save();
    } else {
      // In-memory fallback
      const mockId = new mongoose.Types.ObjectId().toString();
      const calculatedDist = calculateRouteDistance(sessionPayload.routeData);
      savedSession = {
        _id: mockId,
        ...sessionPayload,
        pointCount: sessionPayload.routeData.length,
        totalDistanceMeters: calculatedDist,
        status: 'completed',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      inMemorySessions.push(savedSession);
      console.log(`ℹ️ Saved session ${mockId} to in-memory store (${sessionPayload.routeData.length} coordinates, ${calculatedDist}m)`);
    }

    // Automatically trigger Generative Art Engine
    const sessionId = savedSession._id ? savedSession._id.toString() : savedSession.id;
    const generatedArtPayload = generateArtwork({
      routeData: sessionPayload.routeData,
      userId: sessionPayload.userId,
      sessionId: sessionId,
      rarityOverride: rarityOverride || null,
      durationSeconds
    });

    // Save Artwork record
    const savedArtwork = await saveArtworkRecord(generatedArtPayload);

    // Gamification Engine: Calculate distance and update streak & totalDistance
    const sessionDistance =
      savedSession.totalDistanceMeters ||
      generatedArtPayload.stats?.totalDistanceMeters ||
      calculateRouteDistance(sessionPayload.routeData);

    const userGamification = await updateUserGamification(
      sessionPayload.userId,
      sessionDistance,
      sessionPayload.endTime
    );

    const sessionObj = savedSession.toObject ? savedSession.toObject() : { ...savedSession };

    return res.status(201).json({
      success: true,
      message: 'Walk session saved, generative artwork minted, and gamification updated',
      data: {
        ...sessionObj,
        artwork: savedArtwork,
        svgData: savedArtwork.svgData,
        rarity: savedArtwork.rarity,
        gamification: {
          totalDistance: userGamification.totalDistance,
          currentStreak: userGamification.currentStreak,
          longestStreak: userGamification.longestStreak,
          totalSessions: userGamification.totalSessions
        }
      },
      artwork: savedArtwork,
      user: userGamification
    });
  } catch (error) {
    console.error('Error saving walk session & updating gamification:', error);
    return res.status(500).json({
      success: false,
      error: 'Failed to save walk session and update gamification',
      details: error.message
    });
  }
};

/**
 * Controller to get all walk sessions
 * GET /api/v1/sessions
 */
exports.getAllSessions = async (req, res) => {
  try {
    if (mongoose.connection.readyState === 1) {
      const sessions = await WalkSession.find().sort({ createdAt: -1 }).limit(50);
      return res.status(200).json({
        success: true,
        count: sessions.length,
        data: sessions
      });
    } else {
      return res.status(200).json({
        success: true,
        count: inMemorySessions.length,
        data: inMemorySessions
      });
    }
  } catch (error) {
    return res.status(500).json({
      success: false,
      error: 'Failed to retrieve sessions',
      details: error.message
    });
  }
};

/**
 * Controller to get a specific session by ID
 * GET /api/v1/sessions/:id
 */
exports.getSessionById = async (req, res) => {
  try {
    const { id } = req.params;

    if (mongoose.connection.readyState === 1) {
      const session = await WalkSession.findById(id);
      if (!session) {
        return res.status(404).json({ success: false, error: 'Session not found' });
      }
      return res.status(200).json({ success: true, data: session });
    } else {
      const session = inMemorySessions.find((s) => s._id === id);
      if (!session) {
        return res.status(404).json({ success: false, error: 'Session not found' });
      }
      return res.status(200).json({ success: true, data: session });
    }
  } catch (error) {
    return res.status(500).json({
      success: false,
      error: 'Failed to retrieve session',
      details: error.message
    });
  }
};

module.exports = {
  ...exports,
  inMemorySessions
};
