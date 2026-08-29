const mongoose = require('mongoose');

const UserSchema = new mongoose.Schema(
  {
    userId: {
      type: String,
      required: true,
      unique: true,
      index: true
    },
    username: {
      type: String,
      required: true,
      default: function () {
        return this.userId || 'Student Runner';
      }
    },
    totalDistance: {
      type: Number,
      default: 0
    },
    currentStreak: {
      type: Number,
      default: 0
    },
    longestStreak: {
      type: Number,
      default: 0
    },
    lastSessionDate: {
      type: Date,
      default: null
    },
    totalSessions: {
      type: Number,
      default: 0
    },
    avatarEmoji: {
      type: String,
      default: '🏃'
    },
    campusBadge: {
      type: String,
      default: 'Campus Explorer'
    }
  },
  {
    timestamps: true
  }
);

module.exports = mongoose.model('User', UserSchema);
