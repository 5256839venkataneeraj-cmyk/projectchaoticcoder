import React, { useState, useEffect } from 'react';
import {
  StyleSheet,
  Text,
  View,
  FlatList,
  TouchableOpacity,
  ActivityIndicator
} from 'react-native';

const API_BASE_URL = 'http://localhost:5000/api';

export default function Leaderboard() {
  const [leaderboardData, setLeaderboardData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('dorms'); // 'dorms' or 'students'

  useEffect(() => {
    fetchLeaderboard();
  }, []);

  const fetchLeaderboard = async () => {
    try {
      setLoading(true);
      const res = await fetch(`${API_BASE_URL}/leaderboard`);
      const data = await res.json();
      if (data.success) {
        setLeaderboardData(data.leaderboard || []);
      }
    } catch (e) {
      console.warn('Fallback local leaderboard');
      setLeaderboardData([
        { rank: 1, name: 'Block D Dragons', dorm: 'Hostel Wing', department: '412 Artists', totalDistance: 148.9, totalSteps: 198400, currentStreak: 14, avatar: '🐉' },
        { rank: 2, name: 'Diya Patel', dorm: 'Ladies Hostel', department: 'Design Dept', totalDistance: 84.2, totalSteps: 112300, currentStreak: 9, avatar: '👩‍🎨' },
        { rank: 3, name: 'Block A Titans', dorm: 'Hostel Wing', department: '380 Artists', totalDistance: 78.5, totalSteps: 104200, currentStreak: 8, avatar: '⚡' },
        { rank: 4, name: 'Aarav Sharma', dorm: 'Block B', department: 'CS Dept', totalDistance: 64.8, totalSteps: 86400, currentStreak: 5, avatar: '🧑‍💻' },
        { rank: 5, name: 'Rohan Verma', dorm: 'Block C', department: 'Mech Dept', totalDistance: 52.1, totalSteps: 69500, currentStreak: 3, avatar: '🏃' },
        { rank: 6, name: 'Sneha Reddy', dorm: 'Ladies Hostel', department: 'Biotech Dept', totalDistance: 45.3, totalSteps: 60400, currentStreak: 2, avatar: '🌸' }
      ]);
    } finally {
      setLoading(false);
    }
  };

  const renderLeaderItem = ({ item }) => {
    const isTop3 = item.rank <= 3;
    const rankBadgeColor = item.rank === 1 ? '#FFD700' : item.rank === 2 ? '#C0C0C0' : item.rank === 3 ? '#CD7F32' : '#30363D';

    return (
      <View style={[styles.itemCard, isTop3 && styles.topCard]}>
        {/* Rank */}
        <View style={[styles.rankBadge, { backgroundColor: rankBadgeColor }]}>
          <Text style={[styles.rankText, isTop3 && { color: '#000000' }]}>{item.rank}</Text>
        </View>

        {/* Avatar */}
        <Text style={styles.avatarEmoji}>{item.avatar || '🏃'}</Text>

        {/* User / Dorm Info */}
        <View style={styles.infoCol}>
          <Text style={styles.userName}>{item.name}</Text>
          <Text style={styles.userSub}>{item.dorm} • {item.department}</Text>
        </View>

        {/* Score & Streak */}
        <View style={styles.scoreCol}>
          <Text style={styles.scoreText}>{item.totalDistance} km</Text>
          <View style={styles.streakPill}>
            <Text style={styles.streakPillText}>🔥 {item.currentStreak}d</Text>
          </View>
        </View>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Campus Leaderboard</Text>
        <Text style={styles.headerSubtitle}>Top campus walking artists & hostel wings</Text>
      </View>

      {/* Tabs */}
      <View style={styles.tabsRow}>
        <TouchableOpacity
          style={[styles.tabBtn, activeTab === 'dorms' && styles.activeTabBtn]}
          onPress={() => setActiveTab('dorms')}
        >
          <Text style={[styles.tabText, activeTab === 'dorms' && styles.activeTabText]}>Hostel Wings</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.tabBtn, activeTab === 'students' && styles.activeTabBtn]}
          onPress={() => setActiveTab('students')}
        >
          <Text style={[styles.tabText, activeTab === 'students' && styles.activeTabText]}>Student Artists</Text>
        </TouchableOpacity>
      </View>

      {loading ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#00F5D4" />
        </View>
      ) : (
        <FlatList
          data={leaderboardData}
          keyExtractor={(item) => item.rank.toString()}
          renderItem={renderLeaderItem}
          contentContainerStyle={styles.listContent}
          showsVerticalScrollIndicator={false}
          onRefresh={fetchLeaderboard}
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
    paddingTop: 16
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
  tabsRow: {
    flexDirection: 'row',
    paddingHorizontal: 20,
    marginBottom: 12,
    gap: 8
  },
  tabBtn: {
    flex: 1,
    paddingVertical: 10,
    borderRadius: 14,
    backgroundColor: '#161B22',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#21262D'
  },
  activeTabBtn: {
    backgroundColor: '#21262D',
    borderColor: '#00F5D4'
  },
  tabText: {
    fontSize: 13,
    fontWeight: '700',
    color: '#8B949E'
  },
  activeTabText: {
    color: '#00F5D4'
  },
  listContent: {
    padding: 20,
    paddingTop: 4,
    paddingBottom: 40
  },
  itemCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#161B22',
    padding: 14,
    borderRadius: 18,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: '#21262D'
  },
  topCard: {
    borderColor: '#30363D'
  },
  rankBadge: {
    width: 28,
    height: 28,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10
  },
  rankText: {
    fontSize: 12,
    fontWeight: '800',
    color: '#8B949E'
  },
  avatarEmoji: {
    fontSize: 24,
    marginRight: 12
  },
  infoCol: {
    flex: 1
  },
  userName: {
    fontSize: 15,
    fontWeight: '700',
    color: '#F0F6FC'
  },
  userSub: {
    fontSize: 11,
    color: '#8B949E',
    marginTop: 2
  },
  scoreCol: {
    alignItems: 'flex-end'
  },
  scoreText: {
    fontSize: 14,
    fontWeight: '800',
    color: '#00F5D4'
  },
  streakPill: {
    backgroundColor: '#0D1117',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 8,
    marginTop: 4
  },
  streakPillText: {
    fontSize: 10,
    color: '#FFD700',
    fontWeight: '700'
  },
  loadingContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center'
  }
});
