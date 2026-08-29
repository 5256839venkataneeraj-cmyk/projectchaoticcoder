import React from 'react';
import { View, StyleSheet, Platform } from 'react-native';
import { SvgXml } from 'react-native-svg';

/**
 * Robust cross-platform SVG renderer using react-native-svg's SvgXml.
 */
export default function ArtworkSvg({
  svgData,
  width = '100%',
  height = '100%',
  style
}) {
  if (!svgData) {
    return <View style={[styles.placeholder, style]} />;
  }

  // Ensure svgData string is clean
  let cleanXml = svgData.trim();
  // Strip XML declaration if present for certain parser compatibility
  if (cleanXml.startsWith('<?xml')) {
    cleanXml = cleanXml.replace(/<\?xml.*?\?>/i, '').trim();
  }

  return (
    <View style={[styles.container, style]}>
      <SvgXml xml={cleanXml} width={width} height={height} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    overflow: 'hidden',
    justifyContent: 'center',
    alignItems: 'center',
  },
  placeholder: {
    backgroundColor: '#1E293B',
    borderRadius: 16,
  },
});
