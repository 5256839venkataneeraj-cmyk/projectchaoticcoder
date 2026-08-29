/**
 * Controller for testing backend health and basic info
 */
exports.getTestStatus = (req, res) => {
  res.status(200).json({
    success: true,
    message: "Campus Route-to-Art backend API is running smoothly!",
    timestamp: new Date().toISOString(),
    service: "campus-route-to-art-api",
    version: "1.0.0"
  });
};
