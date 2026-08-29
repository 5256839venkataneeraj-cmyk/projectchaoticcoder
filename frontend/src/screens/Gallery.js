import React, { useState, useEffect } from 'react';
import {
  StyleSheet,
  Text,
  View,
  FlatList,
  TouchableOpacity,
  ActivityIndicator,
  Share,
  Alert,
  Dimensions
} from 'react-native';
import { SvgXml } from 'react-native-svg';

const { width } = Dimensions.get('window');
const CARD_SIZE = width - 40;
const API_BASE_URL = 'http://localhost:5000/api';

export default function Gallery() {
  const [artworks, setArtworks] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchArtworks();
  }, []);

  const fetchArtworks = async () => {
    try {
      setLoading(true);
      const res = await fetch(`${API_BASE_URL}/artworks/user/demo_student_user`);
      const data = await res.json();
      if (data.success) {
        setArtworks(data.artworks || []);
      }
    } catch (e) {
      console.warn('Fallback local demo artworks');
      setArtworks([
        {
          id: 'local_1',
          title: "Today's Campus Bloom",
          streakTier: 'NEON_CYBER',
          stats: { distanceKm: 5.6, steps: 7842, durationMinutes: 48 },
          createdAt: new Date().toISOString(),
          svgString: `
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 500 500" width="500" height="500">
              <rect width="100%" height="100%" fill="#0D1117" rx="28" />
              <ellipse cx="250" cy="250" rx="90" ry="70" fill="#00F5D4" fill-opacity="0.3" />
              <ellipse cx="280" cy="220" rx="80" ry="60" fill="#FF007F" fill-opacity="0.3" />
              <path d="M 120 280 C 180 120, 320 120, 380 280 C 320 420, 180 420, 120 280" fill="none" stroke="#00F5D4" stroke-width="4.5" stroke-linecap="round" />
            </svg>
          `
        },
        {
          id: 'local_2',
          title: "Central Library Butterfly",
          streakTier: 'CELESTIAL_GOLD',
          stats: { distanceKm: 6.4, steps: 9120, durationMinutes: 56 },
          createdAt: new Date(Date.now() - 86400000).toISOString(),
          svgString: `
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 500 500" width="500" height="500">
              <rect width="100%" height="100%" fill="#0D1117" rx="28" />
              <ellipse cx="220" cy="240" rx="75" ry="95" fill="#FFD700" fill-opacity="0.3" />
              <ellipse cx="280" cy="240" rx="75" ry="95" fill="#FF007F" fill-opacity="0.3" />
              <path d="M 250 140 C 140 180, 150 340, 250 360 C 350 340, 360 180, 250 140" fill="none" stroke="#FFD700" stroke-width="4.5" stroke-linecap="round" />
            </svg>
          `
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleShare = async (artwork) => {
    try {
      await Share.share({
        message: `🎨 Check out my Campus Route Artwork from my ${artwork.stats.distanceKm} km walk! Generated with Campus Route-to-Art.`
      });
    } catch (error) {
      Alert.alert('Share Error', error.message);
    }
  };

  const renderArtwork = ({ item }) => {
    const isNeon = item.streakTier === 'NEON_CYBER';
    const isGold = item.streakTier === 'CELESTIAL_GOLD';
    const badgeBg = isGold ? '#FFD700' : isNeon ? '#00F5D4' : '#2A9D8F';
    const badgeText = isGold ? '#000000' : '#0A0E14';

    return (
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <View style={{ flex: 1 }}>
            <Text style={styles.artTitle}>{item.title}</Text>
            <Text style={styles.artDate}>
              {new Date(item.createdAt).toLocaleDateString(undefined, { day: 'numeric', month: 'short', year: 'numeric' })}
            </Text>
          </View>
          <View style={[styles.tierBadge, { backgroundColor: badgeBg }]}>
            <Text style={[styles.tierBadgeText, { color: badgeText }]}>{item.streakTier}</Text>
          </View>
        </View>

        {/* SVG Render */}
        <View style={styles.svgWrapper}>
          {item.svgString ? (
            <SvgXml xml={item.svgString} width={CARD_SIZE - 32} height={CARD_SIZE - 32} />
          ) : (
            <View style={styles.placeholderSvg} />
          )}
        </View>

        {/* Stats and Share */}
        <View style={styles.cardFooter}>
          <View style={styles.statsInline}>
            <Text style={styles.statInlineText}>👟 {item.stats.steps.toLocaleString()} steps</Text>
            <Text style={styles.statInlineDot}>•</Text>
            <Text style={styles.statInlineText}>📍 {item.stats.distanceKm} km</Text>
            <Text style={styles.statInlineDot}>•</Text>
            <Text style={styles.statInlineText}>⏱️ {item.stats.durationMinutes}m</Text>
          </View>
          <TouchableOpacity style={styles.shareBtn} onPress={() => handleShare(item)}>
            <Text style={styles.shareBtnText}>Share ↗</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Route Art Gallery</Text>
        <Text style={styles.headerSubtitle}>Your personal campus walk art collection</Text>
      </View>

      {loading ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#00F5D4" />
          <Text style={styles.loadingText}>Loading your gallery...</Text>
        </View>
      ) : (
        <FlatList
          data={artworks}
          keyExtractor={(item) => item.id}
          renderItem={renderArtwork}
          contentContainerStyle={styles.listContent}
          showsVerticalScrollIndicator={false}
          onRefresh={fetchArtworks}
          refreshing={loading}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0A0E14'
  },
  header: {
    padding: 20,
    paddingTop: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#161B22'
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
  listContent: {
    padding: 20,
    paddingBottom: 40
  },
  card: {
    backgroundColor: '#161B22',
    borderRadius: 24,
    padding: 16,
    marginBottom: 20,
    borderWidth: 1,
    borderColor: '#21262D'
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 12
  },
  artTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#F0F6FC'
  },
  artDate: {
    fontSize: 12,
    color: '#8B949E',
    marginTop: 2
  },
  tierBadge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 8
  },
  tierBadgeText: {
    fontSize: 10,
    fontWeight: '800'
  },
  svgWrapper: {
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: 6,
    borderRadius: 18,
    overflow: 'hidden'
  },
  placeholderSvg: {
    width: CARD_SIZE - 32,
    height: CARD_SIZE - 32,
    backgroundColor: '#0D1117',
    borderRadius: 18
  },
  cardFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 12,
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: '#21262D'
  },
  statsInline: {
    flexDirection: 'row',
    alignItems: 'center'
  },
  statInlineText: {
    fontSize: 12,
    color: '#8B949E'
  },
  statInlineDot: {
    color: '#484F58',
    marginHorizontal: 6
  },
  shareBtn: {
    backgroundColor: '#21262D',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 12
  },
  shareBtnText: {
    color: '#00F5D4',
    fontSize: 12,
    fontWeight: '700'
  },
  loadingContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center'
  },
  loadingText: {
    color: '#8B949E',
    marginTop: 12
  }
});
