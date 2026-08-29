const express = require('express');
const router = express.Router();
const { getTestStatus } = require('../controllers/testController');

// Test endpoint
router.get('/test', getTestStatus);

module.exports = router;
