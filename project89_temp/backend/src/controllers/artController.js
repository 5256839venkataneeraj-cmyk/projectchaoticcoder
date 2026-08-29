const Artwork = require('../models/Artwork');
const mongoose = require('mongoose');
const { generateArtwork } = require('../services/artService');

// In-memory fallback storage for artworks when MongoDB is running in offline/memory mode
const inMemoryArtworks = [];

/**
 * Helper to save an artwork document or push to in-memory fallback
 */
async function saveArtworkRecord(artworkPayload) {
  if (mongoose.connection.readyState === 1) {
    const artworkDoc = new Artwork(artworkPayload);
    return await artworkDoc.save();
  } else {
    const mockId = new mongoose.Types.ObjectId().toString();
    const saved = {
      _id: mockId,
      ...artworkPayload,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    inMemoryArtworks.unshift(saved); // newest first
    return saved;
  }
}

/**
 * Controller: GET /api/v1/artworks/gallery/:userId
 * Retrieves all generated artworks for a specific user, sorted newest first
 */
exports.getUserArtworks = async (req, res) => {
  try {
    const { userId } = req.params;
    const { rarity } = req.query;

    if (!userId) {
      return res.status(400).json({
        success: false,
        error: 'userId parameter is required'
      });
    }

    if (mongoose.connection.readyState === 1) {
      const query = { userId };
      if (rarity) {
        query.rarity = rarity;
      }
      const artworks = await Artwork.find(query).sort({ createdAt: -1 });

      return res.status(200).json({
        success: true,
        count: artworks.length,
        data: artworks
      });
    } else {
      let userArtworks = inMemoryArtworks.filter((art) => art.userId === userId);
      if (rarity) {
        userArtworks = userArtworks.filter((art) => art.rarity === rarity);
      }
      // Sort newest first
      userArtworks.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

      return res.status(200).json({
        success: true,
        count: userArtworks.length,
        data: userArtworks
      });
    }
  } catch (error) {
    console.error('Error fetching user gallery:', error);
    return res.status(500).json({
      success: false,
      error: 'Failed to retrieve user gallery',
      details: error.message
    });
  }
};

/**
 * Controller: GET /api/v1/artworks/:id
 * Retrieves a single artwork by its ID
 */
exports.getArtworkById = async (req, res) => {
  try {
    const { id } = req.params;

    if (mongoose.connection.readyState === 1) {
      const artwork = await Artwork.findById(id);
      if (!artwork) {
        return res.status(404).json({ success: false, error: 'Artwork not found' });
      }
      return res.status(200).json({ success: true, data: artwork });
    } else {
      const artwork = inMemoryArtworks.find((art) => art._id === id || art.id === id);
      if (!artwork) {
        return res.status(404).json({ success: false, error: 'Artwork not found' });
      }
      return res.status(200).json({ success: true, data: artwork });
    }
  } catch (error) {
    console.error('Error fetching artwork by ID:', error);
    return res.status(500).json({
      success: false,
      error: 'Failed to retrieve artwork',
      details: error.message
    });
  }
};

/**
 * Controller: POST /api/v1/artworks/generate
 * Generates an SVG artwork preview directly from raw coordinate data without persisting
 */
exports.generateArtPreview = async (req, res) => {
  try {
    const { routeData, userId, rarityOverride } = req.body;

    if (!routeData || !Array.isArray(routeData)) {
      return res.status(400).json({
        success: false,
        error: 'Invalid payload: routeData array is required'
      });
    }

    const generated = generateArtwork({
      routeData,
      userId: userId || 'user_anonymous',
      rarityOverride
    });

    return res.status(200).json({
      success: true,
      message: 'Artwork preview generated successfully',
      data: generated
    });
  } catch (error) {
    console.error('Error generating artwork preview:', error);
    return res.status(500).json({
      success: false,
      error: 'Failed to generate artwork preview',
      details: error.message
    });
  }
};

module.exports = {
  ...exports,
  inMemoryArtworks,
  saveArtworkRecord
};
