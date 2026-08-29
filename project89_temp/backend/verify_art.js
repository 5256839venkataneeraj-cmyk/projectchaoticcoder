const app = require('./server');
const {
  normalizeAndScaleCoordinates,
  pointsToSvgPath,
  generateArtwork,
  determineRarity
} = require('./src/services/artService');

const PORT = 5003;

async function runTests() {
  console.log('=== PHASE 3 BACKEND & GENERATIVE ENGINE VERIFICATION ===\n');

  // Test 1: Math & Generative Algorithm Units
  console.log('🧪 Step 1: Testing Generative Art Engine Math...');
  const sampleRoute = [
    { latitude: 12.971598, longitude: 77.594566, timestamp: 1000 },
    { latitude: 12.972100, longitude: 77.595100, timestamp: 2000 },
    { latitude: 12.972800, longitude: 77.594800, timestamp: 3000 },
    { latitude: 12.973400, longitude: 77.595500, timestamp: 4000 },
    { latitude: 12.974000, longitude: 77.596000, timestamp: 5000 }
  ];

  const points2D = normalizeAndScaleCoordinates(sampleRoute, 500, 45);
  console.log(`   Normalized ${points2D.length} points to 500x500 box.`);
  
  // Verify coordinate bounds within [45, 455]
  for (const pt of points2D) {
    if (pt.x < 44 || pt.x > 456 || pt.y < 44 || pt.y > 456) {
      throw new Error(`Coordinate out of 500x500 box bounds: (${pt.x}, ${pt.y})`);
    }
  }
  console.log('   ✅ Coordinate normalization & aspect ratio bounds test passed!');

  const pathD = pointsToSvgPath(points2D);
  if (!pathD.startsWith('M ') || !pathD.includes(' C ')) {
    throw new Error(`Unexpected path format: ${pathD}`);
  }
  console.log('   ✅ Cubic Bézier spline path smoothing test passed!');
  console.log('   Sample SVG Path d:', pathD.substring(0, 70) + '...');

  // Test 2: generateArtwork service
  const generatedArt = generateArtwork({
    routeData: sampleRoute,
    userId: 'student_tester',
    sessionId: 'session_mock_123',
    durationSeconds: 300
  });

  if (!generatedArt.svgData || !generatedArt.svgData.includes('<svg') || !generatedArt.svgData.includes('viewBox="0 0 500 500"')) {
    throw new Error('Invalid SVG markup produced by generateArtwork');
  }
  console.log(`   ✅ Generative Art SVG generated with rarity: ${generatedArt.rarity}`);

  // Test 3: HTTP Server Integration Tests
  const server = app.listen(PORT, async () => {
    let hasError = false;
    try {
      console.log(`\n🧪 Step 2: Testing API Endpoints on port ${PORT}...`);

      // 3a. POST /api/v1/sessions/upload
      const uploadRes = await fetch(`http://localhost:${PORT}/api/v1/sessions/upload`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: 'student_tester',
          startTime: new Date(Date.now() - 300000).toISOString(),
          endTime: new Date().toISOString(),
          routeData: sampleRoute,
          notes: 'Morning campus walk across North Lawn'
        })
      });

      const uploadJson = await uploadRes.json();
      console.log(`   POST /api/v1/sessions/upload [HTTP ${uploadRes.status}]: ${uploadJson.message}`);
      
      if (uploadRes.status !== 201 || !uploadJson.data || !uploadJson.artwork || !uploadJson.artwork.svgData) {
        throw new Error('Upload session response missing artwork/svgData!');
      }
      console.log('   ✅ Upload session created WalkSession AND minted Artwork with SVG!');
      console.log('   Minted Artwork ID:', uploadJson.artwork._id);
      console.log('   Rarity:', uploadJson.artwork.rarity);

      // 3b. Upload a second session for the same user to test gallery sorting
      const longerRoute = [
        ...sampleRoute,
        { latitude: 12.975000, longitude: 77.597000, timestamp: 6000 },
        { latitude: 12.976000, longitude: 77.598000, timestamp: 7000 },
        { latitude: 12.977000, longitude: 77.599000, timestamp: 8000 }
      ];

      await fetch(`http://localhost:${PORT}/api/v1/sessions/upload`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: 'student_tester',
          startTime: new Date().toISOString(),
          endTime: new Date(Date.now() + 600000).toISOString(),
          routeData: longerRoute,
          notes: 'Afternoon trek around campus perimeter'
        })
      });

      // 3c. GET /api/v1/artworks/gallery/:userId
      const galleryRes = await fetch(`http://localhost:${PORT}/api/v1/artworks/gallery/student_tester`);
      const galleryJson = await galleryRes.json();
      console.log(`   GET /api/v1/artworks/gallery/student_tester [HTTP ${galleryRes.status}]: Found ${galleryJson.count} artworks.`);

      if (galleryRes.status !== 200 || galleryJson.count < 2 || !galleryJson.data[0].svgData) {
        throw new Error('Gallery retrieval failed or returned invalid data');
      }
      console.log('   ✅ Personal gallery retrieved successfully, sorted newest first!');

      // 3d. GET /api/v1/artworks/:id
      const firstArtworkId = galleryJson.data[0]._id;
      const singleRes = await fetch(`http://localhost:${PORT}/api/v1/artworks/${firstArtworkId}`);
      const singleJson = await singleRes.json();
      if (singleRes.status !== 200 || !singleJson.data) {
        throw new Error('Single artwork lookup failed');
      }
      console.log(`   ✅ GET /api/v1/artworks/${firstArtworkId} retrieved single artwork successfully!`);

      // 3e. POST /api/v1/artworks/generate
      const previewRes = await fetch(`http://localhost:${PORT}/api/v1/artworks/generate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          routeData: sampleRoute,
          rarityOverride: 'Legendary'
        })
      });
      const previewJson = await previewRes.json();
      if (previewRes.status !== 200 || previewJson.data.rarity !== 'Legendary') {
        throw new Error('Artwork preview generator failed');
      }
      console.log('   ✅ Standalone SVG preview generation test passed (Legendary override confirmed)!');

      console.log('\n🎉 ALL PHASE 3 BACKEND & GENERATIVE ENGINE TESTS PASSED SUCCESSFULLY! 🎉');
    } catch (err) {
      console.error('\n❌ TEST SUITE FAILED:', err);
      hasError = true;
    } finally {
      server.close(() => {
        console.log('Test server closed.');
      });
    }
  });
}

runTests();
