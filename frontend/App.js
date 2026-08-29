import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { StatusBar } from 'expo-status-bar';
import { Text, View, StyleSheet } from 'react-native';

import HomeTracker from './src/screens/HomeTracker';
import Gallery from './src/screens/Gallery';
import Leaderboard from './src/screens/Leaderboard';

const Tab = createBottomTabNavigator();

export default function App() {
  return (
    <NavigationContainer>
      <StatusBar style="light" />
      <Tab.Navigator
        screenOptions={({ route }) => ({
          headerShown: false,
          tabBarStyle: {
            backgroundColor: '#0D1117',
            borderTopColor: '#21262D',
            borderTopWidth: 1,
            height: 64,
            paddingBottom: 8,
            paddingTop: 8
          },
          tabBarActiveTintColor: '#00F5D4',
          tabBarInactiveTintColor: '#8B949E',
          tabBarLabelStyle: {
            fontSize: 12,
            fontWeight: '700'
          },
          tabBarIcon: ({ focused, color }) => {
            let icon = '📍';
            if (route.name === 'Tracker') icon = '🧭';
            if (route.name === 'Gallery') icon = '🎨';
            if (route.name === 'Leaderboard') icon = '🏆';

            return (
              <View style={[styles.iconContainer, focused && styles.iconActive]}>
                <Text style={{ fontSize: 18 }}>{icon}</Text>
              </View>
            );
          }
        })}
      >
        <Tab.Screen name="Tracker" component={HomeTracker} options={{ title: 'Walk Tracker' }} />
        <Tab.Screen name="Gallery" component={Gallery} options={{ title: 'Art Gallery' }} />
        <Tab.Screen name="Leaderboard" component={Leaderboard} options={{ title: 'Leaderboard' }} />
      </Tab.Navigator>
    </NavigationContainer>
  );
}

const styles = StyleSheet.create({
  iconContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    padding: 4,
    borderRadius: 12
  },
  iconActive: {
    backgroundColor: '#161B22'
  }
});
