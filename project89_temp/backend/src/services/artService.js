/**
 * Generative Art Engine
 * Converts raw GPS breadcrumb coordinates into dynamic, normalized, smoothed SVG artwork.
 */

// Rarity Palettes & Configurations
const RARITY_CONFIGS = {
  Common: {
    name: 'Common',
    primaryColor: '#10B981', // Emerald
    secondaryColor: '#34D399', // Mint
    accentGlow: '#059669',
    strokeWidth: 4,
    minDistanceMeters: 0
  },
  Uncommon: {
    name: 'Uncommon',
    primaryColor: '#06B6D4', // Cyan
    secondaryColor: '#38BDF8', // Sky Blue
    accentGlow: '#0284C7',
    strokeWidth: 5,
    minDistanceMeters: 500
  },
  Rare: {
    name: 'Rare',
    primaryColor: '#8B5CF6', // Purple
    secondaryColor: '#C084FC', // Lavender
    accentGlow: '#7C3AED',
    strokeWidth: 6,
    minDistanceMeters: 1500
  },
  Epic: {
    name: 'Epic',
    primaryColor: '#F59E0B', // Amber
    secondaryColor: '#F43F5E', // Rose / Coral
    accentGlow: '#D97706',
    strokeWidth: 6,
    minDistanceMeters: 3000
  },
  Legendary: {
    name: 'Legendary',
    primaryColor: '#FCD34D', // Gold
    secondaryColor: '#EC4899', // Holographic Pink
    accentGlow: '#F59E0B',
    strokeWidth: 7,
    minDistanceMeters: 5000
  }
};

/**
 * Determine rarity based on distance, point count, or explicit rarity override
 */
function determineRarity(distanceMeters = 0, pointCount = 0, rarityOverride = null) {
  if (rarityOverride && RARITY_CONFIGS[rarityOverride]) {
    return rarityOverride;
  }

  if (distanceMeters >= 5000 || pointCount >= 100) return 'Legendary';
  if (distanceMeters >= 3000 || pointCount >= 60) return 'Epic';
  if (distanceMeters >= 1500 || pointCount >= 35) return 'Rare';
  if (distanceMeters >= 500 || pointCount >= 15) return 'Uncommon';
  return 'Common';
}

/**
 * Normalizes raw GPS coordinate points dynamically to fit a 500x500 2D coordinate box.
 * Preserves aspect ratio and centers the path with a balanced margin.
 */
function normalizeAndScaleCoordinates(routeData, boxSize = 500, padding = 45) {
  if (!routeData || !Array.isArray(routeData) || routeData.length === 0) {
    return [];
  }

  // Filter valid coordinates
  const validPoints = routeData.filter(
    (pt) =>
      pt &&
      typeof pt.latitude === 'number' &&
      !isNaN(pt.latitude) &&
      typeof pt.longitude === 'number' &&
      !isNaN(pt.longitude)
  );

  if (validPoints.length === 0) {
    return [];
  }

  // Calculate GPS bounding box
  let minLat = Infinity;
  let maxLat = -Infinity;
  let minLng = Infinity;
  let maxLng = -Infinity;

  for (const pt of validPoints) {
    if (pt.latitude < minLat) minLat = pt.latitude;
    if (pt.latitude > maxLat) maxLat = pt.latitude;
    if (pt.longitude < minLng) minLng = pt.longitude;
    if (pt.longitude > maxLng) maxLng = pt.longitude;
  }

  const latSpan = maxLat - minLat;
  const lngSpan = maxLng - minLng;

  const usableWidth = boxSize - padding * 2;
  const usableHeight = boxSize - padding * 2;

  // Single point or zero span edge case: place at center
  if (latSpan === 0 && lngSpan === 0) {
    return [
      {
        x: Math.round(boxSize / 2),
        y: Math.round(boxSize / 2),
        original: validPoints[0]
      }
    ];
  }

  // Use dynamic max span to maintain geographic aspect ratio
  const maxSpan = Math.max(latSpan, lngSpan) || 0.0001;
  const scale = Math.min(usableWidth, usableHeight) / maxSpan;

  const renderedWidth = lngSpan * scale;
  const renderedHeight = latSpan * scale;

  const offsetX = padding + (usableWidth - renderedWidth) / 2;
  const offsetY = padding + (usableHeight - renderedHeight) / 2;

  return validPoints.map((pt) => {
    // Invert latitude: North (higher latitude) is Top (lower Y in SVG)
    const x = offsetX + (pt.longitude - minLng) * scale;
    const y = offsetY + (maxLat - pt.latitude) * scale;

    return {
      x: Math.round(x * 100) / 100,
      y: Math.round(y * 100) / 100,
      original: pt
    };
  });
}

/**
 * Smooths 2D points into an SVG Path `d` string using Catmull-Rom to Cubic Bézier spline interpolation.
 */
function pointsToSvgPath(points) {
  if (!points || points.length === 0) {
    return 'M 250 250';
  }

  if (points.length === 1) {
    // Render a small central circle/glyph path
    const { x, y } = points[0];
    return `M ${x - 5} ${y} A 5 5 0 1 0 ${x + 5} ${y} A 5 5 0 1 0 ${x - 5} ${y}`;
  }

  if (points.length === 2) {
    const p0 = points[0];
    const p1 = points[1];
    // Gentle curve between 2 points
    const midX = (p0.x + p1.x) / 2;
    const midY = (p0.y + p1.y) / 2 - 8;
    return `M ${p0.x} ${p0.y} Q ${midX} ${midY} ${p1.x} ${p1.y}`;
  }

  // Multi-point Catmull-Rom spline interpolation converted to cubic Bézier
  let path = `M ${points[0].x} ${points[0].y}`;

  const tension = 6.0; // Standard Catmull-Rom smoothing factor

  for (let i = 0; i < points.length - 1; i++) {
    const p0 = i > 0 ? points[i - 1] : points[i];
    const p1 = points[i];
    const p2 = points[i + 1];
    const p3 = i < points.length - 2 ? points[i + 2] : p2;

    const cp1x = Math.round((p1.x + (p2.x - p0.x) / tension) * 100) / 100;
    const cp1y = Math.round((p1.y + (p2.y - p0.y) / tension) * 100) / 100;

    const cp2x = Math.round((p2.x - (p3.x - p1.x) / tension) * 100) / 100;
    const cp2y = Math.round((p2.y - (p3.y - p1.y) / tension) * 100) / 100;

    path += ` C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${p2.x} ${p2.y}`;
  }

  return path;
}

/**
 * Calculates total route distance using Haversine formula in meters
 */
function calculateRouteDistance(points) {
  if (!points || points.length < 2) return 0;
  let total = 0;
  for (let i = 1; i < points.length; i++) {
    const p1 = points[i - 1];
    const p2 = points[i];
    if (p1.latitude && p1.longitude && p2.latitude && p2.longitude) {
      const R = 6371e3;
      const φ1 = (p1.latitude * Math.PI) / 180;
      const φ2 = (p2.latitude * Math.PI) / 180;
      const Δφ = ((p2.latitude - p1.latitude) * Math.PI) / 180;
      const Δλ = ((p2.longitude - p1.longitude) * Math.PI) / 180;
      const a =
        Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
        Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      total += R * c;
    }
  }
  return Math.round(total * 100) / 100;
}

/**
 * Generates the complete, self-contained SVG document markup
 */
function generateSvgDocument({
  pathData,
  points2D,
  rarityConfig,
  boxSize = 500,
  stats = {}
}) {
  const { primaryColor, secondaryColor, accentGlow, strokeWidth, name: rarityName } = rarityConfig;
  const gradientId = `grad_${Math.random().toString(36).substring(2, 9)}`;
  const glowId = `glow_${Math.random().toString(36).substring(2, 9)}`;
  const bgGradId = `bg_${Math.random().toString(36).substring(2, 9)}`;

  const startPt = points2D && points2D.length > 0 ? points2D[0] : { x: 250, y: 250 };
  const endPt = points2D && points2D.length > 0 ? points2D[points2D.length - 1] : { x: 250, y: 250 };

  return `<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${boxSize} ${boxSize}" width="100%" height="100%" shape-rendering="geometricPrecision">
  <defs>
    <!-- Background Gradient -->
    <radialGradient id="${bgGradId}" cx="50%" cy="50%" r="70%">
      <stop offset="0%" stop-color="#141B2D" />
      <stop offset="100%" stop-color="#090D16" />
    </radialGradient>

    <!-- Stroke Gradient -->
    <linearGradient id="${gradientId}" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="${primaryColor}" />
      <stop offset="50%" stop-color="${secondaryColor}" />
      <stop offset="100%" stop-color="${primaryColor}" />
    </linearGradient>

    <!-- Neon Glow Filter -->
    <filter id="${glowId}" x="-20%" y="-20%" width="140%" height="140%">
      <feGaussianBlur stdDeviation="6" result="blur1" />
      <feGaussianBlur stdDeviation="12" result="blur2" />
      <feMerge>
        <feMergeNode in="blur2" />
        <feMergeNode in="blur1" />
        <feMergeNode in="SourceGraphic" />
      </feMerge>
    </filter>
  </defs>

  <!-- Canvas Card Background -->
  <rect width="${boxSize}" height="${boxSize}" rx="24" fill="url(#${bgGradId})" stroke="#1F293D" stroke-width="1.5" />

  <!-- Ambient Grid & Decorative Radar Aesthetics -->
  <g opacity="0.08" stroke="#FFFFFF" stroke-width="1">
    <line x1="50" y1="50" x2="450" y2="50" stroke-dasharray="4 8" />
    <line x1="50" y1="250" x2="450" y2="250" stroke-dasharray="4 8" />
    <line x1="50" y1="450" x2="450" y2="450" stroke-dasharray="4 8" />
    <line x1="50" y1="50" x2="50" y2="450" stroke-dasharray="4 8" />
    <line x1="250" y1="50" x2="250" y2="450" stroke-dasharray="4 8" />
    <line x1="450" y1="50" x2="450" y2="450" stroke-dasharray="4 8" />
    <circle cx="250" cy="250" r="180" fill="none" stroke-dasharray="2 10" />
  </g>

  <!-- Ambient Glow Underlay Path -->
  <path
    d="${pathData}"
    fill="none"
    stroke="${accentGlow}"
    stroke-width="${strokeWidth + 4}"
    stroke-linecap="round"
    stroke-linejoin="round"
    opacity="0.3"
    filter="url(#${glowId})"
  />

  <!-- Main Smoothed Generative Route Path -->
  <path
    d="${pathData}"
    fill="none"
    stroke="url(#${gradientId})"
    stroke-width="${strokeWidth}"
    stroke-linecap="round"
    stroke-linejoin="round"
  />

  <!-- Start Waypoint Beacon (Origin) -->
  <g transform="translate(${startPt.x}, ${startPt.y})">
    <circle r="7" fill="#10B981" opacity="0.25" />
    <circle r="4" fill="#10B981" />
    <circle r="1.5" fill="#FFFFFF" />
  </g>

  <!-- End Waypoint Beacon (Destination) -->
  <g transform="translate(${endPt.x}, ${endPt.y})">
    <circle r="8" fill="${secondaryColor}" opacity="0.35" />
    <circle r="4.5" fill="${secondaryColor}" />
    <circle r="2" fill="#FFFFFF" />
  </g>

  <!-- Subtle Rarity Branding Watermark -->
  <text x="24" y="${boxSize - 20}" fill="#475569" font-size="10" font-family="sans-serif" font-weight="600" letter-spacing="1">
    CAMPUS ART • ${rarityName.toUpperCase()}
  </text>
  <text x="${boxSize - 24}" y="${boxSize - 20}" text-anchor="end" fill="#475569" font-size="10" font-family="sans-serif" font-weight="500">
    ${stats.totalDistanceMeters || 0}m • ${stats.pointCount || 0} pts
  </text>
</svg>`;
}

/**
 * Main Art Service Entry Point
 * Takes a raw routeData array and optional metadata, and generates the complete Artwork payload.
 */
function generateArtwork({
  routeData = [],
  userId = 'user_anonymous',
  sessionId = null,
  rarityOverride = null,
  durationSeconds = 0
}) {
  const points = Array.isArray(routeData) ? routeData : [];
  const distance = calculateRouteDistance(points);
  const rarity = determineRarity(distance, points.length, rarityOverride);
  const rarityConfig = RARITY_CONFIGS[rarity] || RARITY_CONFIGS.Common;

  const points2D = normalizeAndScaleCoordinates(points, 500, 45);
  const pathData = pointsToSvgPath(points2D);

  const stats = {
    totalDistanceMeters: distance,
    pointCount: points.length,
    durationSeconds: durationSeconds || 0
  };

  const svgData = generateSvgDocument({
    pathData,
    points2D,
    rarityConfig,
    boxSize: 500,
    stats
  });

  return {
    userId,
    sessionId,
    svgData,
    pathData,
    rarity,
    stats,
    theme: {
      primaryColor: rarityConfig.primaryColor,
      secondaryColor: rarityConfig.secondaryColor,
      strokeWidth: rarityConfig.strokeWidth
    }
  };
}

module.exports = {
  generateArtwork,
  normalizeAndScaleCoordinates,
  pointsToSvgPath,
  determineRarity,
  calculateRouteDistance,
  generateSvgDocument,
  RARITY_CONFIGS
};
