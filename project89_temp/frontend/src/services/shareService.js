import { Share, Platform, Alert } from 'react-native';
import * as Sharing from 'expo-sharing';
import * as FileSystem from 'expo-file-system';

/**
 * Shares generative artwork via Expo Sharing dialog or React Native Share fallback
 */
export async function shareArtwork(artwork) {
  if (!artwork) return;

  const rarity = artwork.rarity || 'Common';
  const distance = artwork.stats?.totalDistanceMeters || 0;
  const formattedDistance =
    distance >= 1000 ? `${(distance / 1000).toFixed(2)} km` : `${Math.round(distance)} m`;

  const shareTitle = `Campus Route-to-Art • ${rarity} Artefact`;
  const shareMessage = `🎨 Check out my generative campus route art! [${rarity} • ${formattedDistance} walked on campus]`;

  try {
    const isSharingAvailable = await Sharing.isAvailableAsync().catch(() => false);

    // If FileSystem and Sharing are available, write SVG to temporary file and share
    if (isSharingAvailable && FileSystem.cacheDirectory && artwork.svgData) {
      const filename = `campus_art_${artwork._id || artwork.id || Date.now()}.svg`;
      const fileUri = `${FileSystem.cacheDirectory}${filename}`;

      await FileSystem.writeAsStringAsync(fileUri, artwork.svgData, {
        encoding: FileSystem.EncodingType.UTF8
      });

      await Sharing.shareAsync(fileUri, {
        mimeType: 'image/svg+xml',
        dialogTitle: shareTitle,
        UTI: 'public.svg-image'
      });
      return;
    }

    // Native Share fallback
    await Share.share({
      title: shareTitle,
      message: shareMessage
    });
  } catch (error) {
    console.warn('Share error:', error);
    try {
      await Share.share({
        title: shareTitle,
        message: shareMessage
      });
    } catch (fallbackError) {
      Alert.alert('Share Failed', 'Unable to open share sheet on this device.');
    }
  }
}
