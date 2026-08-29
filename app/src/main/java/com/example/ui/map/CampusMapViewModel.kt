package com.example.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.PointF
import com.example.data.model.WalkRouteEntity
import com.example.data.repository.RouteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.sqrt

enum class CampusCategory(val displayName: String, val emoji: String) {
    ALL("All Zones", "🗺️"),
    ACADEMIC("Academic & Admin", "🏫"),
    HOSTELS("Men's Hostels", "🏢"),
    SPORTS("Sports & Lake", "🏃"),
    TRANSIT("Gates & Parking", "🚌"),
    AMENITIES("Food & ATMs", "☕")
}

enum class MapLayerMode(val label: String, val icon: String) {
    CAMPUS_MAP("Standard Map", "🗺️"),
    ROUTE_ART("Route Art Paths", "🎨"),
    SHUTTLE_TRANSIT("Shuttle Transit", "🚌")
}

data class DetailedCampusLandmark(
    val id: String,
    val name: String,
    val shortCode: String,
    val category: CampusCategory,
    val emoji: String,
    val x: Float, // normalized 0..1000
    val y: Float, // normalized 0..1000
    val description: String,
    val popularArtRoute: String = "Campus Loop",
    val estimatedStepsFromGate1: Int = 1200,
    val colorHex: Long = 0xFF56B386
)

data class CampusArtRoute(
    val id: String,
    val name: String,
    val shapeName: String,
    val emoji: String,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val points: List<PointF>,
    val description: String,
    val themeColorHex: Long = 0xFF56B386
)

val VIT_CAMPUS_DETAILED_LANDMARKS = listOf(
    // Academic & Administration Blocks (Terracotta / Orange)
    DetailedCampusLandmark(
        id = "tt",
        name = "Technology Tower (TT)",
        shortCode = "TT",
        category = CampusCategory.ACADEMIC,
        emoji = "🗼",
        x = 240f,
        y = 710f,
        description = "Core Computer Science, IT & Electronics engineering laboratories and amphitheaters.",
        popularArtRoute = "Technology Tower Spiral",
        estimatedStepsFromGate1 = 450,
        colorHex = 0xFFF4A261
    ),
    DetailedCampusLandmark(
        id = "sjt",
        name = "Silver Jubilee Tower (SJT)",
        shortCode = "SJT",
        category = CampusCategory.ACADEMIC,
        emoji = "🏛️",
        x = 710f,
        y = 570f,
        description = "Iconic 8-storey multi-department academic tower with smart lecture complexes.",
        popularArtRoute = "SJT Infinity Loop",
        estimatedStepsFromGate1 = 2100,
        colorHex = 0xFFF4A261
    ),
    DetailedCampusLandmark(
        id = "prp",
        name = "Pearl Research Park (PRP)",
        shortCode = "PRP",
        category = CampusCategory.ACADEMIC,
        emoji = "🔬",
        x = 860f,
        y = 520f,
        description = "Advanced multidisciplinary research hub, innovation labs, and faculty offices.",
        popularArtRoute = "PRP Diamond Circuit",
        estimatedStepsFromGate1 = 2800,
        colorHex = 0xFFE76F51
    ),
    DetailedCampusLandmark(
        id = "mach",
        name = "Mach Building",
        shortCode = "MACH",
        category = CampusCategory.ACADEMIC,
        emoji = "🏗️",
        x = 955f,
        y = 455f,
        description = "Mechanical and Aerospace engineering laboratories and heavy testing facility.",
        popularArtRoute = "East Boundary Sprint",
        estimatedStepsFromGate1 = 3400,
        colorHex = 0xFFF4A261
    ),
    DetailedCampusLandmark(
        id = "mb_admin",
        name = "Academic & Admin Central (MB)",
        shortCode = "MB",
        category = CampusCategory.ACADEMIC,
        emoji = "🏫",
        x = 160f,
        y = 650f,
        description = "Main Administrative Block, Admissions Office, Chancellor's Secretariat & Registrar.",
        popularArtRoute = "Central Heritage Walk",
        estimatedStepsFromGate1 = 300,
        colorHex = 0xFFF4A261
    ),
    DetailedCampusLandmark(
        id = "library",
        name = "Central Library & Digital Archive",
        shortCode = "LIB",
        category = CampusCategory.ACADEMIC,
        emoji = "📚",
        x = 420f,
        y = 350f,
        description = "Air-conditioned 4-tier study center, digital research repositories and quiet study halls.",
        popularArtRoute = "Scholar's Pathway",
        estimatedStepsFromGate1 = 1500,
        colorHex = 0xFFF4A261
    ),

    // Men's Hostels (MH Blocks - Teal / Green)
    DetailedCampusLandmark(
        id = "mh_b",
        name = "MH B Block & Annexe",
        shortCode = "MH-B",
        category = CampusCategory.HOSTELS,
        emoji = "🏢",
        x = 315f,
        y = 200f,
        description = "Men's residential block with inner quadrangles, study rooms, and dining hall.",
        popularArtRoute = "North Quad Stride",
        estimatedStepsFromGate1 = 1800,
        colorHex = 0xFF56B386
    ),
    DetailedCampusLandmark(
        id = "mh_f",
        name = "MH F Block",
        shortCode = "MH-F",
        category = CampusCategory.HOSTELS,
        emoji = "🏢",
        x = 475f,
        y = 360f,
        description = "Large multi-wing residential block surrounded by greenery and food stalls.",
        popularArtRoute = "F-Block Orbit",
        estimatedStepsFromGate1 = 2000,
        colorHex = 0xFF56B386
    ),
    DetailedCampusLandmark(
        id = "mh_n",
        name = "MH N Block & Nm Block",
        shortCode = "MH-N",
        category = CampusCategory.HOSTELS,
        emoji = "🏢",
        x = 740f,
        y = 195f,
        description = "High-rise modern residential tower overlooking the sports ground and hills.",
        popularArtRoute = "Highrise Sky Walk",
        estimatedStepsFromGate1 = 2600,
        colorHex = 0xFF56B386
    ),
    DetailedCampusLandmark(
        id = "mh_q",
        name = "MH Q Block",
        shortCode = "MH-Q",
        category = CampusCategory.HOSTELS,
        emoji = "🏢",
        x = 680f,
        y = 300f,
        description = "Men's hostel block with integrated indoor sports and gym room.",
        popularArtRoute = "Quad Connector Walk",
        estimatedStepsFromGate1 = 2300,
        colorHex = 0xFF56B386
    ),
    DetailedCampusLandmark(
        id = "mh_r",
        name = "MH R Block",
        shortCode = "MH-R",
        category = CampusCategory.HOSTELS,
        emoji = "🏢",
        x = 660f,
        y = 365f,
        description = "Modern residential facility close to the central cafeteria and lake path.",
        popularArtRoute = "R-Block Stroll",
        estimatedStepsFromGate1 = 2200,
        colorHex = 0xFF56B386
    ),
    DetailedCampusLandmark(
        id = "nhj_block",
        name = "NHJ Block",
        shortCode = "NHJ",
        category = CampusCategory.HOSTELS,
        emoji = "🏢",
        x = 830f,
        y = 280f,
        description = "Student hostel accommodation near eastern fields and parking zones.",
        popularArtRoute = "Eastern Hostel Perimeter",
        estimatedStepsFromGate1 = 2900,
        colorHex = 0xFF56B386
    ),
    DetailedCampusLandmark(
        id = "mh_office",
        name = "MH Office (Men's Hostel Office)",
        shortCode = "MH-OFF",
        category = CampusCategory.HOSTELS,
        emoji = "📋",
        x = 450f,
        y = 460f,
        description = "Chief Warden Office, room allocation desk, and hostel student grievance center.",
        popularArtRoute = "Central Admin Link",
        estimatedStepsFromGate1 = 1600,
        colorHex = 0xFFE76F51
    ),

    // Sports & Recreation (Blue & Green)
    DetailedCampusLandmark(
        id = "vit_lake",
        name = "VIT Lake",
        shortCode = "LAKE",
        category = CampusCategory.SPORTS,
        emoji = "🌊",
        x = 520f,
        y = 695f,
        description = "Scenic campus freshwater lake with scenic perimeter running track and gazebo seating.",
        popularArtRoute = "VIT Lake Dolphin Loop",
        estimatedStepsFromGate1 = 1100,
        colorHex = 0xFF4A90E2
    ),
    DetailedCampusLandmark(
        id = "sjt_ground",
        name = "SJT Ground (Induction Venue)",
        shortCode = "SJT-GRD",
        category = CampusCategory.SPORTS,
        emoji = "🎪",
        x = 680f,
        y = 700f,
        description = "Spacious grass arena hosting annual university convocations, Rivera fests, and sports.",
        popularArtRoute = "Induction Festival Ring",
        estimatedStepsFromGate1 = 2200,
        colorHex = 0xFF56B386
    ),
    DetailedCampusLandmark(
        id = "open_stadium",
        name = "Open Stadium & Track",
        shortCode = "STAD",
        category = CampusCategory.SPORTS,
        emoji = "🏟️",
        x = 510f,
        y = 145f,
        description = "400m Olympic standard synthetic athletic track, football ground, and viewing stands.",
        popularArtRoute = "Olympic 400m Dash Route",
        estimatedStepsFromGate1 = 2300,
        colorHex = 0xFFE76F51
    ),
    DetailedCampusLandmark(
        id = "swimming_pool",
        name = "Olympic Swimming Pool",
        shortCode = "POOL",
        category = CampusCategory.SPORTS,
        emoji = "🏊",
        x = 505f,
        y = 265f,
        description = "50-meter 8-lane competition swimming pool and diving platform.",
        popularArtRoute = "Aquatic Stride",
        estimatedStepsFromGate1 = 2100,
        colorHex = 0xFF4A90E2
    ),
    DetailedCampusLandmark(
        id = "vit_fields",
        name = "VIT Fields & Sports Arena",
        shortCode = "FIELDS",
        category = CampusCategory.SPORTS,
        emoji = "⚽",
        x = 890f,
        y = 350f,
        description = "Cricket ground, basketball, volleyball courts and tennis practice academy.",
        popularArtRoute = "Champions Run",
        estimatedStepsFromGate1 = 3100,
        colorHex = 0xFF56B386
    ),

    // Transit, Gates & Parking
    DetailedCampusLandmark(
        id = "gate_1",
        name = "Main Entrance (Gate 1 - West)",
        shortCode = "GATE-1",
        category = CampusCategory.TRANSIT,
        emoji = "🚪",
        x = 110f,
        y = 605f,
        description = "Primary university gate on Katpadi Main Road with security checkpoint and visitor lounge.",
        popularArtRoute = "Welcome Gate Straight",
        estimatedStepsFromGate1 = 0,
        colorHex = 0xFF2A9D8F
    ),
    DetailedCampusLandmark(
        id = "gate_2a",
        name = "Gate 2A (Katpadi Road)",
        shortCode = "GATE-2A",
        category = CampusCategory.TRANSIT,
        emoji = "🚪",
        x = 400f,
        y = 885f,
        description = "Pedestrian subway access and bus boarding point for Vellore city shuttles.",
        popularArtRoute = "South Highway Loop",
        estimatedStepsFromGate1 = 900,
        colorHex = 0xFF2A9D8F
    ),
    DetailedCampusLandmark(
        id = "gate_11",
        name = "Gate 11 (East Campus)",
        shortCode = "GATE-11",
        category = CampusCategory.TRANSIT,
        emoji = "🚪",
        x = 705f,
        y = 930f,
        description = "Southern access gate near railway line and outdoor induction parking.",
        popularArtRoute = "East Gate Promenade",
        estimatedStepsFromGate1 = 2400,
        colorHex = 0xFF2A9D8F
    ),
    DetailedCampusLandmark(
        id = "shuttle_stop_30",
        name = "Shuttle Stop 30 (Central)",
        shortCode = "BUS-30",
        category = CampusCategory.TRANSIT,
        emoji = "🚌",
        x = 505f,
        y = 530f,
        description = "Electric buggy shuttle terminal connecting MH Hostels to Academic Blocks every 5 mins.",
        popularArtRoute = "Shuttle Express Track",
        estimatedStepsFromGate1 = 1400,
        colorHex = 0xFFE76F51
    ),
    DetailedCampusLandmark(
        id = "bike_parking",
        name = "Multi-level Bike & Car Parking",
        shortCode = "PARK",
        category = CampusCategory.TRANSIT,
        emoji = "🅿️",
        x = 785f,
        y = 720f,
        description = "Student bicycle stands, EV charging spots, and covered faculty car parking.",
        popularArtRoute = "Parking Grid Sprint",
        estimatedStepsFromGate1 = 2500,
        colorHex = 0xFF6B7280
    )
)

val VIT_CAMPUS_PRESET_ART_ROUTES = listOf(
    CampusArtRoute(
        id = "route_lake_dolphin",
        name = "VIT Lake Dolphin Loop",
        shapeName = "Dolphin",
        emoji = "🐬",
        distanceKm = 2.4,
        estimatedMinutes = 28,
        themeColorHex = 0xFF4A90E2,
        description = "Walk along the curved perimeter path of VIT Lake to create a fluid aquatic dolphin artwork.",
        points = listOf(
            PointF(420f, 660f),
            PointF(470f, 640f),
            PointF(530f, 650f),
            PointF(590f, 670f),
            PointF(630f, 710f),
            PointF(600f, 760f),
            PointF(540f, 775f),
            PointF(480f, 760f),
            PointF(440f, 730f),
            PointF(410f, 680f),
            PointF(420f, 660f)
        )
    ),
    CampusArtRoute(
        id = "route_tt_sjt_infinity",
        name = "TT to SJT Infinity Path",
        shapeName = "Infinity",
        emoji = "♾️",
        distanceKm = 3.6,
        estimatedMinutes = 42,
        themeColorHex = 0xFF9C27B0,
        description = "Trace a figure-eight crossing between Technology Tower, Library, and Silver Jubilee Tower.",
        points = listOf(
            PointF(240f, 710f),
            PointF(320f, 600f),
            PointF(450f, 530f),
            PointF(600f, 500f),
            PointF(710f, 570f),
            PointF(680f, 680f),
            PointF(550f, 630f),
            PointF(450f, 530f),
            PointF(300f, 650f),
            PointF(240f, 710f)
        )
    ),
    CampusArtRoute(
        id = "route_hostel_crown",
        name = "Hostel Quad Geometric Crown",
        shapeName = "Crown",
        emoji = "👑",
        distanceKm = 3.1,
        estimatedMinutes = 35,
        themeColorHex = 0xFF56B386,
        description = "Weave through MH-B, MH-F, and MH-N quads creating sharp majestic crown peaks.",
        points = listOf(
            PointF(315f, 200f),
            PointF(400f, 290f),
            PointF(475f, 200f),
            PointF(600f, 290f),
            PointF(740f, 195f),
            PointF(700f, 380f),
            PointF(500f, 380f),
            PointF(315f, 380f),
            PointF(315f, 200f)
        )
    ),
    CampusArtRoute(
        id = "route_stadium_star",
        name = "Stadium Sprint Star Art",
        shapeName = "Star",
        emoji = "⭐",
        distanceKm = 2.8,
        estimatedMinutes = 32,
        themeColorHex = 0xFFF4A261,
        description = "Sprint across the Open Stadium and Helipad avenues to draw a five-pointed star.",
        points = listOf(
            PointF(510f, 80f),
            PointF(550f, 160f),
            PointF(630f, 160f),
            PointF(570f, 210f),
            PointF(590f, 290f),
            PointF(510f, 240f),
            PointF(430f, 290f),
            PointF(450f, 210f),
            PointF(390f, 160f),
            PointF(470f, 160f),
            PointF(510f, 80f)
        )
    )
)

data class CampusMapUiState(
    val selectedCategory: CampusCategory = CampusCategory.ALL,
    val searchQuery: String = "",
    val layerMode: MapLayerMode = MapLayerMode.CAMPUS_MAP,
    val selectedLandmark: DetailedCampusLandmark? = null,
    val selectedArtRoute: CampusArtRoute? = null,
    val userPosition: PointF = PointF(240f, 710f), // At Technology Tower by default
    val totalSavedArtworks: Int = 0,
    val campusDistanceKm: Double = 0.0
)

class CampusMapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RouteRepository(application)

    private val _uiState = MutableStateFlow(CampusMapUiState())
    val uiState: StateFlow<CampusMapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allRoutes.collect { routes ->
                _uiState.update {
                    it.copy(
                        totalSavedArtworks = routes.size,
                        campusDistanceKm = routes.sumOf { r -> r.distanceKm }
                    )
                }
            }
        }
    }

    fun selectCategory(category: CampusCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setLayerMode(mode: MapLayerMode) {
        _uiState.update { it.copy(layerMode = mode) }
    }

    fun selectLandmark(landmark: DetailedCampusLandmark?) {
        _uiState.update { it.copy(selectedLandmark = landmark, selectedArtRoute = null) }
    }

    fun selectArtRoute(route: CampusArtRoute?) {
        _uiState.update { it.copy(selectedArtRoute = route, selectedLandmark = null) }
    }

    fun getFilteredLandmarks(): List<DetailedCampusLandmark> {
        val state = _uiState.value
        val query = state.searchQuery.trim().lowercase()

        return VIT_CAMPUS_DETAILED_LANDMARKS.filter { lm ->
            val matchesCat = state.selectedCategory == CampusCategory.ALL || lm.category == state.selectedCategory
            val matchesQuery = query.isEmpty() ||
                    lm.name.lowercase().contains(query) ||
                    lm.shortCode.lowercase().contains(query) ||
                    lm.description.lowercase().contains(query)
            matchesCat && matchesQuery
        }
    }

    fun calculateStepsFromUser(landmark: DetailedCampusLandmark): Int {
        val user = _uiState.value.userPosition
        val dx = landmark.x - user.x
        val dy = landmark.y - user.y
        val distUnits = sqrt(dx * dx + dy * dy)
        // Scaled to real campus distance (approx 1 unit ~ 2.5 meters)
        val meters = (distUnits * 2.5).toInt()
        return (meters / 0.72).toInt().coerceAtLeast(150)
    }

    fun calculateDistanceKmFromUser(landmark: DetailedCampusLandmark): Double {
        val user = _uiState.value.userPosition
        val dx = landmark.x - user.x
        val dy = landmark.y - user.y
        val distUnits = sqrt(dx * dx + dy * dy)
        val meters = distUnits * 2.5
        val km = meters / 1000.0
        return Math.round(km * 100.0) / 100.0
    }
}
