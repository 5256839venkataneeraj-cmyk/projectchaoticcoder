import React from 'react';
import { View, Text, StyleSheet, StatusBar, Platform } from 'react-native';
import { NavigationContainer, DarkTheme } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { SafeAreaProvider } from 'react-native-safe-area-context';

import TrackerScreen from './src/screens/TrackerScreen';
import GalleryScreen from './src/screens/GalleryScreen';
import LeaderboardScreen from './src/screens/LeaderboardScreen';
import { THEME } from './src/theme';

const Tab = createBottomTabNavigator();

const customDarkTheme = {
  ...DarkTheme,
  colors: {
    ...DarkTheme.colors,
    background: THEME.colors.background,
    card: THEME.colors.surface,
    text: THEME.colors.textPrimary,
    border: THEME.colors.borderDefault,
    primary: THEME.colors.neonCyan
  }
};

export default function App() {
  return (
    <SafeAreaProvider>
      <StatusBar barStyle="light-content" backgroundColor={THEME.colors.background} />
      <NavigationContainer theme={customDarkTheme}>
        <Tab.Navigator
          initialRouteName="Tracker"
          screenOptions={{
            headerShown: false,
            tabBarStyle: {
              backgroundColor: THEME.colors.surface,
              borderTopColor: THEME.colors.borderDefault,
              borderTopWidth: 1.5,
              height: Platform.OS === 'ios' ? 88 : 68,
              paddingBottom: Platform.OS === 'ios' ? 28 : 10,
              paddingTop: 8,
              elevation: 10,
              shadowColor: '#000',
              shadowOffset: { width: 0, height: -4 },
              shadowOpacity: 0.35,
              shadowRadius: 8
            },
            tabBarActiveTintColor: THEME.colors.neonCyan,
            tabBarInactiveTintColor: THEME.colors.textMuted,
            tabBarLabelStyle: {
              fontSize: 12,
              fontWeight: '700',
              marginTop: 2
            }
          }}
        >
          <Tab.Screen
            name="Tracker"
            component={TrackerScreen}
            options={{
              tabBarLabel: 'Walk Tracker',
              tabBarIcon: ({ focused }) => (
                <View style={[styles.tabIconBox, focused && styles.tabIconActive]}>
                  <Text style={styles.tabIconEmoji}>🚶</Text>
                </View>
              )
            }}
          />
          <Tab.Screen
            name="Gallery"
            component={GalleryScreen}
            options={{
              tabBarLabel: 'Art Gallery',
              tabBarIcon: ({ focused }) => (
                <View style={[styles.tabIconBox, focused && styles.tabIconActive]}>
                  <Text style={styles.tabIconEmoji}>🎨</Text>
                </View>
              )
            }}
          />
          <Tab.Screen
            name="Leaderboard"
            component={LeaderboardScreen}
            options={{
              tabBarLabel: 'Leaderboard',
              tabBarIcon: ({ focused }) => (
                <View style={[styles.tabIconBox, focused && styles.tabIconActive]}>
                  <Text style={styles.tabIconEmoji}>🏆</Text>
                </View>
              )
            }}
          />
        </Tab.Navigator>
      </NavigationContainer>
    </SafeAreaProvider>
  );
}

const styles = StyleSheet.create({
  tabIconBox: {
    width: 36,
    height: 32,
    borderRadius: 10,
    justifyContent: 'center',
    alignItems: 'center'
  },
  tabIconActive: {
    backgroundColor: 'rgba(56, 189, 248, 0.15)'
  },
  tabIconEmoji: {
    fontSize: 18
  }
});
