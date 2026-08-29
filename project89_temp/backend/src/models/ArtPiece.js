const mongoose = require('mongoose');

const ArtPieceSchema = new mongoose.Schema(
  {
    title: {
      type: String,
      required: true,
      trim: true
    },
    artist: {
      type: String,
      default: 'Unknown'
    },
    description: {
      type: String,
      default: ''
    },
    location: {
      latitude: {
        type: Number,
        required: true
      },
      longitude: {
        type: Number,
        required: true
      },
      campusBuilding: {
        type: String,
        default: ''
      }
    },
    imageUrl: {
      type: String,
      default: ''
    },
    year: {
      type: Number
    },
    tags: [String]
  },
  {
    timestamps: true
  }
);

module.exports = mongoose.model('ArtPiece', ArtPieceSchema);
