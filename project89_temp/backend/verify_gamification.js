const app = require('./server');
const { updateUserGamification, inMemoryUsers } = require('./src/controllers/userController');

const PORT = 5004;

async function runGamificationTests() {
  console.log('=== PHASE 4 GAMIFICATION & LEADERBOARD VERIFICATION ===\n');

  // Test 1: Unit testing streak logic
  console.log('🧪 Step 1: Testing Gamification Streak & Distance Calculation...');
  const testUser = 'streak_test_hero';

  // Day 1: Walk 1200m (qualifying walk > 500m)
  const day1Time = new Date('2026-08-01T10:00:00Z');
  const u1 = await updateUserGamification(testUser, 1200, day1Time);
  console.log(`   Day 1 Walk (1200m): Streak = ${u1.currentStreak}, TotalDist = ${u1.totalDistance}m`);
  if (u1.currentStreak !== 1 || u1.totalDistance !== 1200) {
    throw new Error(`Day 1 streak mismatch: got ${u1.currentStreak}, expected 1`);
  }

  // Day 2: Walk 800m 26 hours later (within 24-48h window)
  const day2Time = new Date('2026-08-02T12:00:00Z'); // 26 hours later
  const u2 = await updateUserGamification(testUser, 800, day2Time);
  console.log(`   Day 2 Walk (800m, +26h): Streak = ${u2.currentStreak}, TotalDist = ${u2.totalDistance}m`);
  if (u2.currentStreak !== 2 || u2.totalDistance !== 2000) {
    throw new Error(`Day 2 streak increment failed: got ${u2.currentStreak}, expected 2`);
  }

  // Day 3: Walk 1500m 24 hours later
  const day3Time = new Date('2026-08-03T12:00:00Z'); // 24 hours later
  const u3 = await updateUserGamification(testUser, 1500, day3Time);
  console.log(`   Day 3 Walk (1500m, +24h): Streak = ${u3.currentStreak}, TotalDist = ${u3.totalDistance}m`);
  if (u3.currentStreak !== 3 || u3.totalDistance !== 3500) {
    throw new Error(`Day 3 streak increment failed: got ${u3.currentStreak}, expected 3`);
  }

  // Day 6: Walk 900m 72 hours later (streak broken > 48h)
  const day6Time = new Date('2026-08-06T12:00:00Z'); // 72 hours later
  const u4 = await updateUserGamification(testUser, 900, day6Time);
  console.log(`   Day 6 Walk (900m, +72h - broken): Streak = ${u4.currentStreak}, TotalDist = ${u4.totalDistance}m`);
  if (u4.currentStreak !== 1 || u4.totalDistance !== 4400) {
    throw new Error(`Streak reset failed: got ${u4.currentStreak}, expected 1`);
  }
  console.log('   ✅ Gamification 24-48h streak logic and totalDistance accumulation verified!');

  // Test 2: HTTP API Integration Tests
  const server = app.listen(PORT, async () => {
    try {
      console.log(`\n🧪 Step 2: Testing API Endpoints on port ${PORT}...`);

      // 2a. POST /api/v1/sessions/upload with gamification
      const walkPayload = {
        userId: 'leaderboard_champion',
        startTime: new Date(Date.now() - 3600000).toISOString(),
        endTime: new Date().toISOString(),
        routeData: [
          { latitude: 12.9716, longitude: 77.5945, timestamp: 1000 },
          { latitude: 12.9730, longitude: 77.5960, timestamp: 2000 },
          { latitude: 12.9750, longitude: 77.5980, timestamp: 3000 },
          { latitude: 12.9770, longitude: 77.6000, timestamp: 4000 }
        ],
        notes: 'Leaderboard qualifying campus trek'
      };

      const uploadRes = await fetch(`http://localhost:${PORT}/api/v1/sessions/upload`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(walkPayload)
      });
      const uploadJson = await uploadRes.json();
      console.log(`   POST /api/v1/sessions/upload [HTTP ${uploadRes.status}]: ${uploadJson.message}`);
      
      if (uploadRes.status !== 201 || !uploadJson.user || !uploadJson.user.totalDistance) {
        throw new Error('Upload session response missing user gamification data!');
      }
      console.log('   ✅ Session upload updated user gamification stats:', {
        userId: uploadJson.user.userId,
        totalDistance: uploadJson.user.totalDistance,
        streak: uploadJson.user.currentStreak
      });

      // 2b. GET /api/v1/users/leaderboard
      const leadRes = await fetch(`http://localhost:${PORT}/api/v1/users/leaderboard`);
      const leadJson = await leadRes.json();
      console.log(`   GET /api/v1/users/leaderboard [HTTP ${leadRes.status}]: ${leadJson.count} users returned.`);

      if (leadRes.status !== 200 || !Array.isArray(leadJson.data) || leadJson.data.length === 0) {
        throw new Error('Leaderboard API failed');
      }

      // Verify descending order
      for (let i = 1; i < leadJson.data.length; i++) {
        if (leadJson.data[i - 1].totalDistance < leadJson.data[i].totalDistance) {
          throw new Error('Leaderboard not sorted by totalDistance descending!');
        }
      }
      console.log('   ✅ Leaderboard returned top users sorted by totalDistance descending!');
      console.log('   Top 3 campus runners:');
      leadJson.data.slice(0, 3).forEach((u) => {
        console.log(`     #${u.rank} ${u.avatarEmoji} ${u.username}: ${u.totalDistanceKm} km (🔥 ${u.currentStreak}d streak)`);
      });

      // 2c. GET /api/v1/users/profile/:userId
      const profileRes = await fetch(`http://localhost:${PORT}/api/v1/users/profile/leaderboard_champion`);
      const profileJson = await profileRes.json();
      if (profileRes.status !== 200 || profileJson.data.userId !== 'leaderboard_champion') {
        throw new Error('Profile lookup failed');
      }
      console.log(`   ✅ GET /api/v1/users/profile/leaderboard_champion verified!`);

      console.log('\n🎉 ALL PHASE 4 GAMIFICATION & LEADERBOARD TESTS PASSED! 🎉');
    } catch (err) {
      console.error('\n❌ TEST SUITE FAILED:', err);
      process.exitCode = 1;
    } finally {
      server.close(() => {
        console.log('Test server closed.');
      });
    }
  });
}

runGamificationTests();
