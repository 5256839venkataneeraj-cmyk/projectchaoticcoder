const express = require('express');
const router = express.Router();
const {
  getUserArtworks,
  getArtworkById,
  generateArtPreview
} = require('../controllers/artController');

/**
 * Route: GET /api/v1/artworks/gallery/:userId
 * Description: Retrieve all generated generative artworks for a user sorted by newest first
 */
router.get('/gallery/:userId', getUserArtworks);

/**
 * Route: POST /api/v1/artworks/generate
 * Description: Generate on-the-fly artwork preview from coordinates
 */
router.post('/generate', generateArtPreview);

/**
 * Route: GET /api/v1/artworks/:id
 * Description: Retrieve single artwork by ID
 */
router.get('/:id', getArtworkById);

module.exports = router;
