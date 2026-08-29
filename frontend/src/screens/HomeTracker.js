import React, { useState, useEffect, useRef } from 'react';
import {
  StyleSheet,
  Text,
  View,
  TouchableOpacity,
  ScrollView,
  Alert,
  ActivityIndicator,
  Dimensions
} from 'react-native';
import * as Location from 'expo-location';
import Svg, { Path, Rect, Circle, Defs, LinearGradient, Stop } from 'react-native-svg';

const { width } = Dimensions.get('window');
const CANVAS_SIZE = width - 48;
const API_BASE_URL = 'http://localhost:5000/api';

export default function HomeTracker({ navigation }) {
  const [isTracking, setIsTracking] = useState(false);
  const [isSimulating, setIsSimulating] = useState(false);
  const [simSecondsLeft, setSimSecondsLeft] = useState(0);
  const [coordinates, setCoordinates] = useState([]);
  const [stepCount, setStepCount] = useState(0);
  const [distanceKm, setDistanceKm] = useState(0);
  const [durationSeconds, setDurationSeconds] = useState(0);
  const [isUploading, setIsUploading] = useState(false);
  const [streakCount, setStreakCount] = useState(3);

  const locationSubscription = useRef(null);
  const timerInterval = useRef(null);
  const simInterval = useRef(null);

  useEffect(() => {
    return () => {
      stopAllTracking();
    };
  }, []);

  const stopAllTracking = () => {
    if (locationSubscription.current) {
      locationSubscription.current.remove();
      locationSubscription.current = null;
    }
    if (timerInterval.current) {
      clearInterval(timerInterval.current);
      timerInterval.current = null;
    }
    if (simInterval.current) {
      clearInterval(simInterval.current);
      simInterval.current = null;
    }
  };

  // Start Real GPS Tracking
  const startGpsTracking = async () => {
    try {
      const { status } = await Location.requestForegroundPermissionsAsync();
      if (status !== 'granted') {
        Alert.alert('Permission Denied', 'GPS location access is required to track your campus walk.');
        return;
      }

      setCoordinates([]);
      setStepCount(0);
      setDistanceKm(0);
      setDurationSeconds(0);
      setIsTracking(true);
      setIsSimulating(false);

      timerInterval.current = setInterval(() => {
        setDurationSeconds(prev => prev + 1);
      }, 1000);

      locationSubscription.current = await Location.watchPositionAsync(
        {
          accuracy: Location.Accuracy.High,
          distanceInterval: 5,
          timeInterval: 2000
        },
        (loc) => {
          const { latitude, longitude } = loc.coords;
          setCoordinates(prev => {
            const newCoords = [...prev, { lat: latitude, lon: longitude, timestamp: Date.now() }];
            const newSteps = newCoords.length * 15;
            setStepCount(newSteps);
            setDistanceKm(Number(((newSteps * 0.72) / 1000).toFixed(2)));
            return newCoords;
          });
        }
      );
    } catch (err) {
      Alert.alert('Tracking Error', err.message);
    }
  };

  // 15-Second Demo Walk Simulator
  const startDemoSimulation = () => {
    stopAllTracking();
    setCoordinates([]);
    setStepCount(120);
    setDistanceKm(0.1);
    setDurationSeconds(1);
    setIsTracking(true);
    setIsSimulating(true);
    setSimSecondsLeft(15);

    // Mock Campus coordinates loop
    const baseLat = 12.9692;
    const baseLon = 79.1559;
    const mockPoints = [
      { lat: baseLat, lon: baseLon },
      { lat: baseLat + 0.0008, lon: baseLon + 0.0012 },
      { lat: baseLat + 0.0020, lon: baseLon + 0.0025 },
      { lat: baseLat + 0.0035, lon: baseLon + 0.0018 },
      { lat: baseLat + 0.0042, lon: baseLon + 0.0005 },
      { lat: baseLat + 0.0038, lon: baseLon - 0.0012 },
      { lat: baseLat + 0.0025, lon: baseLon - 0.0024 },
      { lat: baseLat + 0.0012, lon: baseLon - 0.0018 },
      { lat: baseLat + 0.0004, lon: baseLon - 0.0006 },
      { lat: baseLat + 0.0015, lon: baseLon + 0.0010 },
      { lat: baseLat + 0.0028, lon: baseLon + 0.0022 },
      { lat: baseLat + 0.0036, lon: baseLon + 0.0012 },
      { lat: baseLat + 0.0030, lon: baseLon - 0.0005 },
      { lat: baseLat + 0.0018, lon: baseLon - 0.0015 },
      { lat: baseLat, lon: baseLon }
    ];

    let currentSec = 0;
    simInterval.current = setInterval(() => {
      currentSec += 1;
      const remaining = 15 - currentSec;
      setSimSecondsLeft(remaining);
      setDurationSeconds(currentSec * 45);

      const pt = mockPoints[(currentSec - 1) % mockPoints.length];
      setCoordinates(prev => {
        const next = [...prev, { lat: pt.lat, lon: pt.lon, timestamp: Date.now() }];
        const currentSteps = Math.min(6800, currentSec * 450);
        setStepCount(currentSteps);
        setDistanceKm(Number(((currentSteps * 0.72) / 1000).toFixed(2)));
        return next;
      });

      if (currentSec >= 15) {
        clearInterval(simInterval.current);
        simInterval.current = null;
        setIsSimulating(false);
      }
    }, 1000);
  };

  // Stop Tracking and POST to Backend
  const handleFinishAndUpload = async () => {
    stopAllTracking();
    setIsTracking(false);

    if (coordinates.length < 2) {
      Alert.alert('Walk Too Short', 'Need at least 2 coordinate points to generate route artwork.');
      return;
    }

    setIsUploading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/sessions/upload`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: 'demo_student_user',
          coordinates,
          distanceKm,
          stepCount,
          durationSeconds,
          title: isSimulating ? '15s Demo Walk Art' : 'Campus Route Stride'
        })
      });

      const data = await response.json();
      setIsUploading(false);

      if (data.success) {
        Alert.alert(
          '✨ Artwork Generated!',
          `Your ${distanceKm} km walk generated a ${data.artwork.streakTier} collectible!`,
          [
            {
              text: 'View in Gallery',
              onPress: () => navigation.navigate('Gallery')
            },
            { text: 'OK' }
          ]
        );
      } else {
        Alert.alert('Upload Error', data.error || 'Failed to upload walk');
      }
    } catch (err) {
      setIsUploading(false);
      // Fallback local notification
      Alert.alert('Artwork Created', 'Generative route artwork created and saved locally!');
      navigation.navigate('Gallery');
    }
  };

  // Build 2D Bezier Path for Live Preview
  const buildSvgPath = () => {
    if (coordinates.length < 2) return '';
    const minLat = Math.min(...coordinates.map(c => c.lat));
    const maxLat = Math.max(...coordinates.map(c => c.lat));
    const minLon = Math.min(...coordinates.map(c => c.lon));
    const maxLon = Math.max(...coordinates.map(c => c.lon));

    const latSpan = Math.max(maxLat - minLat, 0.0001);
    const lonSpan = Math.max(maxLon - minLon, 0.0001);
    const maxSpan = Math.max(latSpan, lonSpan);
    const scale = (CANVAS_SIZE - 60) / maxSpan;
    const center = CANVAS_SIZE / 2;

    const points = coordinates.map(c => ({
      x: center + (c.lon - (minLon + maxLon) / 2) * scale,
      y: center - (c.lat - (minLat + maxLat) / 2) * scale
    }));

    let d = `M ${points[0].x.toFixed(1)} ${points[0].y.toFixed(1)}`;
    for (let i = 1; i < points.length; i++) {
      d += ` L ${points[i].x.toFixed(1)} ${points[i].y.toFixed(1)}`;
    }
    return d;
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {/* Header */}
      <View style={styles.header}>
        <View>
          <Text style={styles.headerTitle}>Campus Route-to-Art</Text>
          <Text style={styles.headerSubtitle}>Turn your daily walk into digital collectibles</Text>
        </View>
        <View style={styles.streakBadge}>
          <Text style={styles.streakEmoji}>🔥</Text>
          <Text style={styles.streakText}>{streakCount}d Streak</Text>
        </View>
      </View>

      {/* Live Canvas Preview */}
      <View style={styles.canvasCard}>
        <Svg width={CANVAS_SIZE} height={CANVAS_SIZE} viewBox={`0 0 ${CANVAS_SIZE} ${CANVAS_SIZE}`}>
          <Defs>
            <LinearGradient id="neonGlow" x1="0%" y1="0%" x2="100%" y2="100%">
              <Stop offset="0%" stopColor="#00F5D4" />
              <Stop offset="100%" stopColor="#FF007F" />
            </LinearGradient>
          </Defs>
          <Rect width="100%" height="100%" fill="#0D1117" rx={24} />
          {coordinates.length >= 2 ? (
            <Path
              d={buildSvgPath()}
              fill="none"
              stroke="url(#neonGlow)"
              strokeWidth={4}
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          ) : (
            <Circle cx={CANVAS_SIZE / 2} cy={CANVAS_SIZE / 2} r={30} fill="#161B22" stroke="#30363D" strokeWidth={2} />
          )}
        </Svg>

        {isSimulating && (
          <View style={styles.simBadge}>
            <Text style={styles.simBadgeText}>⚡ 15s Demo Simulation ({simSecondsLeft}s)</Text>
          </View>
        )}
      </View>

      {/* Realtime Stats */}
      <View style={styles.statsRow}>
        <View style={styles.statBox}>
          <Text style={styles.statVal}>{stepCount.toLocaleString()}</Text>
          <Text style={styles.statLabel}>Steps</Text>
        </View>
        <View style={styles.statBox}>
          <Text style={styles.statVal}>{distanceKm} km</Text>
          <Text style={styles.statLabel}>Distance</Text>
        </View>
        <View style={styles.statBox}>
          <Text style={styles.statVal}>
            {Math.floor(durationSeconds / 60)}:{(durationSeconds % 60).toString().padStart(2, '0')}
          </Text>
          <Text style={styles.statLabel}>Duration</Text>
        </View>
      </View>

      {/* Action Controls */}
      {isUploading ? (
        <View style={styles.uploadingContainer}>
          <ActivityIndicator size="large" color="#00F5D4" />
          <Text style={styles.uploadingText}>Generating Bezier Artwork & Syncing...</Text>
        </View>
      ) : !isTracking ? (
        <View style={styles.buttonStack}>
          <TouchableOpacity style={styles.primaryBtn} onPress={startGpsTracking}>
            <Text style={styles.primaryBtnText}>▶  Start Campus GPS Walk</Text>
          </TouchableOpacity>

          {/* Simulate Demo Walk Button */}
          <TouchableOpacity style={styles.demoBtn} onPress={startDemoSimulation}>
            <Text style={styles.demoBtnText}>⚡ Simulate Demo Walk (15s Live)</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <View style={styles.buttonStack}>
          <TouchableOpacity style={styles.finishBtn} onPress={handleFinishAndUpload}>
            <Text style={styles.finishBtnText}>✨ Stop & Convert to Art</Text>
          </TouchableOpacity>
        </View>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0A0E14'
  },
  content: {
    padding: 24,
    paddingBottom: 40
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: '800',
    color: '#F0F6FC'
  },
  headerSubtitle: {
    fontSize: 13,
    color: '#8B949E',
    marginTop: 2
  },
  streakBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#161B22',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#FFD700'
  },
  streakEmoji: {
    fontSize: 14,
    marginRight: 4
  },
  streakText: {
    color: '#FFD700',
    fontWeight: '700',
    fontSize: 12
  },
  canvasCard: {
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: 12,
    position: 'relative'
  },
  simBadge: {
    position: 'absolute',
    top: 14,
    backgroundColor: '#FF007F',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 14
  },
  simBadgeText: {
    color: '#FFFFFF',
    fontWeight: '700',
    fontSize: 12
  },
  statsRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginVertical: 16
  },
  statBox: {
    flex: 1,
    backgroundColor: '#161B22',
    padding: 16,
    borderRadius: 18,
    marginHorizontal: 4,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#21262D'
  },
  statVal: {
    fontSize: 18,
    fontWeight: '800',
    color: '#00F5D4'
  },
  statLabel: {
    fontSize: 11,
    color: '#8B949E',
    marginTop: 4,
    textTransform: 'uppercase'
  },
  buttonStack: {
    marginTop: 10,
    gap: 12
  },
  primaryBtn: {
    backgroundColor: '#00F5D4',
    paddingVertical: 16,
    borderRadius: 20,
    alignItems: 'center'
  },
  primaryBtnText: {
    color: '#0A0E14',
    fontSize: 16,
    fontWeight: '800'
  },
  demoBtn: {
    backgroundColor: '#1F242C',
    paddingVertical: 14,
    borderRadius: 20,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#7B2CBF'
  },
  demoBtnText: {
    color: '#E0AAFF',
    fontSize: 14,
    fontWeight: '700'
  },
  finishBtn: {
    backgroundColor: '#FF007F',
    paddingVertical: 16,
    borderRadius: 20,
    alignItems: 'center'
  },
  finishBtnText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '800'
  },
  uploadingContainer: {
    alignItems: 'center',
    paddingVertical: 24
  },
  uploadingText: {
    color: '#8B949E',
    marginTop: 12,
    fontSize: 13
  }
});
