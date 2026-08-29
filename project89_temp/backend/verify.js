const app = require('./server');

const PORT = 5002;
const server = app.listen(PORT, async () => {
  console.log(`Backend verification server running on port ${PORT}`);

  try {
    // Test 1: GET /api/test
    const res1 = await fetch(`http://localhost:${PORT}/api/test`);
    const data1 = await res1.json();
    console.log('✅ 1. GET /api/test:', data1.message);

    // Test 2: POST /api/v1/sessions/upload
    const testPayload = {
      userId: 'test_student_2026',
      startTime: new Date().toISOString(),
      endTime: new Date(Date.now() + 600000).toISOString(),
      routeData: [
        { latitude: 12.971598, longitude: 77.594566, timestamp: Date.now() },
        { latitude: 12.97175, longitude: 77.59472, timestamp: Date.now() + 5000 },
        { latitude: 12.97192, longitude: 77.59489, timestamp: Date.now() + 10000 }
      ],
      notes: 'Campus quadrangle walk test'
    };

    const res2 = await fetch(`http://localhost:${PORT}/api/v1/sessions/upload`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(testPayload)
    });

    const data2 = await res2.json();
    console.log(`✅ 2. POST /api/v1/sessions/upload [HTTP ${res2.status}]:`, data2.message);
    console.log('   Saved session data:', {
      id: data2.data?._id || data2.data?.id,
      userId: data2.data?.userId,
      pointCount: data2.data?.routeData?.length
    });

    if (res2.status === 201 && data2.success) {
      console.log('🎉 ALL BACKEND TESTS PASSED (Status 201 Created confirmed)!');
    } else {
      console.error('❌ Backend test FAILED: Expected 201 status, got', res2.status);
    }
  } catch (err) {
    console.error('❌ Verification script error:', err);
  } finally {
    server.close(() => {
      console.log('Verification server closed.');
    });
  }
});
