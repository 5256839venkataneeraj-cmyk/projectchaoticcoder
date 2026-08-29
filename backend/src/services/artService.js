/**
 * artService.js - Algorithmic Generative SVG Art Engine
 */

/**
 * Normalizes raw GPS coordinates [{ lat, lon, timestamp }] to 500x500 canvas coordinates
 * @param {Array<{lat: number, lon: number, timestamp?: number}>} coordinates 
 * @param {number} size Canvas width/height (default 500)
 * @param {number} padding Canvas margin padding (default 40)
 * @returns {Array<{x: number, y: number}>}
 */
function normalizeCoordinates(coordinates, size = 500, padding = 40) {
  if (!coordinates || coordinates.length === 0) {
    return [];
  }

  // Find geospatial bounding box
  let minLat = Infinity, maxLat = -Infinity;
  let minLon = Infinity, maxLon = -Infinity;

  for (const pt of coordinates) {
    const lat = Number(pt.lat);
    const lon = Number(pt.lon);
    if (lat < minLat) minLat = lat;
    if (lat > maxLat) maxLat = lat;
    if (lon < minLon) minLon = lon;
    if (lon > maxLon) maxLon = lon;
  }

  const latSpan = Math.max(maxLat - minLat, 0.00001);
  const lonSpan = Math.max(maxLon - minLon, 0.00001);
  const maxSpan = Math.max(latSpan, lonSpan);

  const availableDim = size - padding * 2;
  const scale = availableDim / maxSpan;

  const centerLat = (minLat + maxLat) / 2;
  const centerLon = (minLon + maxLon) / 2;
  const centerCanvas = size / 2;

  return coordinates.map(pt => {
    const lat = Number(pt.lat);
    const lon = Number(pt.lon);
    // Invert latitude so North is top (-Y)
    const x = centerCanvas + (lon - centerLon) * scale;
    const y = centerCanvas - (lat - centerLat) * scale;
    return {
      x: Number(x.toFixed(2)),
      y: Number(y.toFixed(2))
    };
  });
}

/**
 * Builds smooth Cubic Bezier SVG path data from 2D points (Catmull-Rom to Cubic Bezier)
 * @param {Array<{x: number, y: number}>} points 
 * @returns {string} SVG path 'd' attribute
 */
function generateBezierPathData(points) {
  if (!points || points.length === 0) return '';
  if (points.length === 1) return `M ${points[0].x} ${points[0].y}`;

  let d = `M ${points[0].x} ${points[0].y}`;

  for (let i = 0; i < points.length - 1; i++) {
    const p0 = i > 0 ? points[i - 1] : points[i];
    const p1 = points[i];
    const p2 = points[i + 1];
    const p3 = i + 2 < points.length ? points[i + 2] : p2;

    // Catmull-Rom spline control points
    const cp1x = (p1.x + (p2.x - p0.x) / 6).toFixed(2);
    const cp1y = (p1.y + (p2.y - p0.y) / 6).toFixed(2);
    const cp2x = (p2.x - (p3.x - p1.x) / 6).toFixed(2);
    const cp2y = (p2.y - (p3.y - p1.y) / 6).toFixed(2);

    d += ` C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${p2.x} ${p2.y}`;
  }

  return d;
}

/**
 * Calculates dynamic palette and effects based on current streak
 * @param {number} streak 
 */
function getStreakVisuals(streak = 1) {
  if (streak >= 7) {
    return {
      tier: 'CELESTIAL_GOLD',
      primaryColor: '#FFD700',
      secondaryColor: '#FF007F',
      glowColor: '#FF0055',
      strokeWidth: 4.5,
      filterId: 'goldGlow',
      titleSuffix: 'Celestial Gold'
    };
  } else if (streak >= 3) {
    return {
      tier: 'NEON_CYBER',
      primaryColor: '#00F5D4',
      secondaryColor: '#7B2CBF',
      glowColor: '#00F5D4',
      strokeWidth: 4.0,
      filterId: 'cyberGlow',
      titleSuffix: 'Cyber Neon'
    };
  } else {
    return {
      tier: 'STANDARD',
      primaryColor: '#2A9D8F',
      secondaryColor: '#E9C46A',
      glowColor: '#2A9D8F',
      strokeWidth: 3.5,
      filterId: 'standardGlow',
      titleSuffix: 'Campus Flow'
    };
  }
}

/**
 * Generates full SVG Document
 * @param {Array<{lat: number, lon: number, timestamp?: number}>} coordinates 
 * @param {number} currentStreak 
 * @param {string} title 
 */
function generateRouteSvg(coordinates, currentStreak = 1, title = 'Campus Route Artwork') {
  const points = normalizeCoordinates(coordinates, 500, 45);
  const pathD = generateBezierPathData(points);
  const visuals = getStreakVisuals(currentStreak);

  // Background subtle decorative watercolor zones
  const blobs = points.filter((_, idx) => idx % Math.max(1, Math.floor(points.length / 5)) === 0);
  const blobElements = blobs.map((pt, i) => {
    const rx = 40 + ((i * 19) % 35);
    const ry = 30 + ((i * 23) % 40);
    const color = i % 2 === 0 ? visuals.primaryColor : visuals.secondaryColor;
    return `<ellipse cx="${pt.x}" cy="${pt.y}" rx="${rx}" ry="${ry}" fill="${color}" fill-opacity="0.25" filter="blur(16px)" />`;
  }).join('\n    ');

  const svg = `
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 500 500" width="500" height="500">
  <defs>
    <linearGradient id="routeGradient" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="${visuals.primaryColor}" />
      <stop offset="100%" stop-color="${visuals.secondaryColor}" />
    </linearGradient>
    <filter id="neonBlur" x="-30%" y="-30%" width="160%" height="160%">
      <feGaussianBlur stdDeviation="5" result="blur" />
      <feComposite in="SourceGraphic" in2="blur" operator="over" />
    </filter>
  </defs>
  
  <!-- Canvas Canvas Base -->
  <rect width="100%" height="100%" fill="#0D1117" rx="28" />
  
  <!-- Background Glow Zones -->
  <g id="ambient-blobs">
    ${blobElements}
  </g>
  
  <!-- Generated Smoothed Bezier GPS Route -->
  <g id="route-layer">
    <path d="${pathD}" fill="none" stroke="${visuals.glowColor}" stroke-width="${visuals.strokeWidth * 2}" stroke-linecap="round" stroke-linejoin="round" opacity="0.45" filter="url(#neonBlur)" />
    <path d="${pathD}" fill="none" stroke="url(#routeGradient)" stroke-width="${visuals.strokeWidth}" stroke-linecap="round" stroke-linejoin="round" />
  </g>
  
  ${points.length > 0 ? `
  <!-- Start & End Nodes -->
  <circle cx="${points[0].x}" cy="${points[0].y}" r="6" fill="#00F5D4" />
  <circle cx="${points[0].x}" cy="${points[0].y}" r="3" fill="#FFFFFF" />
  <circle cx="${points[points.length - 1].x}" cy="${points[points.length - 1].y}" r="6" fill="#FF007F" />
  <circle cx="${points[points.length - 1].x}" cy="${points[points.length - 1].y}" r="3" fill="#FFFFFF" />
  ` : ''}
</svg>`.trim();

  return {
    svgString: svg,
    pathData: pathD,
    visuals,
    pointCount: points.length
  };
}

/**
 * Uploads generated SVG to storage (or returns mock CDN URL)
 */
async function uploadSvgArt(svgString, artworkId) {
  try {
    const mockStorageUrl = `https://storage.googleapis.com/campus-route-art.appspot.com/artworks/${artworkId}.svg`;
    return mockStorageUrl;
  } catch (error) {
    console.error('Storage upload error:', error);
    return null;
  }
}

module.exports = {
  normalizeCoordinates,
  generateBezierPathData,
  getStreakVisuals,
  generateRouteSvg,
  uploadSvgArt
};
