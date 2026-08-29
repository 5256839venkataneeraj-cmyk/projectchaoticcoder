import { Platform } from 'react-native';

// Dynamically select localhost or network IP depending on platform
// For Android emulator: 10.0.2.2; For iOS simulator / Web: localhost
export const DEFAULT_API_URL =
  Platform.OS === 'android' ? 'http://10.0.2.2:5000' : 'http://localhost:5000';

let currentApiUrl = DEFAULT_API_URL;

export const setApiUrl = (url) => {
  if (url) currentApiUrl = url;
};

export const getApiUrl = () => currentApiUrl;

/**
 * Uploads a completed walk session and mints generative artwork
 */
export async function uploadWalkSession({
  userId,
  startTime,
  endTime,
  routeData,
  notes,
  rarityOverride
}) {
  const url = `${getApiUrl()}/api/v1/sessions/upload`;
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      userId: userId || 'student_creator',
      startTime: startTime || new Date().toISOString(),
      endTime: endTime || new Date().toISOString(),
      routeData,
      notes: notes || 'Campus GPS generative art journey',
      rarityOverride
    })
  });

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody.error || `Failed to upload session (${response.status})`);
  }

  return await response.json();
}

/**
 * Fetches all generated artworks for a given user sorted newest first
 */
export async function fetchUserGallery(userId, rarity = null) {
  let url = `${getApiUrl()}/api/v1/artworks/gallery/${encodeURIComponent(userId)}`;
  if (rarity && rarity !== 'All') {
    url += `?rarity=${encodeURIComponent(rarity)}`;
  }

  const response = await fetch(url);
  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody.error || `Failed to fetch gallery (${response.status})`);
  }

  return await response.json();
}

/**
 * Fetches a single artwork by ID
 */
export async function fetchArtworkById(artworkId) {
  const url = `${getApiUrl()}/api/v1/artworks/${encodeURIComponent(artworkId)}`;
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Failed to fetch artwork ${artworkId}`);
  }
  return await response.json();
}

/**
 * Generates an SVG preview directly from route coordinates without persisting
 */
export async function generateArtworkPreview({ routeData, rarityOverride, userId }) {
  const url = `${getApiUrl()}/api/v1/artworks/generate`;
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      routeData,
      rarityOverride,
      userId
    })
  });

  if (!response.ok) {
    throw new Error('Failed to generate preview');
  }

  return await response.json();
}

/**
 * Fetches the campus leaderboard sorted by totalDistance descending
 */
export async function fetchLeaderboard() {
  const url = `${getApiUrl()}/api/v1/users/leaderboard`;
  const response = await fetch(url);
  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody.error || `Failed to fetch leaderboard (${response.status})`);
  }
  return await response.json();
}

/**
 * Fetches a user's profile with gamification stats
 */
export async function fetchUserProfile(userId) {
  const url = `${getApiUrl()}/api/v1/users/profile/${encodeURIComponent(userId)}`;
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Failed to fetch profile for user ${userId}`);
  }
  return await response.json();
}
