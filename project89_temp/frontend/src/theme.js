/**
 * Campus Route-to-Art Centralized Design System & Theme Tokens
 * Dark Digital Art Aesthetic with Neon Cyan, Purple, Emerald & Gold Accents.
 */

export const THEME = {
  colors: {
    // Core Backgrounds
    background: '#090D16',
    surface: '#0F172A',
    surfaceSubtle: '#141E33',
    surfaceElevated: '#1E293B',
    modalBackground: 'rgba(9, 13, 22, 0.94)',

    // Neon Accents
    neonCyan: '#38BDF8',
    neonPurple: '#8B5CF6',
    neonEmerald: '#10B981',
    neonRose: '#EC4899',
    neonGold: '#FCD34D',
    neonAmber: '#F59E0B',

    // Typography
    textPrimary: '#F8FAFC',
    textSecondary: '#94A3B8',
    textMuted: '#64748B',
    textDisabled: '#475569',

    // Borders & Dividers
    borderDefault: '#1E293B',
    borderSubtle: '#334155',
    borderGlow: 'rgba(56, 189, 248, 0.35)',

    // Status / Feedback
    success: '#10B981',
    warning: '#F59E0B',
    error: '#EF4444',
    info: '#38BDF8'
  },

  rarity: {
    Common: {
      name: 'Common',
      color: '#10B981',
      bgColor: 'rgba(16, 185, 129, 0.15)',
      borderColor: 'rgba(16, 185, 129, 0.4)',
      label: 'Common Artefact'
    },
    Uncommon: {
      name: 'Uncommon',
      color: '#06B6D4',
      bgColor: 'rgba(6, 182, 212, 0.15)',
      borderColor: 'rgba(6, 182, 212, 0.45)',
      label: 'Uncommon Artefact'
    },
    Rare: {
      name: 'Rare',
      color: '#8B5CF6',
      bgColor: 'rgba(139, 92, 246, 0.15)',
      borderColor: 'rgba(139, 92, 246, 0.45)',
      label: 'Rare Masterpiece'
    },
    Epic: {
      name: 'Epic',
      color: '#F59E0B',
      bgColor: 'rgba(245, 158, 11, 0.15)',
      borderColor: 'rgba(245, 158, 11, 0.45)',
      label: 'Epic Masterpiece'
    },
    Legendary: {
      name: 'Legendary',
      color: '#F43F5E',
      bgColor: 'rgba(244, 63, 94, 0.2)',
      borderColor: '#FCD34D',
      label: '👑 Legendary Relic'
    }
  },

  spacing: {
    xs: 4,
    sm: 8,
    md: 14,
    lg: 20,
    xl: 28
  },

  radii: {
    sm: 8,
    md: 14,
    lg: 20,
    xl: 28,
    full: 9999
  },

  typography: {
    headerTitle: {
      fontSize: 28,
      fontWeight: '800',
      color: '#F8FAFC',
      letterSpacing: -0.5
    },
    subtitle: {
      fontSize: 11,
      fontWeight: '700',
      color: '#38BDF8',
      letterSpacing: 1.5,
      textTransform: 'uppercase'
    },
    sectionHeading: {
      fontSize: 11,
      fontWeight: '700',
      color: '#64748B',
      letterSpacing: 1,
      textTransform: 'uppercase'
    }
  }
};
