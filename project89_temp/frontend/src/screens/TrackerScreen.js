import React, { useState, useEffect, useRef } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  TextInput,
  ActivityIndicator,
  Alert,
  Platform
} from 'react-native';
import { uploadWalkSession } from '../services/api';
import ArtworkModal from '../components/ArtworkModal';
import { THEME } from '../theme';

// Recognizable 15-Step Star of Campus Art coordinates
// Center: (12.9716, 77.5945)
const MOCK_STAR_COORDINATES = [
  { latitude: 12.97510, longitude: 77.59450, name: 'North Star Apex', timestamp: 1000 },
  { latitude: 12.97270, longitude: 77.59570, name: 'North-East Valley', timestamp: 2000 },
  { latitude: 12.97380, longitude: 77.59820, name: 'East Star Vertex', timestamp: 3000 },
  { latitude: 12.97160, longitude: 77.59620, name: 'South-East Valley', timestamp: 4000 },
  { latitude: 12.96920, longitude: 77.59750, name: 'South-East Star Vertex', timestamp: 5000 },
  { latitude: 12.97010, longitude: 77.59450, name: 'South Center Valley', timestamp: 6000 },
  { latitude: 12.96920, longitude: 77.59150, name: 'South-West Star Vertex', timestamp: 7000 },
  { latitude: 12.97160, longitude: 77.59280, name: 'South-West Valley', timestamp: 8000 },
  { latitude: 12.97380, longitude: 77.59080, name: 'West Star Vertex', timestamp: 9000 },
  { latitude: 12.97270, longitude: 77.59330, name: 'North-West Valley', timestamp: 10000 },
  { latitude: 12.97420, longitude: 77.59400, name: 'North-West Spire', timestamp: 11000 },
  { latitude: 12.97510, longitude: 77.59450, name: 'Crown Return Point', timestamp: 12000 },
  { latitude: 12.97350, longitude: 77.59550, name: 'Inner Star Glow Node', timestamp: 13000 },
  { latitude: 12.97160, longitude: 77.59450, name: 'Core Cosmic Center', timestamp: 14000 },
  { latitude: 12.97510, longitude: 77.59450, name: 'Final Star Seal', timestamp: 15000 }
];

const PRESET_ROUTES = [
  {
    id: 'quad',
    name: 'Library Quad Loop',
    tag: 'Classic Campus Walk',
    projectedRarity: 'Common',
    color: '#10B981',
    distanceMeters: 450,
    coordinates: [
      { latitude: 12.9716, longitude: 77.5945, timestamp: 1000 },
      { latitude: 12.9720, longitude: 77.5948, timestamp: 2000 },
      { latitude: 12.9725, longitude: 77.5954, timestamp: 3000 },
      { latitude: 12.9722, longitude: 77.5960, timestamp: 4000 },
      { latitude: 12.9715, longitude: 77.5958, timestamp: 5000 },
      { latitude: 12.9710, longitude: 77.5950, timestamp: 6000 },
      { latitude: 12.9716, longitude: 77.5945, timestamp: 7000 }
    ]
  },
  {
    id: 'sculpture',
    name: 'Sculpture Garden Path',
    tag: 'Scenic & Organic',
    projectedRarity: 'Uncommon',
    color: '#06B6D4',
    distanceMeters: 1250,
    coordinates: [
      { latitude: 12.9710, longitude: 77.5930, timestamp: 1000 },
      { latitude: 12.9718, longitude: 77.5935, timestamp: 2000 },
      { latitude: 12.9724, longitude: 77.5942, timestamp: 3000 },
      { latitude: 12.9732, longitude: 77.5938, timestamp: 4000 },
      { latitude: 12.9740, longitude: 77.5945, timestamp: 5000 },
      { latitude: 12.9746, longitude: 77.5956, timestamp: 6000 },
      { latitude: 12.9738, longitude: 77.5965, timestamp: 7000 },
      { latitude: 12.9728, longitude: 77.5972, timestamp: 8000 },
      { latitude: 12.9719, longitude: 77.5968, timestamp: 9000 },
      { latitude: 12.9710, longitude: 77.5955, timestamp: 10000 },
      { latitude: 12.9705, longitude: 77.5940, timestamp: 11000 }
    ]
  },
  {
    id: 'tech_spine',
    name: 'Engineering Tech Spine',
    tag: 'Long Distance Sprint',
    projectedRarity: 'Rare',
    color: '#8B5CF6',
    distanceMeters: 2400,
    coordinates: [
      { latitude: 12.9690, longitude: 77.5910, timestamp: 1000 },
      { latitude: 12.9702, longitude: 77.5925, timestamp: 2000 },
      { latitude: 12.9715, longitude: 77.5938, timestamp: 3000 },
      { latitude: 12.9728, longitude: 77.5950, timestamp: 4000 },
      { latitude: 12.9745, longitude: 77.5962, timestamp: 5000 },
      { latitude: 12.9760, longitude: 77.5978, timestamp: 6000 },
      { latitude: 12.9772, longitude: 77.5992, timestamp: 7000 },
      { latitude: 12.9765, longitude: 77.6008, timestamp: 8000 },
      { latitude: 12.9752, longitude: 77.6015, timestamp: 9000 },
      { latitude: 12.9738, longitude: 77.6002, timestamp: 10000 },
      { latitude: 12.9720, longitude: 77.5985, timestamp: 11000 },
      { latitude: 12.9705, longitude: 77.5960, timestamp: 12000 },
      { latitude: 12.9695, longitude: 77.5935, timestamp: 13000 }
    ]
  },
  {
    id: 'lakeview',
    name: 'Lakeview Campus Perimeter',
    tag: 'Epic Grand Tour',
    projectedRarity: 'Epic',
    color: '#F59E0B',
    distanceMeters: 4200,
    coordinates: [
      { latitude: 12.9680, longitude: 77.5900, timestamp: 1000 },
      { latitude: 12.9700, longitude: 77.5915, timestamp: 2000 },
      { latitude: 12.9720, longitude: 77.5930, timestamp: 3000 },
      { latitude: 12.9745, longitude: 77.5945, timestamp: 4000 },
      { latitude: 12.9770, longitude: 77.5965, timestamp: 5000 },
      { latitude: 12.9790, longitude: 77.5990, timestamp: 6000 },
      { latitude: 12.9805, longitude: 77.6020, timestamp: 7000 },
      { latitude: 12.9795, longitude: 77.6050, timestamp: 8000 },
      { latitude: 12.9775, longitude: 77.6070, timestamp: 9000 },
      { latitude: 12.9750, longitude: 77.6080, timestamp: 10000 },
      { latitude: 12.9720, longitude: 77.6065, timestamp: 11000 },
      { latitude: 12.9695, longitude: 77.6040, timestamp: 12000 },
      { latitude: 12.9675, longitude: 77.6005, timestamp: 13000 },
      { latitude: 12.9665, longitude: 77.5965, timestamp: 14000 },
      { latitude: 12.9670, longitude: 77.5925, timestamp: 15000 },
      { latitude: 12.9680, longitude: 77.5900, timestamp: 16000 }
    ]
  },
  {
    id: 'legendary_helix',
    name: 'Celestial Helix Expedition',
    tag: 'Legendary Multi-Sector Trek',
    projectedRarity: 'Legendary',
    color: '#EC4899',
    distanceMeters: 6200,
    coordinates: Array.from({ length: 30 }, (_, i) => {
      const angle = (i / 30) * Math.PI * 4;
      const radius = 0.008 * (1 + 0.3 * Math.sin(angle * 3));
      return {
        latitude: 12.9716 + radius * Math.cos(angle),
        longitude: 77.5945 + radius * Math.sin(angle) * 1.3,
        timestamp: 1000 + i * 1000
      };
    })
  }
];

export default function TrackerScreen({ navigation }) {
  const [userId, setUserId] = useState('student_creator');
  const [selectedRouteId, setSelectedRouteId] = useState('sculpture');
  const [isRecording, setIsRecording] = useState(false);
  const [elapsedSeconds, setElapsedSeconds] = useState(0);
  const [isMinting, setIsMinting] = useState(false);
  const [mintedArtwork, setMintedArtwork] = useState(null);
  const [showModal, setShowModal] = useState(false);

  // Phase 5 & 6 Hackathon Demo Simulator State
  const [isSimulatorMode, setIsSimulatorMode] = useState(false);
  const [simActive, setSimActive] = useState(false);
  const [simStep, setSimStep] = useState(0);
  const [simRouteData, setSimRouteData] = useState([]);

  const timerRef = useRef(null);
  const simTimerRef = useRef(null);

  const selectedRoute =
    PRESET_ROUTES.find((r) => r.id === selectedRouteId) || PRESET_ROUTES[0];

  // Standard walk timer
  useEffect(() => {
    if (isRecording && !simActive) {
      timerRef.current = setInterval(() => {
        setElapsedSeconds((prev) => prev + 1);
      }, 1000);
    } else {
      if (timerRef.current) clearInterval(timerRef.current);
    }
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [isRecording, simActive]);

  // Hackathon Demo 1-second Mock GPS Breadcrumb Interval
  useEffect(() => {
    if (simActive) {
      simTimerRef.current = setInterval(() => {
        setSimStep((currentStep) => {
          const nextStep = currentStep + 1;

          if (nextStep <= MOCK_STAR_COORDINATES.length) {
            const newPoint = {
              ...MOCK_STAR_COORDINATES[nextStep - 1],
              timestamp: Date.now()
            };
            setSimRouteData((prev) => [...prev, newPoint]);
            setElapsedSeconds(nextStep);
          }

          // Auto-Stop after 15 seconds
          if (nextStep >= 15) {
            clearInterval(simTimerRef.current);
            setSimActive(false);
            executeAutoStopAndMint(MOCK_STAR_COORDINATES);
            return 15;
          }

          return nextStep;
        });
      }, 1000);
    } else {
      if (simTimerRef.current) clearInterval(simTimerRef.current);
    }

    return () => {
      if (simTimerRef.current) clearInterval(simTimerRef.current);
    };
  }, [simActive]);

  // Auto-stop and submit function for Simulator
  const executeAutoStopAndMint = async (coordinatesToMint) => {
    try {
      setIsMinting(true);
      const startTime = new Date(Date.now() - 15000).toISOString();
      const endTime = new Date().toISOString();

      const result = await uploadWalkSession({
        userId: userId.trim() || 'student_creator',
        startTime,
        endTime,
        routeData: coordinatesToMint,
        notes: '✨ Hackathon Demo: 15s Celestial Star Walk Simulator',
        rarityOverride: 'Rare'
      });

      setIsRecording(false);
      setSimStep(0);
      setSimRouteData([]);

      const artwork = result.artwork || result.data?.artwork;
      if (artwork) {
        setMintedArtwork(artwork);
        setShowModal(true);
      } else {
        Alert.alert('Success', 'Hackathon demo completed and star artwork minted!');
      }
    } catch (error) {
      Alert.alert('Simulation Notice', error.message || 'Failed to auto-mint simulation art');
    } finally {
      setIsMinting(false);
    }
  };

  const handleStartSimulation = () => {
    setSimRouteData([]);
    setSimStep(0);
    setElapsedSeconds(0);
    setIsRecording(true);
    setSimActive(true);
  };

  const handleStopSimulation = () => {
    if (simTimerRef.current) clearInterval(simTimerRef.current);
    setSimActive(false);
    setIsRecording(false);
    if (simRouteData.length > 0) {
      executeAutoStopAndMint(simRouteData);
    }
  };

  const handleStartWalk = () => {
    setIsRecording(true);
  };

  const handlePauseWalk = () => {
    setIsRecording(false);
  };

  const handleResetWalk = () => {
    setIsRecording(false);
    setSimActive(false);
    setSimStep(0);
    setSimRouteData([]);
    setElapsedSeconds(0);
  };

  const handleFinishAndMint = async () => {
    try {
      setIsMinting(true);
      const startTime = new Date(Date.now() - (elapsedSeconds || 420) * 1000).toISOString();
      const endTime = new Date().toISOString();

      const routeToUpload =
        simRouteData.length > 0 ? simRouteData : selectedRoute.coordinates;

      const result = await uploadWalkSession({
        userId: userId.trim() || 'student_creator',
        startTime,
        endTime,
        routeData: routeToUpload,
        notes: `Route: ${selectedRoute.name}`,
        rarityOverride: selectedRoute.projectedRarity
      });

      setIsRecording(false);
      setElapsedSeconds(0);
      setSimRouteData([]);

      const artwork = result.artwork || result.data?.artwork;
      if (artwork) {
        setMintedArtwork(artwork);
        setShowModal(true);
      } else {
        Alert.alert('Success', 'Walk session uploaded and art minted!');
      }
    } catch (error) {
      Alert.alert('Notice', error.message || 'Failed to upload session and mint art');
    } finally {
      setIsMinting(false);
    }
  };

  const formatTimer = (secs) => {
    const m = Math.floor(secs / 60);
    const s = secs % 60;
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  const activePointsCount = simActive
    ? simRouteData.length
    : selectedRoute.coordinates.length;

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
      {/* Header */}
      <View style={styles.header}>
        <View style={styles.headerTopRow}>
          <Text style={styles.headerSubtitle}>CAMPUS ROUTE-TO-ART</Text>
          
          {/* Clean Integrated Simulator Button */}
          <TouchableOpacity
            style={[styles.devTogglePill, isSimulatorMode && styles.devTogglePillActive]}
            onPress={() => setIsSimulatorMode(!isSimulatorMode)}
          >
            <Text style={styles.devToggleText}>
              {isSimulatorMode ? '⚡ Demo Simulator: ON' : '⚡ Simulate Walk'}
            </Text>
          </TouchableOpacity>
        </View>
        <Text style={styles.headerTitle}>Walk Tracker & Mint</Text>
      </View>

      {/* Hackathon Demo Simulator Banner */}
      {isSimulatorMode && (
        <View style={styles.simulatorCard}>
          <View style={styles.simCardHeader}>
            <Text style={styles.simCardTitle}>⚡ 15s HACKATHON LIVE SIMULATOR</Text>
            <View style={styles.simBadge}>
              <Text style={styles.simBadgeText}>Demo Ready</Text>
            </View>
          </View>
          <Text style={styles.simCardDesc}>
            Streams mock GPS breadcrumbs every 1s tracing a recognizable 5-Pointed Star of Campus Art.
            Automatically stops & mints generative SVG at 15s.
          </Text>

          {/* Progress Bar */}
          <View style={styles.progressBarBg}>
            <View
              style={[
                styles.progressBarFill,
                { width: `${Math.min(100, (simStep / 15) * 100)}%` }
              ]}
            />
          </View>

          <View style={styles.simStatusRow}>
            <Text style={styles.simStepText}>
              Step: {simStep}/15 • {simActive ? 'Streaming GPS...' : simStep === 15 ? 'Completed! Minting...' : 'Ready'}
            </Text>
            {simActive && (
              <Text style={styles.simWaypointText}>
                {MOCK_STAR_COORDINATES[simStep - 1]?.name || 'Origin'}
              </Text>
            )}
          </View>

          {!simActive ? (
            <TouchableOpacity
              style={styles.startSimButton}
              onPress={handleStartSimulation}
              disabled={isMinting}
            >
              <Text style={styles.startSimButtonText}>
                🚀 Start 15s Star Walk Simulation
              </Text>
            </TouchableOpacity>
          ) : (
            <TouchableOpacity
              style={styles.stopSimButton}
              onPress={handleStopSimulation}
            >
              <Text style={styles.stopSimButtonText}>
                ⏹ Finish / Mint Art ({simStep}s)
              </Text>
            </TouchableOpacity>
          )}
        </View>
      )}

      {/* User Selector */}
      <View style={styles.userCard}>
        <Text style={styles.inputLabel}>ACTIVE CREATOR ID</Text>
        <TextInput
          style={styles.textInput}
          value={userId}
          onChangeText={setUserId}
          placeholder="Enter user id (e.g. student_creator)"
          placeholderTextColor={THEME.colors.textMuted}
          autoCapitalize="none"
        />
      </View>

      {/* Live Dashboard Card */}
      <View style={styles.dashboardCard}>
        <View style={styles.dashboardTop}>
          <View>
            <Text style={styles.timerLabel}>WALK DURATION</Text>
            <Text style={styles.timerValue}>{formatTimer(elapsedSeconds)}</Text>
          </View>
          <View
            style={[
              styles.rarityPill,
              {
                borderColor: simActive ? THEME.colors.neonRose : selectedRoute.color,
                backgroundColor: simActive ? 'rgba(244, 63, 94, 0.2)' : `${selectedRoute.color}20`
              }
            ]}
          >
            <View
              style={[
                styles.rarityDot,
                { backgroundColor: simActive ? THEME.colors.neonRose : selectedRoute.color }
              ]}
            />
            <Text
              style={[
                styles.rarityText,
                { color: simActive ? THEME.colors.neonRose : selectedRoute.color }
              ]}
            >
              {simActive ? 'Star (Rare)' : selectedRoute.projectedRarity}
            </Text>
          </View>
        </View>

        <View style={styles.metricsRow}>
          <View style={styles.metricItem}>
            <Text style={styles.metricLabel}>Distance</Text>
            <Text style={styles.metricValue}>
              {simActive
                ? `${(simStep * 110).toFixed(0)} m`
                : selectedRoute.distanceMeters >= 1000
                ? `${(selectedRoute.distanceMeters / 1000).toFixed(1)} km`
                : `${selectedRoute.distanceMeters} m`}
            </Text>
          </View>
          <View style={styles.metricDivider} />
          <View style={styles.metricItem}>
            <Text style={styles.metricLabel}>GPS Points</Text>
            <Text style={styles.metricValue}>{activePointsCount}</Text>
          </View>
          <View style={styles.metricDivider} />
          <View style={styles.metricItem}>
            <Text style={styles.metricLabel}>Mode</Text>
            <Text style={[styles.metricValue, { color: simActive ? THEME.colors.neonRose : THEME.colors.neonCyan }]}>
              {simActive ? 'Mock GPS' : 'Live GPS'}
            </Text>
          </View>
        </View>

        {/* Live Controls */}
        <View style={styles.controlRow}>
          {!isRecording ? (
            <TouchableOpacity style={styles.startButton} onPress={handleStartWalk}>
              <Text style={styles.buttonText}>▶ Start Walk</Text>
            </TouchableOpacity>
          ) : (
            <TouchableOpacity style={styles.pauseButton} onPress={handlePauseWalk}>
              <Text style={styles.buttonText}>⏸ Pause Walk</Text>
            </TouchableOpacity>
          )}
          <TouchableOpacity style={styles.resetButton} onPress={handleResetWalk}>
            <Text style={styles.resetText}>Reset</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Campus Route Presets */}
      <View style={styles.presetSection}>
        <Text style={styles.sectionHeading}>SELECT CAMPUS ROUTE TRAIL</Text>
        <Text style={styles.sectionSub}>Choose a route path to record or simulate:</Text>

        {PRESET_ROUTES.map((route) => {
          const isSelected = route.id === selectedRouteId && !simActive;
          return (
            <TouchableOpacity
              key={route.id}
              activeOpacity={0.8}
              style={[
                styles.routeCard,
                isSelected && {
                  borderColor: route.color,
                  backgroundColor: 'rgba(30, 41, 59, 0.9)'
                }
              ]}
              onPress={() => setSelectedRouteId(route.id)}
            >
              <View style={styles.routeCardLeft}>
                <View style={[styles.routeIconBox, { backgroundColor: `${route.color}25` }]}>
                  <View style={[styles.routeIndicatorDot, { backgroundColor: route.color }]} />
                </View>
                <View>
                  <Text style={styles.routeName}>{route.name}</Text>
                  <Text style={styles.routeTag}>{route.tag}</Text>
                </View>
              </View>

              <View style={styles.routeCardRight}>
                <Text style={styles.routeDistance}>
                  {route.distanceMeters >= 1000
                    ? `${(route.distanceMeters / 1000).toFixed(1)} km`
                    : `${route.distanceMeters} m`}
                </Text>
                <Text style={[styles.routeRarity, { color: route.color }]}>
                  {route.projectedRarity}
                </Text>
              </View>
            </TouchableOpacity>
          );
        })}
      </View>

      {/* Primary Minting CTA */}
      <TouchableOpacity
        activeOpacity={0.85}
        disabled={isMinting}
        style={[styles.mintButton, isMinting && { opacity: 0.6 }]}
        onPress={handleFinishAndMint}
      >
        {isMinting ? (
          <View style={styles.mintingRow}>
            <ActivityIndicator size="small" color="#FFFFFF" />
            <Text style={styles.mintButtonText}> Minting Generative Artwork...</Text>
          </View>
        ) : (
          <Text style={styles.mintButtonText}>✨ Finish Walk & Mint Art</Text>
        )}
      </TouchableOpacity>

      {/* Minted Artwork Modal */}
      <ArtworkModal
        visible={showModal}
        artwork={mintedArtwork}
        onClose={() => {
          setShowModal(false);
          if (navigation && navigation.navigate) {
            navigation.navigate('Gallery');
          }
        }}
      />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: THEME.colors.background
  },
  contentContainer: {
    padding: THEME.spacing.lg,
    paddingTop: Platform.OS === 'ios' ? 60 : 40,
    paddingBottom: 60
  },
  header: {
    marginBottom: 16
  },
  headerTopRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center'
  },
  headerSubtitle: {
    ...THEME.typography.subtitle
  },
  devTogglePill: {
    paddingHorizontal: 12,
    paddingVertical: 5,
    borderRadius: THEME.radii.sm,
    backgroundColor: THEME.colors.surfaceElevated,
    borderWidth: 1,
    borderColor: THEME.colors.borderSubtle
  },
  devTogglePillActive: {
    backgroundColor: 'rgba(56, 189, 248, 0.2)',
    borderColor: THEME.colors.neonCyan
  },
  devToggleText: {
    color: THEME.colors.neonCyan,
    fontSize: 12,
    fontWeight: '700'
  },
  headerTitle: {
    ...THEME.typography.headerTitle,
    marginTop: 4
  },
  simulatorCard: {
    backgroundColor: 'rgba(236, 72, 153, 0.12)',
    borderRadius: THEME.radii.xl,
    padding: 16,
    borderWidth: 1.5,
    borderColor: THEME.colors.neonRose,
    marginBottom: 18,
    shadowColor: THEME.colors.neonRose,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.2,
    shadowRadius: 8
  },
  simCardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 6
  },
  simCardTitle: {
    color: THEME.colors.neonRose,
    fontSize: 12,
    fontWeight: '800',
    letterSpacing: 1
  },
  simBadge: {
    backgroundColor: THEME.colors.neonRose,
    borderRadius: THEME.radii.sm,
    paddingHorizontal: 6,
    paddingVertical: 2
  },
  simBadgeText: {
    color: '#FFFFFF',
    fontSize: 9,
    fontWeight: '800'
  },
  simCardDesc: {
    color: THEME.colors.textSecondary,
    fontSize: 12,
    lineHeight: 16,
    marginBottom: 12
  },
  progressBarBg: {
    height: 8,
    backgroundColor: THEME.colors.surfaceElevated,
    borderRadius: 4,
    overflow: 'hidden',
    marginBottom: 8
  },
  progressBarFill: {
    height: '100%',
    backgroundColor: THEME.colors.neonRose,
    borderRadius: 4
  },
  simStatusRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12
  },
  simStepText: {
    color: THEME.colors.textPrimary,
    fontSize: 11,
    fontWeight: '700'
  },
  simWaypointText: {
    color: THEME.colors.neonCyan,
    fontSize: 10,
    fontWeight: '600'
  },
  startSimButton: {
    backgroundColor: THEME.colors.neonRose,
    borderRadius: THEME.radii.md,
    paddingVertical: 12,
    alignItems: 'center'
  },
  startSimButtonText: {
    color: '#FFFFFF',
    fontSize: 13,
    fontWeight: '800'
  },
  stopSimButton: {
    backgroundColor: THEME.colors.error,
    borderRadius: THEME.radii.md,
    paddingVertical: 12,
    alignItems: 'center'
  },
  stopSimButtonText: {
    color: '#FFFFFF',
    fontSize: 13,
    fontWeight: '800'
  },
  userCard: {
    backgroundColor: THEME.colors.surface,
    borderRadius: THEME.radii.lg,
    padding: 14,
    borderWidth: 1,
    borderColor: THEME.colors.borderDefault,
    marginBottom: 16
  },
  inputLabel: {
    color: THEME.colors.textMuted,
    fontSize: 10,
    fontWeight: '700',
    letterSpacing: 1,
    marginBottom: 6
  },
  textInput: {
    backgroundColor: THEME.colors.surfaceElevated,
    borderRadius: THEME.radii.sm,
    paddingHorizontal: 12,
    paddingVertical: 8,
    color: THEME.colors.textPrimary,
    fontSize: 14,
    fontWeight: '600',
    borderWidth: 1,
    borderColor: THEME.colors.borderSubtle
  },
  dashboardCard: {
    backgroundColor: THEME.colors.surface,
    borderRadius: THEME.radii.xl,
    padding: 18,
    borderWidth: 1.5,
    borderColor: THEME.colors.borderDefault,
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.4,
    shadowRadius: 10,
    elevation: 6
  },
  dashboardTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16
  },
  timerLabel: {
    color: THEME.colors.textSecondary,
    fontSize: 11,
    fontWeight: '600',
    letterSpacing: 0.5
  },
  timerValue: {
    color: THEME.colors.textPrimary,
    fontSize: 32,
    fontWeight: '800',
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
    marginTop: 2
  },
  rarityPill: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: THEME.radii.full,
    borderWidth: 1.5
  },
  rarityDot: {
    width: 7,
    height: 7,
    borderRadius: 3.5,
    marginRight: 6
  },
  rarityText: {
    fontSize: 11,
    fontWeight: '700',
    textTransform: 'uppercase'
  },
  metricsRow: {
    flexDirection: 'row',
    backgroundColor: THEME.colors.surfaceSubtle,
    borderRadius: THEME.radii.md,
    padding: 12,
    justifyContent: 'space-around',
    alignItems: 'center',
    marginBottom: 16
  },
  metricItem: {
    alignItems: 'center'
  },
  metricLabel: {
    color: THEME.colors.textMuted,
    fontSize: 10,
    fontWeight: '600'
  },
  metricValue: {
    color: THEME.colors.textPrimary,
    fontSize: 14,
    fontWeight: '700',
    marginTop: 2
  },
  metricDivider: {
    width: 1,
    height: 24,
    backgroundColor: THEME.colors.borderDefault
  },
  controlRow: {
    flexDirection: 'row',
    gap: 10
  },
  startButton: {
    flex: 2,
    backgroundColor: THEME.colors.neonEmerald,
    borderRadius: THEME.radii.sm,
    paddingVertical: 12,
    alignItems: 'center'
  },
  pauseButton: {
    flex: 2,
    backgroundColor: THEME.colors.neonAmber,
    borderRadius: THEME.radii.sm,
    paddingVertical: 12,
    alignItems: 'center'
  },
  buttonText: {
    color: '#FFFFFF',
    fontSize: 14,
    fontWeight: '700'
  },
  resetButton: {
    flex: 1,
    backgroundColor: THEME.colors.surfaceElevated,
    borderRadius: THEME.radii.sm,
    paddingVertical: 12,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: THEME.colors.borderSubtle
  },
  resetText: {
    color: THEME.colors.textSecondary,
    fontSize: 14,
    fontWeight: '600'
  },
  presetSection: {
    marginBottom: 24
  },
  sectionHeading: {
    ...THEME.typography.sectionHeading,
    marginBottom: 4
  },
  sectionSub: {
    color: THEME.colors.textMuted,
    fontSize: 12,
    marginBottom: 12
  },
  routeCard: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: THEME.colors.surface,
    borderRadius: THEME.radii.md,
    padding: 14,
    borderWidth: 1.5,
    borderColor: THEME.colors.borderDefault,
    marginBottom: 10
  },
  routeCardLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12
  },
  routeIconBox: {
    width: 36,
    height: 36,
    borderRadius: 10,
    justifyContent: 'center',
    alignItems: 'center'
  },
  routeIndicatorDot: {
    width: 10,
    height: 10,
    borderRadius: 5
  },
  routeName: {
    color: THEME.colors.textPrimary,
    fontSize: 14,
    fontWeight: '600'
  },
  routeTag: {
    color: THEME.colors.textMuted,
    fontSize: 11,
    marginTop: 2
  },
  routeCardRight: {
    alignItems: 'flex-end'
  },
  routeDistance: {
    color: THEME.colors.textPrimary,
    fontSize: 13,
    fontWeight: '600'
  },
  routeRarity: {
    fontSize: 11,
    fontWeight: '700',
    marginTop: 2
  },
  mintButton: {
    backgroundColor: THEME.colors.neonPurple,
    borderRadius: THEME.radii.lg,
    paddingVertical: 16,
    alignItems: 'center',
    shadowColor: THEME.colors.neonPurple,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.4,
    shadowRadius: 10,
    elevation: 8,
    marginBottom: 20
  },
  mintingRow: {
    flexDirection: 'row',
    alignItems: 'center'
  },
  mintButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '800',
    letterSpacing: 0.5
  }
});
