import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  RefreshControl,
  TouchableOpacity,
  ActivityIndicator,
  TextInput,
  Dimensions,
  Platform
} from 'react-native';
import { fetchUserGallery } from '../services/api';
import { shareArtwork } from '../services/shareService';
import ArtworkCard from '../components/ArtworkCard';
import ArtworkModal from '../components/ArtworkModal';
import { THEME } from '../theme';

const FILTER_TABS = ['All', 'Common', 'Uncommon', 'Rare', 'Epic', 'Legendary'];

export default function GalleryScreen({ navigation }) {
  const [userId, setUserId] = useState('student_creator');
  const [artworks, setArtworks] = useState([]);
  const [selectedRarity, setSelectedRarity] = useState('All');
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [selectedArtwork, setSelectedArtwork] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [error, setError] = useState(null);

  const loadGallery = useCallback(
    async (isRefresh = false) => {
      if (!isRefresh) setLoading(true);
      setError(null);
      try {
        const result = await fetchUserGallery(
          userId.trim() || 'student_creator',
          selectedRarity
        );
        setArtworks(result.data || []);
      } catch (err) {
        setError(err.message || 'Could not load gallery artworks');
      } finally {
        setLoading(false);
        if (isRefresh) setRefreshing(false);
      }
    },
    [userId, selectedRarity]
  );

  useEffect(() => {
    loadGallery();
  }, [loadGallery]);

  const onRefresh = () => {
    setRefreshing(true);
    loadGallery(true);
  };

  const handleCardPress = (artwork) => {
    setSelectedArtwork(artwork);
    setShowModal(true);
  };

  const handleShareArtwork = async (artwork) => {
    await shareArtwork(artwork);
  };

  // Group artworks into 2-column pairs for clean grid layout
  const gridPairs = [];
  for (let i = 0; i < artworks.length; i += 2) {
    gridPairs.push([artworks[i], artworks[i + 1]]);
  }

  return (
    <View style={styles.container}>
      <ScrollView
        contentContainerStyle={styles.contentContainer}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            tintColor={THEME.colors.neonCyan}
            colors={[THEME.colors.neonCyan]}
          />
        }
      >
        {/* Header */}
        <View style={styles.header}>
          <View>
            <Text style={styles.headerSubtitle}>PERSONAL VAULT</Text>
            <Text style={styles.headerTitle}>Generative Art Gallery</Text>
          </View>
          <View style={styles.countBadge}>
            <Text style={styles.countText}>{artworks.length}</Text>
            <Text style={styles.countLabel}>Pieces</Text>
          </View>
        </View>

        {/* User Search Bar */}
        <View style={styles.userBar}>
          <Text style={styles.userLabel}>CREATOR ID:</Text>
          <TextInput
            style={styles.userInput}
            value={userId}
            onChangeText={setUserId}
            onSubmitEditing={() => loadGallery(false)}
            placeholder="student_creator"
            placeholderTextColor={THEME.colors.textMuted}
            autoCapitalize="none"
          />
          <TouchableOpacity style={styles.searchButton} onPress={() => loadGallery(false)}>
            <Text style={styles.searchButtonText}>Filter</Text>
          </TouchableOpacity>
        </View>

        {/* Filter Chips Bar */}
        <ScrollView
          horizontal
          showsHorizontalScrollIndicator={false}
          style={styles.filterScroll}
          contentContainerStyle={styles.filterContainer}
        >
          {FILTER_TABS.map((tab) => {
            const isActive = selectedRarity === tab;
            const rarityTheme = THEME.rarity[tab];
            return (
              <TouchableOpacity
                key={tab}
                style={[
                  styles.filterChip,
                  isActive && styles.filterChipActive,
                  isActive && rarityTheme && { backgroundColor: rarityTheme.color, borderColor: rarityTheme.color }
                ]}
                onPress={() => setSelectedRarity(tab)}
              >
                <Text
                  style={[
                    styles.filterChipText,
                    isActive && styles.filterChipTextActive
                  ]}
                >
                  {tab}
                </Text>
              </TouchableOpacity>
            );
          })}
        </ScrollView>

        {/* Loading Spinner */}
        {loading && !refreshing && (
          <View style={styles.loadingContainer}>
            <ActivityIndicator size="large" color={THEME.colors.neonCyan} />
            <Text style={styles.loadingText}>Fetching generative artefacts...</Text>
          </View>
        )}

        {/* Error State */}
        {!loading && error && (
          <View style={styles.errorCard}>
            <Text style={styles.errorTitle}>Connection Issue</Text>
            <Text style={styles.errorSub}>{error}</Text>
            <TouchableOpacity style={styles.retryButton} onPress={() => loadGallery(false)}>
              <Text style={styles.retryButtonText}>Retry Connection</Text>
            </TouchableOpacity>
          </View>
        )}

        {/* Enhanced Empty State with Visual Illustration */}
        {!loading && !error && artworks.length === 0 && (
          <View style={styles.emptyCard}>
            <View style={styles.emptyIconCircle}>
              <Text style={styles.emptyEmoji}>🎨</Text>
            </View>
            <Text style={styles.emptyTitle}>Canvas Awaiting Footprints</Text>
            <Text style={styles.emptySub}>
              {selectedRarity !== 'All'
                ? `No ${selectedRarity} tier artworks found in user "${userId}"'s collection.`
                : `User "${userId}" hasn't generated any route art yet. Start a campus journey or run the 15s simulator to mint your first piece!`}
            </Text>

            <TouchableOpacity
              style={styles.startWalkButton}
              onPress={() => {
                if (navigation && navigation.navigate) {
                  navigation.navigate('Tracker');
                }
              }}
            >
              <Text style={styles.startWalkButtonText}>⚡ Record Walk or Run Simulator</Text>
            </TouchableOpacity>
          </View>
        )}

        {/* 2-Column Art Card Grid */}
        {!loading && artworks.length > 0 && (
          <View style={styles.gridContainer}>
            {gridPairs.map((pair, rowIndex) => (
              <View key={`row-${rowIndex}`} style={styles.gridRow}>
                <ArtworkCard
                  artwork={pair[0]}
                  width="48%"
                  onPress={handleCardPress}
                  onShare={handleShareArtwork}
                />
                {pair[1] ? (
                  <ArtworkCard
                    artwork={pair[1]}
                    width="48%"
                    onPress={handleCardPress}
                    onShare={handleShareArtwork}
                  />
                ) : (
                  <View style={styles.emptyGridSlot} />
                )}
              </View>
            ))}
          </View>
        )}
      </ScrollView>

      {/* Detailed Inspection Modal with Share dialog */}
      <ArtworkModal
        visible={showModal}
        artwork={selectedArtwork}
        onClose={() => setShowModal(false)}
      />
    </View>
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
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16
  },
  headerSubtitle: {
    ...THEME.typography.subtitle
  },
  headerTitle: {
    ...THEME.typography.headerTitle,
    marginTop: 4
  },
  countBadge: {
    backgroundColor: THEME.colors.surfaceElevated,
    borderRadius: THEME.radii.md,
    paddingHorizontal: 14,
    paddingVertical: 6,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: THEME.colors.borderSubtle
  },
  countText: {
    color: THEME.colors.neonCyan,
    fontSize: 18,
    fontWeight: '800'
  },
  countLabel: {
    color: THEME.colors.textSecondary,
    fontSize: 10,
    fontWeight: '600'
  },
  userBar: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: THEME.colors.surface,
    borderRadius: THEME.radii.md,
    padding: 8,
    paddingLeft: 14,
    borderWidth: 1,
    borderColor: THEME.colors.borderDefault,
    marginBottom: 14
  },
  userLabel: {
    color: THEME.colors.textMuted,
    fontSize: 10,
    fontWeight: '700',
    letterSpacing: 0.5,
    marginRight: 8
  },
  userInput: {
    flex: 1,
    color: THEME.colors.textPrimary,
    fontSize: 13,
    fontWeight: '600',
    paddingVertical: 4
  },
  searchButton: {
    backgroundColor: THEME.colors.surfaceElevated,
    borderRadius: THEME.radii.sm,
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderWidth: 1,
    borderColor: THEME.colors.borderSubtle
  },
  searchButtonText: {
    color: THEME.colors.neonCyan,
    fontSize: 12,
    fontWeight: '700'
  },
  filterScroll: {
    marginBottom: 20
  },
  filterContainer: {
    gap: 8
  },
  filterChip: {
    paddingHorizontal: 14,
    paddingVertical: 7,
    borderRadius: THEME.radii.full,
    backgroundColor: THEME.colors.surface,
    borderWidth: 1,
    borderColor: THEME.colors.borderDefault
  },
  filterChipActive: {
    backgroundColor: THEME.colors.neonCyan,
    borderColor: THEME.colors.neonCyan
  },
  filterChipText: {
    color: THEME.colors.textSecondary,
    fontSize: 12,
    fontWeight: '600'
  },
  filterChipTextActive: {
    color: '#090D16',
    fontWeight: '700'
  },
  loadingContainer: {
    paddingVertical: 60,
    alignItems: 'center'
  },
  loadingText: {
    color: THEME.colors.textSecondary,
    fontSize: 13,
    marginTop: 12
  },
  errorCard: {
    backgroundColor: 'rgba(239, 68, 68, 0.1)',
    borderRadius: THEME.radii.lg,
    padding: 20,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: 'rgba(239, 68, 68, 0.3)',
    marginTop: 20
  },
  errorTitle: {
    color: THEME.colors.error,
    fontSize: 16,
    fontWeight: '700',
    marginBottom: 6
  },
  errorSub: {
    color: THEME.colors.textSecondary,
    fontSize: 12,
    textAlign: 'center',
    marginBottom: 14
  },
  retryButton: {
    backgroundColor: THEME.colors.error,
    borderRadius: THEME.radii.sm,
    paddingHorizontal: 20,
    paddingVertical: 8
  },
  retryButtonText: {
    color: '#FFFFFF',
    fontSize: 13,
    fontWeight: '700'
  },
  emptyCard: {
    backgroundColor: THEME.colors.surface,
    borderRadius: THEME.radii.xl,
    padding: 32,
    alignItems: 'center',
    borderWidth: 1.5,
    borderColor: THEME.colors.borderDefault,
    borderStyle: 'dashed',
    marginTop: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.3,
    shadowRadius: 10
  },
  emptyIconCircle: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: 'rgba(56, 189, 248, 0.12)',
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1.5,
    borderColor: THEME.colors.borderGlow,
    marginBottom: 16
  },
  emptyEmoji: {
    fontSize: 38
  },
  emptyTitle: {
    color: THEME.colors.textPrimary,
    fontSize: 18,
    fontWeight: '800',
    marginBottom: 8,
    textAlign: 'center'
  },
  emptySub: {
    color: THEME.colors.textSecondary,
    fontSize: 13,
    textAlign: 'center',
    marginBottom: 22,
    lineHeight: 19
  },
  startWalkButton: {
    backgroundColor: THEME.colors.neonPurple,
    borderRadius: THEME.radii.md,
    paddingHorizontal: 22,
    paddingVertical: 13,
    shadowColor: THEME.colors.neonPurple,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.4,
    shadowRadius: 8,
    elevation: 5
  },
  startWalkButtonText: {
    color: '#FFFFFF',
    fontSize: 14,
    fontWeight: '800'
  },
  gridContainer: {
    width: '100%'
  },
  gridRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 4
  },
  emptyGridSlot: {
    width: '48%'
  }
});
