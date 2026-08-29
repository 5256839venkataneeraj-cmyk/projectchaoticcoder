import React, { useState } from 'react';
import {
  Modal,
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  Dimensions,
  Platform
} from 'react-native';
import ArtworkSvg from './ArtworkSvg';
import { shareArtwork } from '../services/shareService';
import { THEME } from '../theme';

export default function ArtworkModal({ visible, artwork, onClose }) {
  const [showRawSvg, setShowRawSvg] = useState(false);
  const [isSharing, setIsSharing] = useState(false);

  if (!artwork) return null;

  const rarity = artwork.rarity || 'Common';
  const theme = THEME.rarity[rarity] || THEME.rarity.Common;

  const distance = artwork.stats?.totalDistanceMeters || 0;
  const points = artwork.stats?.pointCount || 0;
  const duration = artwork.stats?.durationSeconds || 0;

  const formattedDistance =
    distance >= 1000
      ? `${(distance / 1000).toFixed(2)} km`
      : `${Math.round(distance)} meters`;

  const formattedDuration =
    duration >= 60
      ? `${Math.floor(duration / 60)}m ${duration % 60}s`
      : `${duration}s`;

  const dateObj = new Date(artwork.createdAt || Date.now());
  const formattedDate = dateObj.toLocaleDateString(undefined, {
    weekday: 'short',
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });

  const handleShare = async () => {
    setIsSharing(true);
    try {
      await shareArtwork(artwork);
    } finally {
      setIsSharing(false);
    }
  };

  return (
    <Modal
      visible={visible}
      animationType="slide"
      transparent={true}
      onRequestClose={onClose}
    >
      <View style={styles.modalOverlay}>
        <View style={styles.modalContent}>
          {/* Header */}
          <View style={styles.modalHeader}>
            <View
              style={[
                styles.rarityPill,
                {
                  backgroundColor: theme.bgColor,
                  borderColor: theme.borderColor
                }
              ]}
            >
              <View style={[styles.rarityDot, { backgroundColor: theme.color }]} />
              <Text style={[styles.rarityText, { color: theme.color }]}>
                {theme.label}
              </Text>
            </View>

            <TouchableOpacity style={styles.closeButton} onPress={onClose}>
              <Text style={styles.closeButtonText}>✕</Text>
            </TouchableOpacity>
          </View>

          <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={styles.scrollBody}>
            {/* Generative SVG Art Display */}
            <View
              style={[
                styles.svgDisplayCard,
                {
                  borderColor: theme.borderColor
                }
              ]}
            >
              <ArtworkSvg svgData={artwork.svgData} width={300} height={300} />
            </View>

            {/* Journey Stats Grid */}
            <View style={styles.statsSection}>
              <Text style={styles.sectionTitle}>JOURNEY METRICS</Text>
              <View style={styles.statsGrid}>
                <View style={styles.statBox}>
                  <Text style={styles.statEmoji}>🚶</Text>
                  <Text style={styles.statValue}>{formattedDistance}</Text>
                  <Text style={styles.statLabel}>Distance</Text>
                </View>
                <View style={styles.statBox}>
                  <Text style={styles.statEmoji}>⏱️</Text>
                  <Text style={styles.statValue}>{formattedDuration}</Text>
                  <Text style={styles.statLabel}>Duration</Text>
                </View>
                <View style={styles.statBox}>
                  <Text style={styles.statEmoji}>📍</Text>
                  <Text style={styles.statValue}>{points}</Text>
                  <Text style={styles.statLabel}>GPS Points</Text>
                </View>
              </View>
            </View>

            {/* Metadata Info */}
            <View style={styles.metaCard}>
              <View style={styles.metaRow}>
                <Text style={styles.metaLabel}>Minted By:</Text>
                <Text style={styles.metaValue}>{artwork.userId || 'student_creator'}</Text>
              </View>
              <View style={styles.metaRow}>
                <Text style={styles.metaLabel}>Timestamp:</Text>
                <Text style={styles.metaValue}>{formattedDate}</Text>
              </View>
              {artwork.sessionId && (
                <View style={styles.metaRow}>
                  <Text style={styles.metaLabel}>Session Ref:</Text>
                  <Text style={styles.metaValueMono}>
                    {typeof artwork.sessionId === 'string'
                      ? artwork.sessionId.substring(0, 16) + '...'
                      : artwork.sessionId}
                  </Text>
                </View>
              )}
            </View>

            {/* Toggle Raw SVG Code Button */}
            <TouchableOpacity
              style={styles.toggleRawButton}
              onPress={() => setShowRawSvg(!showRawSvg)}
            >
              <Text style={styles.toggleRawText}>
                {showRawSvg ? 'Hide SVG Code' : 'View Raw SVG XML Payload'}
              </Text>
            </TouchableOpacity>

            {showRawSvg && (
              <View style={styles.rawSvgBox}>
                <Text style={styles.rawSvgText}>{artwork.svgData}</Text>
              </View>
            )}

            {/* Action Buttons */}
            <View style={styles.actionRow}>
              <TouchableOpacity
                style={[styles.shareButton, isSharing && { opacity: 0.7 }]}
                disabled={isSharing}
                onPress={handleShare}
              >
                <Text style={styles.shareButtonText}>
                  {isSharing ? 'Exporting...' : '📤 Export & Share Art'}
                </Text>
              </TouchableOpacity>
              <TouchableOpacity style={styles.dismissButton} onPress={onClose}>
                <Text style={styles.dismissButtonText}>Done</Text>
              </TouchableOpacity>
            </View>
          </ScrollView>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  modalOverlay: {
    flex: 1,
    backgroundColor: THEME.colors.modalBackground,
    justifyContent: 'flex-end'
  },
  modalContent: {
    backgroundColor: THEME.colors.surface,
    borderTopLeftRadius: THEME.radii.xl,
    borderTopRightRadius: THEME.radii.xl,
    borderWidth: 1,
    borderColor: THEME.colors.borderSubtle,
    maxHeight: '92%',
    paddingTop: 16,
    paddingHorizontal: 20,
    paddingBottom: Platform.OS === 'ios' ? 36 : 24
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16
  },
  rarityPill: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 12,
    paddingVertical: 5,
    borderRadius: THEME.radii.full,
    borderWidth: 1.5
  },
  rarityDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginRight: 6
  },
  rarityText: {
    fontSize: 12,
    fontWeight: '700',
    letterSpacing: 0.5
  },
  closeButton: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: THEME.colors.surfaceElevated,
    justifyContent: 'center',
    alignItems: 'center'
  },
  closeButtonText: {
    color: THEME.colors.textSecondary,
    fontSize: 14,
    fontWeight: 'bold'
  },
  scrollBody: {
    alignItems: 'center',
    paddingBottom: 24
  },
  svgDisplayCard: {
    width: 300,
    height: 300,
    borderRadius: THEME.radii.xl,
    borderWidth: 2,
    backgroundColor: '#0B0F19',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.5,
    shadowRadius: 15,
    elevation: 8
  },
  statsSection: {
    width: '100%',
    marginBottom: 16
  },
  sectionTitle: {
    ...THEME.typography.sectionHeading,
    marginBottom: 8
  },
  statsGrid: {
    flexDirection: 'row',
    justifyContent: 'space-between'
  },
  statBox: {
    flex: 1,
    backgroundColor: THEME.colors.surfaceElevated,
    borderRadius: THEME.radii.md,
    padding: 10,
    marginHorizontal: 3,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: THEME.colors.borderSubtle
  },
  statEmoji: {
    fontSize: 18,
    marginBottom: 2
  },
  statValue: {
    color: THEME.colors.textPrimary,
    fontSize: 13,
    fontWeight: '700'
  },
  statLabel: {
    color: THEME.colors.textSecondary,
    fontSize: 10,
    marginTop: 2
  },
  metaCard: {
    width: '100%',
    backgroundColor: THEME.colors.surfaceElevated,
    borderRadius: THEME.radii.md,
    padding: 12,
    marginBottom: 14,
    borderWidth: 1,
    borderColor: THEME.colors.borderSubtle
  },
  metaRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginVertical: 3
  },
  metaLabel: {
    color: THEME.colors.textSecondary,
    fontSize: 12
  },
  metaValue: {
    color: THEME.colors.textPrimary,
    fontSize: 12,
    fontWeight: '600'
  },
  metaValueMono: {
    color: THEME.colors.neonCyan,
    fontSize: 11,
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace'
  },
  toggleRawButton: {
    paddingVertical: 8,
    marginBottom: 10
  },
  toggleRawText: {
    color: THEME.colors.neonCyan,
    fontSize: 12,
    fontWeight: '600',
    textDecorationLine: 'underline'
  },
  rawSvgBox: {
    width: '100%',
    backgroundColor: '#090D16',
    borderRadius: 10,
    padding: 10,
    marginBottom: 16,
    maxHeight: 120,
    borderWidth: 1,
    borderColor: THEME.colors.borderDefault
  },
  rawSvgText: {
    color: THEME.colors.textSecondary,
    fontSize: 10,
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace'
  },
  actionRow: {
    flexDirection: 'row',
    width: '100%',
    marginTop: 8,
    gap: 12
  },
  shareButton: {
    flex: 1,
    backgroundColor: THEME.colors.neonPurple,
    borderRadius: THEME.radii.md,
    paddingVertical: 14,
    alignItems: 'center'
  },
  shareButtonText: {
    color: '#FFFFFF',
    fontSize: 14,
    fontWeight: '700'
  },
  dismissButton: {
    flex: 1,
    backgroundColor: THEME.colors.surfaceElevated,
    borderRadius: THEME.radii.md,
    paddingVertical: 14,
    alignItems: 'center'
  },
  dismissButtonText: {
    color: THEME.colors.textPrimary,
    fontSize: 14,
    fontWeight: '700'
  }
});
