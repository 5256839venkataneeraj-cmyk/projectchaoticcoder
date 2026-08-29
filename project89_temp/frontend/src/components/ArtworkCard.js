import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Dimensions } from 'react-native';
import ArtworkSvg from './ArtworkSvg';
import { THEME } from '../theme';

export default function ArtworkCard({ artwork, onPress, onShare, width }) {
  const rarity = artwork.rarity || 'Common';
  const theme = THEME.rarity[rarity] || THEME.rarity.Common;

  const distance = artwork.stats?.totalDistanceMeters || 0;
  const points = artwork.stats?.pointCount || 0;

  const formattedDistance =
    distance >= 1000
      ? `${(distance / 1000).toFixed(1)} km`
      : `${Math.round(distance)} m`;

  const dateObj = new Date(artwork.createdAt || Date.now());
  const formattedDate = dateObj.toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric'
  });

  return (
    <TouchableOpacity
      activeOpacity={0.85}
      style={[
        styles.card,
        {
          borderColor: theme.borderColor,
          width: width || '48%'
        }
      ]}
      onPress={() => onPress && onPress(artwork)}
    >
      {/* SVG Canvas Preview */}
      <View style={styles.svgWrapper}>
        <ArtworkSvg svgData={artwork.svgData} width="100%" height={160} />
        
        {/* Rarity Badge Overlay */}
        <View
          style={[
            styles.rarityBadge,
            {
              backgroundColor: theme.bgColor,
              borderColor: theme.borderColor
            }
          ]}
        >
          <View style={[styles.rarityDot, { backgroundColor: theme.color }]} />
          <Text style={[styles.rarityText, { color: theme.color }]}>
            {theme.name || rarity}
          </Text>
        </View>

        {/* Quick Share Button */}
        {onShare && (
          <TouchableOpacity
            style={styles.quickShareButton}
            onPress={(e) => {
              e.stopPropagation();
              onShare(artwork);
            }}
          >
            <Text style={styles.quickShareText}>📤</Text>
          </TouchableOpacity>
        )}
      </View>

      {/* Card Info Footer */}
      <View style={styles.cardFooter}>
        <View style={styles.statRow}>
          <Text style={styles.distanceText}>📍 {formattedDistance}</Text>
          <Text style={styles.pointsText}>{points} pts</Text>
        </View>
        <Text style={styles.dateText}>{formattedDate}</Text>
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: THEME.colors.surface,
    borderRadius: THEME.radii.lg,
    borderWidth: 1.5,
    overflow: 'hidden',
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.35,
    shadowRadius: 8,
    elevation: 5
  },
  svgWrapper: {
    width: '100%',
    height: 160,
    backgroundColor: '#0B0F19',
    position: 'relative',
    justifyContent: 'center',
    alignItems: 'center'
  },
  rarityBadge: {
    position: 'absolute',
    top: 8,
    left: 8,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: THEME.radii.full,
    borderWidth: 1
  },
  rarityDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    marginRight: 4
  },
  rarityText: {
    fontSize: 10,
    fontWeight: '700',
    textTransform: 'uppercase',
    letterSpacing: 0.5
  },
  quickShareButton: {
    position: 'absolute',
    top: 8,
    right: 8,
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: 'rgba(15, 23, 42, 0.75)',
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: THEME.colors.borderSubtle
  },
  quickShareText: {
    fontSize: 12
  },
  cardFooter: {
    padding: 10,
    backgroundColor: THEME.colors.surface
  },
  statRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 4
  },
  distanceText: {
    color: THEME.colors.textPrimary,
    fontSize: 13,
    fontWeight: '600'
  },
  pointsText: {
    color: THEME.colors.textSecondary,
    fontSize: 11
  },
  dateText: {
    color: THEME.colors.textMuted,
    fontSize: 10,
    fontWeight: '500'
  }
});
