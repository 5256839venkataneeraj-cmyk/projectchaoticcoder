/**
 * seed.js - Seeds Firestore with initial sample campus data
 */
const { db, Collections } = require('../db');
const artService = require('../services/artService');

async function seed() {
  console.log('🌱 Seeding Firestore with sample Campus Route-to-Art data...');

  const sampleUsers = [
    { uid: 'u1', name: 'Diya Patel', dorm: 'Ladies Hostel Oasis', department: 'Design Dept', totalDistance: 84.2, totalSteps: 112300, currentStreak: 9, avatar: '👩‍🎨' },
    { uid: 'u2', name: 'Aarav Sharma', dorm: 'Block D Dragons', department: 'CS Dept', totalDistance: 64.8, totalSteps: 86400, currentStreak: 5, avatar: '🧑‍💻' },
    { uid: 'u3', name: 'Rohan Verma', dorm: 'Block A Titans', department: 'Mech Dept', totalDistance: 52.1, totalSteps: 69500, currentStreak: 3, avatar: '🏃' },
    { uid: 'u4', name: 'Sneha Reddy', dorm: 'Ladies Hostel', department: 'Biotech Dept', totalDistance: 45.3, totalSteps: 60400, currentStreak: 2, avatar: '🌸' }
  ];

  try {
    for (const user of sampleUsers) {
      await db.collection(Collections.USERS).doc(user.uid).set({
        ...user,
        lastWalkDate: new Date().toISOString(),
        createdAt: new Date().toISOString()
      });
      console.log(`✓ Seeded user: ${user.name}`);
    }

    // Seed sample artwork
    const sampleCoords = [
      { lat: 12.9692, lon: 79.1559 },
      { lat: 12.9710, lon: 79.1585 },
      { lat: 12.9735, lon: 79.1601 },
      { lat: 12.9720, lon: 79.1570 },
      { lat: 12.9692, lon: 79.1559 }
    ];
    const art = artService.generateRouteSvg(sampleCoords, 5, 'Campus Bloom Masterpiece');
    await db.collection(Collections.ARTWORKS).doc('art_seed_01').set({
      id: 'art_seed_01',
      userId: 'u1',
      title: 'Campus Bloom Masterpiece (Cyber Neon)',
      svgString: art.svgString,
      svgUrl: 'https://storage.googleapis.com/campus-route-art.appspot.com/artworks/seed1.svg',
      streakTier: 'NEON_CYBER',
      streakCount: 5,
      stats: { distanceKm: 5.6, steps: 7842, durationMinutes: 48 },
      createdAt: new Date().toISOString()
    });
    console.log('✓ Seeded sample artwork');

    console.log('🎉 Seeding complete successfully!');
  } catch (error) {
    console.error('Error during seeding:', error);
  }
}

if (require.main === module) {
  seed();
}

module.exports = seed;
