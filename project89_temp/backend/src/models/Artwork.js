const mongoose = require('mongoose');

const ArtworkSchema = new mongoose.Schema(
  {
    userId: {
      type: String,
      required: true,
      index: true,
      default: 'user_anonymous'
    },
    sessionId: {
      type: mongoose.Schema.Types.Mixed, // Supports ObjectId or String
      required: true,
      index: true
    },
    svgData: {
      type: String,
      required: true
    },
    pathData: {
      type: String,
      default: ''
    },
    rarity: {
      type: String,
      enum: ['Common', 'Uncommon', 'Rare', 'Epic', 'Legendary'],
      default: 'Common'
    },
    stats: {
      totalDistanceMeters: {
        type: Number,
        default: 0
      },
      pointCount: {
        type: Number,
        default: 0
      },
      durationSeconds: {
        type: Number,
        default: 0
      }
    },
    theme: {
      primaryColor: {
        type: String,
        default: '#10B981'
      },
      secondaryColor: {
        type: String,
        default: '#34D399'
      },
      strokeWidth: {
        type: Number,
        default: 4
      }
    },
    createdAt: {
      type: Date,
      default: Date.now
    }
  },
  {
    timestamps: true
  }
);

module.exports = mongoose.model('Artwork', ArtworkSchema);
