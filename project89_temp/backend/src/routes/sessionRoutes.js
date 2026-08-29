const express = require('express');
const router = express.Router();
const {
  uploadSession,
  getAllSessions,
  getSessionById
} = require('../controllers/sessionController');

/**
 * Route: POST /api/v1/sessions/upload
 * Description: Ingest real-time GPS coordinate breadcrumb arrays from mobile walks
 */
router.post('/upload', uploadSession);

/**
 * Route: GET /api/v1/sessions
 * Description: Retrieve past walk sessions
 */
router.get('/', getAllSessions);

/**
 * Route: GET /api/v1/sessions/:id
 * Description: Retrieve single session with full polyline coordinates
 */
router.get('/:id', getSessionById);

module.exports = router;
