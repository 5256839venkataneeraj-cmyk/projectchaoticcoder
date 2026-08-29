package com.example

import com.example.data.model.WalkRouteEntity
import com.example.util.ArtworkShareHelper
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testShareCaptionFormat() {
    val route = WalkRouteEntity(
      id = 101L,
      dateString = "23 February 2024",
      isoDate = "2024-02-23",
      steps = 4210,
      distanceKm = 3.2,
      durationMinutes = 45,
      calories = 195,
      title = "Botanical Loop",
      shapeName = "Green Leaf Path",
      shapeCategory = "Botanical Garden",
      campusName = "IIT Delhi",
      artStyle = "Pastel Bloom",
      pointsJson = "[]",
      blobsJson = "[]",
      strokesJson = "[]"
    )

    val caption = ArtworkShareHelper.createShareCaption(route, "Aarav Sharma")
    assertTrue(caption.contains("Green Leaf Path"))
    assertTrue(caption.contains("IIT Delhi"))
    assertTrue(caption.contains("4210"))
    assertTrue(caption.contains("3.2 km"))
    assertTrue(caption.contains("45 min"))
    assertTrue(caption.contains("#PathCanvas"))
  }

  @Test
  fun testLocationServiceDurationFormatting() {
    assertEquals("00:00", com.example.service.LocationService.formatDuration(0))
    assertEquals("00:45", com.example.service.LocationService.formatDuration(45))
    assertEquals("05:30", com.example.service.LocationService.formatDuration(330))
    assertEquals("01:15:00", com.example.service.LocationService.formatDuration(4500))
  }

  @Test
  fun testCampusLandmarksAndRoutesExist() {
    val landmarks = com.example.ui.map.VIT_CAMPUS_DETAILED_LANDMARKS
    assertTrue(landmarks.isNotEmpty())
    assertTrue(landmarks.any { it.shortCode == "SJT" })
    assertTrue(landmarks.any { it.shortCode == "TT" })
    assertTrue(landmarks.any { it.shortCode == "PRP" })
    assertTrue(landmarks.any { it.shortCode == "LAKE" })
    assertTrue(landmarks.any { it.shortCode == "MH-F" })

    val artRoutes = com.example.ui.map.VIT_CAMPUS_PRESET_ART_ROUTES
    assertTrue(artRoutes.isNotEmpty())
    assertTrue(artRoutes.any { it.shapeName == "Dolphin" })
    assertTrue(artRoutes.any { it.shapeName == "Infinity" })
  }
}

