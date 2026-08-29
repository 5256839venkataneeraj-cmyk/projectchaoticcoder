const mongoose = require('mongoose');

const CoordinateSchema = new mongoose.Schema(
  {
    latitude: {
      type: Number,
      required: true
    },
    longitude: {
      type: Number,
      required: true
    },
    timestamp: {
      type: Number, // Epoch timestamp in ms
      required: true,
      default: () => Date.now()
    },
    altitude: {
      type: Number,
      default: null
    },
    accuracy: {
      type: Number,
      default: null
    },
    speed: {
      type: Number,
      default: null
    },
    heading: {
      type: Number,
      default: null
    }
  },
  { _id: false }
);

const WalkSessionSchema = new mongoose.Schema(
  {
    userId: {
      type: String,
      required: true,
      default: 'user_anonymous'
    },
    startTime: {
      type: Date,
      required: true,
      default: Date.now
    },
    endTime: {
      type: Date,
      default: null
    },
    routeData: {
      type: [CoordinateSchema],
      required: true,
      validate: {
        validator: function (v) {
          return Array.isArray(v);
        },
        message: 'routeData must be an array of coordinate objects'
      }
    },
    pointCount: {
      type: Number,
      default: 0
    },
    totalDistanceMeters: {
      type: Number,
      default: 0
    },
    status: {
      type: String,
      enum: ['active', 'completed', 'discarded'],
      default: 'completed'
    },
    notes: {
      type: String,
      default: ''
    }
  },
  {
    timestamps: true
  }
);

// Pre-save hook to calculate pointCount and totalDistanceMeters
WalkSessionSchema.pre('save', function (next) {
  if (this.routeData && this.routeData.length > 0) {
    this.pointCount = this.routeData.length;
    
    // Calculate simple haversine distance along coordinates if multiple points exist
    let totalDist = 0;
    for (let i = 1; i < this.routeData.length; i++) {
      const prev = this.routeData[i - 1];
      const curr = this.routeData[i];
      if (prev.latitude && prev.longitude && curr.latitude && curr.longitude) {
        totalDist += calculateDistance(prev.latitude, prev.longitude, curr.latitude, curr.longitude);
      }
    }
    this.totalDistanceMeters = Math.round(totalDist * 100) / 100;
  }
  next();
});

// Haversine formula helper (in meters)
function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371e3; // Earth radius in meters
  const φ1 = (lat1 * Math.PI) / 180;
  const φ2 = (lat2 * Math.PI) / 180;
  const Δφ = ((lat2 - lat1) * Math.PI) / 180;
  const Δλ = ((lon2 - lon1) * Math.PI) / 180;

  const a =
    Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
    Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return R * c;
}

module.exports = mongoose.model('WalkSession', WalkSessionSchema);
