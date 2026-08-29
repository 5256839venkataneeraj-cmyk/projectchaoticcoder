import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  RefreshControl,
  TouchableOpacity,
  ActivityIndicator,
  Platform
} from 'react-native';
import { fetchLeaderboard } from '../services/api';
import { THEME } from '../theme';

const RANK_BADGES = {
  1: { emoji: '🥇', label: '1st', color: '#FCD34D', bg: 'rgba(252, 211, 77, 0.15)', border: '#FCD34D' },
  2: { emoji: '🥈', label: '2nd', color: '#CBD5E1', bg: 'rgba(203, 213, 225, 0.15)', border: '#94A3B8' },
  3: { emoji: '🥉', label: '3rd', color: '#F97316', bg: 'rgba(249, 115, 22, 0.15)', border: '#F97316' }
};

export default function LeaderboardScreen({ navigation }) {
  const [leaderboard, setLeaderboard] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(null);
  const [filterPeriod, setFilterPeriod] = useState('All-Time');

  const loadLeaderboardData = useCallback(async (isRefresh = false) => {
    if (!isRefresh) setLoading(true);
    setError(null);
    try {
      const res = await fetchLeaderboard();
      setLeaderboard(res.data || []);
    } catch (err) {
      setError(err.message || 'Could not load leaderboard rankings');
    } finally {
      setLoading(false);
      if (isRefresh) setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    loadLeaderboardData();
  }, [loadLeaderboardData]);

  const onRefresh = () => {
    setRefreshing(true);
    loadLeaderboardData(true);
  };

  const top3 = leaderboard.slice(0, 3);
  const rank1 = top3.find((u) => u.rank === 1) || top3[0];
  const rank2 = top3.find((u) => u.rank === 2) || top3[1];
  const rank3 = top3.find((u) => u.rank === 3) || top3[2];

  const renderLeaderboardItem = ({ item }) => {
    const isTop3 = item.rank <= 3;
    const badge = RANK_BADGES[item.rank];

    return (
      <View
        style={[
          styles.userRow,
          isTop3 && { borderColor: badge.border, backgroundColor: 'rgba(15, 23, 42, 0.9)' }
        ]}
      >
        {/* Rank Column */}
        <View style={styles.rankCol}>
          {badge ? (
            <View style={[styles.topRankBadge, { backgroundColor: badge.bg }]}>
              <Text style={styles.topRankEmoji}>{badge.emoji}</Text>
            </View>
          ) : (
            <Text style={styles.rankNumberText}>#{item.rank}</Text>
          )}
        </View>

        {/* User Info Column */}
        <View style={styles.userCol}>
          <View style={styles.userNameRow}>
            <Text style={styles.avatarEmoji}>{item.avatarEmoji || '🏃'}</Text>
            <Text style={styles.userNameText} numberOfLines={1}>
              {item.username}
            </Text>
          </View>
          <View style={styles.userSubRow}>
            <Text style={styles.userBadgeText}>{item.campusBadge || 'Campus Explorer'}</Text>
            <Text style={styles.dotSeparator}>•</Text>
            <Text style={styles.sessionsText}>{item.totalSessions || 1} walks</Text>
          </View>
        </View>

        {/* Stats Column */}
        <View style={styles.statsCol}>
          <Text style={styles.distanceValueText}>
            {item.totalDistance >= 1000
              ? `${(item.totalDistance / 1000).toFixed(1)} km`
              : `${Math.round(item.totalDistance)} m`}
          </Text>
          {item.currentStreak > 0 && (
            <View style={styles.streakPill}>
              <Text style={styles.streakText}>🔥 {item.currentStreak}d streak</Text>
            </View>
          )}
        </View>
      </View>
    );
  };

  const renderEmptyList = () => (
    <View style={styles.emptyContainer}>
      <View style={styles.emptyTrophyCircle}>
        <Text style={styles.emptyTrophyEmoji}>🏆</Text>
      </View>
      <Text style={styles.emptyTitle}>Be the Campus Pioneer</Text>
      <Text style={styles.emptySub}>
        No campus runners have logged walks yet. Record a walk to take #1 on the global leaderboard!
      </Text>
      <TouchableOpacity
        style={styles.emptyWalkButton}
        onPress={() => navigation && navigation.navigate && navigation.navigate('Tracker')}
      >
        <Text style={styles.emptyWalkButtonText}>⚡ Start First Walk</Text>
      </TouchableOpacity>
    </View>
  );

  const renderHeader = () => (
    <View style={styles.headerContainer}>
      {/* Title */}
      <View style={styles.titleSection}>
        <Text style={styles.headerSubtitle}>CAMPUS RUNNERS & ARTISANS</Text>
        <Text style={styles.headerTitle}>Leaderboard</Text>
      </View>

      {/* Period Filter Tabs */}
      <View style={styles.filterRow}>
        {['All-Time', 'This Week', 'Streaks'].map((tab) => {
          const isActive = filterPeriod === tab;
          return (
            <TouchableOpacity
              key={tab}
              style={[styles.filterTab, isActive && styles.filterTabActive]}
              onPress={() => setFilterPeriod(tab)}
            >
              <Text style={[styles.filterTabText, isActive && styles.filterTabTextActive]}>
                {tab}
              </Text>
            </TouchableOpacity>
          );
        })}
      </View>

      {/* Top 3 Podium Showcase Card */}
      {top3.length >= 2 && (
        <View style={styles.podiumCard}>
          <Text style={styles.podiumHeading}>🏆 TOP CAMPUS ARTISANS</Text>
          
          <View style={styles.podiumContainer}>
            {/* Rank 2 (Left) */}
            {rank2 && (
              <View style={[styles.podiumCol, styles.podiumCol2]}>
                <View style={styles.podiumAvatarBox}>
                  <Text style={styles.podiumAvatarEmoji}>{rank2.avatarEmoji || '🚀'}</Text>
                  <View style={[styles.podiumRankBadge, { backgroundColor: '#94A3B8' }]}>
                    <Text style={styles.podiumRankText}>2</Text>
                  </View>
                </View>
                <Text style={styles.podiumName} numberOfLines={1}>
                  {rank2.username?.split(' ')[0] || rank2.userId}
                </Text>
                <Text style={styles.podiumDistance}>
                  {((rank2.totalDistance || 0) / 1000).toFixed(1)} km
                </Text>
                <View style={[styles.podiumPillar, styles.pillar2]}>
                  <Text style={styles.pillarRank}>2nd</Text>
                </View>
              </View>
            )}

            {/* Rank 1 (Center - Tallest) */}
            {rank1 && (
              <View style={[styles.podiumCol, styles.podiumCol1]}>
                <View style={styles.podiumAvatarBox}>
                  <Text style={styles.podiumAvatarEmoji}>{rank1.avatarEmoji || '⚡'}</Text>
                  <View style={[styles.podiumRankBadge, { backgroundColor: '#FCD34D' }]}>
                    <Text style={[styles.podiumRankText, { color: '#000' }]}>👑</Text>
                  </View>
                </View>
                <Text style={styles.podiumName} numberOfLines={1}>
                  {rank1.username?.split(' ')[0] || rank1.userId}
                </Text>
                <Text style={[styles.podiumDistance, { color: '#FCD34D' }]}>
                  {((rank1.totalDistance || 0) / 1000).toFixed(1)} km
                </Text>
                <View style={[styles.podiumPillar, styles.pillar1]}>
                  <Text style={styles.pillarRankGold}>1st</Text>
                </View>
              </View>
            )}

            {/* Rank 3 (Right) */}
            {rank3 && (
              <View style={[styles.podiumCol, styles.podiumCol3]}>
                <View style={styles.podiumAvatarBox}>
                  <Text style={styles.podiumAvatarEmoji}>{rank3.avatarEmoji || '🎨'}</Text>
                  <View style={[styles.podiumRankBadge, { backgroundColor: '#F97316' }]}>
                    <Text style={styles.podiumRankText}>3</Text>
                  </View>
                </View>
                <Text style={styles.podiumName} numberOfLines={1}>
                  {rank3.username?.split(' ')[0] || rank3.userId}
                </Text>
                <Text style={styles.podiumDistance}>
                  {((rank3.totalDistance || 0) / 1000).toFixed(1)} km
                </Text>
                <View style={[styles.podiumPillar, styles.pillar3]}>
                  <Text style={styles.pillarRank}>3rd</Text>
                </View>
              </View>
            )}
          </View>
        </View>
      )}

      {leaderboard.length > 0 && (
        <Text style={styles.listHeading}>ALL RANKED WALKERS</Text>
      )}
    </View>
  );

  return (
    <View style={styles.container}>
      {loading && !refreshing ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color={THEME.colors.neonCyan} />
          <Text style={styles.loadingText}>Fetching campus leaderboard rankings...</Text>
        </View>
      ) : error ? (
        <View style={styles.errorContainer}>
          <Text style={styles.errorTitle}>Leaderboard Offline</Text>
          <Text style={styles.errorText}>{error}</Text>
          <TouchableOpacity style={styles.retryBtn} onPress={() => loadLeaderboardData(false)}>
            <Text style={styles.retryBtnText}>Retry Connection</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <FlatList
          data={leaderboard}
          keyExtractor={(item, index) => item.userId || `rank-${index}`}
          renderItem={renderLeaderboardItem}
          ListHeaderComponent={renderHeader}
          ListEmptyComponent={renderEmptyList}
          contentContainerStyle={styles.listContent}
          showsVerticalScrollIndicator={false}
          refreshControl={
            <RefreshControl
              refreshing={refreshing}
              onRefresh={onRefresh}
              tintColor={THEME.colors.neonCyan}
              colors={[THEME.colors.neonCyan]}
            />
          }
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: THEME.colors.background
  },
  listContent: {
    padding: THEME.spacing.lg,
    paddingTop: Platform.OS === 'ios' ? 60 : 40,
    paddingBottom: 60
  },
  headerContainer: {
    marginBottom: 10
  },
  titleSection: {
    marginBottom: 16
  },
  headerSubtitle: {
    ...THEME.typography.subtitle
  },
  headerTitle: {
    ...THEME.typography.headerTitle,
    marginTop: 4
  },
  filterRow: {
    flexDirection: 'row',
    backgroundColor: THEME.colors.surface,
    borderRadius: THEME.radii.md,
    padding: 4,
    marginBottom: 20,
    borderWidth: 1,
    borderColor: THEME.colors.borderDefault
  },
  filterTab: {
    flex: 1,
    paddingVertical: 8,
    alignItems: 'center',
    borderRadius: THEME.radii.sm
  },
  filterTabActive: {
    backgroundColor: THEME.colors.surfaceElevated
  },
  filterTabText: {
    color: THEME.colors.textSecondary,
    fontSize: 12,
    fontWeight: '600'
  },
  filterTabTextActive: {
    color: THEME.colors.neonCyan,
    fontWeight: '700'
  },
  podiumCard: {
    backgroundColor: THEME.colors.surface,
    borderRadius: THEME.radii.xl,
    padding: 18,
    borderWidth: 1.5,
    borderColor: THEME.colors.borderDefault,
    marginBottom: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.4,
    shadowRadius: 12,
    elevation: 6
  },
  podiumHeading: {
    color: THEME.colors.textSecondary,
    fontSize: 11,
    fontWeight: '700',
    letterSpacing: 1,
    textAlign: 'center',
    marginBottom: 16
  },
  podiumContainer: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    justifyContent: 'center',
    height: 180,
    paddingHorizontal: 8
  },
  podiumCol: {
    flex: 1,
    alignItems: 'center'
  },
  podiumCol1: {
    zIndex: 3
  },
  podiumCol2: {
    zIndex: 2
  },
  podiumCol3: {
    zIndex: 1
  },
  podiumAvatarBox: {
    position: 'relative',
    marginBottom: 6
  },
  podiumAvatarEmoji: {
    fontSize: 28
  },
  podiumRankBadge: {
    position: 'absolute',
    bottom: -4,
    right: -6,
    width: 18,
    height: 18,
    borderRadius: 9,
    justifyContent: 'center',
    alignItems: 'center'
  },
  podiumRankText: {
    color: '#FFFFFF',
    fontSize: 10,
    fontWeight: '800'
  },
  podiumName: {
    color: THEME.colors.textPrimary,
    fontSize: 12,
    fontWeight: '700',
    marginBottom: 2
  },
  podiumDistance: {
    color: THEME.colors.neonCyan,
    fontSize: 11,
    fontWeight: '600',
    marginBottom: 6
  },
  podiumPillar: {
    width: '85%',
    borderTopLeftRadius: 10,
    borderTopRightRadius: 10,
    alignItems: 'center',
    justifyContent: 'flex-start',
    paddingTop: 8
  },
  pillar1: {
    height: 80,
    backgroundColor: 'rgba(252, 211, 77, 0.25)',
    borderWidth: 1.5,
    borderColor: '#FCD34D'
  },
  pillar2: {
    height: 55,
    backgroundColor: 'rgba(203, 213, 225, 0.15)',
    borderWidth: 1,
    borderColor: '#94A3B8'
  },
  pillar3: {
    height: 40,
    backgroundColor: 'rgba(249, 115, 22, 0.15)',
    borderWidth: 1,
    borderColor: '#F97316'
  },
  pillarRankGold: {
    color: '#FCD34D',
    fontSize: 12,
    fontWeight: '800'
  },
  pillarRank: {
    color: THEME.colors.textSecondary,
    fontSize: 11,
    fontWeight: '700'
  },
  listHeading: {
    ...THEME.typography.sectionHeading,
    marginBottom: 12
  },
  userRow: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: THEME.colors.surface,
    borderRadius: THEME.radii.lg,
    padding: 14,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: THEME.colors.borderDefault
  },
  rankCol: {
    width: 36,
    alignItems: 'center',
    marginRight: 10
  },
  topRankBadge: {
    width: 32,
    height: 32,
    borderRadius: 16,
    justifyContent: 'center',
    alignItems: 'center'
  },
  topRankEmoji: {
    fontSize: 16
  },
  rankNumberText: {
    color: THEME.colors.textMuted,
    fontSize: 14,
    fontWeight: '700'
  },
  userCol: {
    flex: 1,
    marginRight: 8
  },
  userNameRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 2
  },
  avatarEmoji: {
    fontSize: 15,
    marginRight: 6
  },
  userNameText: {
    color: THEME.colors.textPrimary,
    fontSize: 14,
    fontWeight: '600',
    flexShrink: 1
  },
  userSubRow: {
    flexDirection: 'row',
    alignItems: 'center'
  },
  userBadgeText: {
    color: THEME.colors.neonCyan,
    fontSize: 10,
    fontWeight: '500'
  },
  dotSeparator: {
    color: THEME.colors.textDisabled,
    fontSize: 10,
    marginHorizontal: 4
  },
  sessionsText: {
    color: THEME.colors.textMuted,
    fontSize: 10
  },
  statsCol: {
    alignItems: 'flex-end'
  },
  distanceValueText: {
    color: THEME.colors.textPrimary,
    fontSize: 14,
    fontWeight: '700'
  },
  streakPill: {
    backgroundColor: 'rgba(245, 158, 11, 0.15)',
    borderRadius: THEME.radii.full,
    paddingHorizontal: 7,
    paddingVertical: 2,
    marginTop: 3,
    borderWidth: 1,
    borderColor: 'rgba(245, 158, 11, 0.3)'
  },
  streakText: {
    color: '#F59E0B',
    fontSize: 9,
    fontWeight: '700'
  },
  emptyContainer: {
    backgroundColor: THEME.colors.surface,
    borderRadius: THEME.radii.xl,
    padding: 30,
    alignItems: 'center',
    borderWidth: 1.5,
    borderColor: THEME.colors.borderDefault,
    borderStyle: 'dashed',
    marginTop: 20
  },
  emptyTrophyCircle: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: 'rgba(252, 211, 77, 0.12)',
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1.5,
    borderColor: 'rgba(252, 211, 77, 0.4)',
    marginBottom: 14
  },
  emptyTrophyEmoji: {
    fontSize: 34
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
    marginBottom: 20,
    lineHeight: 18
  },
  emptyWalkButton: {
    backgroundColor: THEME.colors.neonCyan,
    borderRadius: THEME.radii.md,
    paddingHorizontal: 20,
    paddingVertical: 12
  },
  emptyWalkButtonText: {
    color: '#090D16',
    fontSize: 13,
    fontWeight: '800'
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: 60
  },
  loadingText: {
    color: THEME.colors.textSecondary,
    fontSize: 13,
    marginTop: 12
  },
  errorContainer: {
    padding: 30,
    alignItems: 'center',
    marginTop: 60
  },
  errorTitle: {
    color: THEME.colors.error,
    fontSize: 18,
    fontWeight: '700',
    marginBottom: 6
  },
  errorText: {
    color: THEME.colors.textSecondary,
    fontSize: 13,
    textAlign: 'center',
    marginBottom: 16
  },
  retryBtn: {
    backgroundColor: THEME.colors.neonCyan,
    borderRadius: THEME.radii.sm,
    paddingHorizontal: 20,
    paddingVertical: 10
  },
  retryBtnText: {
    color: '#090D16',
    fontWeight: '800'
  }
});
