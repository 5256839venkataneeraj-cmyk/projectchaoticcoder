package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WalkRouteEntity
import com.example.ui.theme.*

@Composable
fun StoryCardView(
    route: WalkRouteEntity,
    studentName: String = "Aarav Sharma",
    brushStyleKey: String = "INK",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(9f / 15.5f)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAFBF9),
                        Color(0xFFEEF6F1),
                        Color(0xFFE5F1EB)
                    )
                )
            )
            .border(2.dp, BorderSubtle, RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Brand & Campus
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AccentMint),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎨", fontSize = 16.sp)
                    }
                    Column {
                        Text(
                            text = "PathCanvas",
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkSlatePrimary
                        )
                        Text(
                            text = "Every walk. Every art.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                // Campus Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.8f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = AccentMint,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = route.campusName,
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkSlatePrimary
                        )
                    }
                }
            }

            // Central Generative Art Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.7f))
                    .border(1.dp, Color.White, RoundedCornerShape(20.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                ArtCanvasView(
                    pointsJson = route.pointsJson,
                    blobsJson = route.blobsJson,
                    artStyle = route.artStyle,
                    stickersJson = route.stickersJson,
                    brushStyleKey = brushStyleKey,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bottom Info: Title, Shape, Stats & Signature
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = route.shapeName,
                            style = MaterialTheme.typography.titleLarge,
                            color = DarkSlatePrimary
                        )
                        Text(
                            text = route.dateString,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AccentLavenderContainer
                    ) {
                        Text(
                            text = route.shapeCategory,
                            style = MaterialTheme.typography.labelMedium,
                            color = DarkSlatePrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Stats Row: Steps, Distance, Duration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.9f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "👟", fontSize = 13.sp)
                            Column {
                                Text(
                                    text = "${route.steps}",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = DarkSlatePrimary
                                )
                                Text(
                                    text = "steps",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.9f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "📍", fontSize = 13.sp)
                            Column {
                                Text(
                                    text = "${route.distanceKm} km",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = DarkSlatePrimary
                                )
                                Text(
                                    text = "distance",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.9f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "⏱️", fontSize = 13.sp)
                            Column {
                                Text(
                                    text = "${route.durationMinutes}m",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = DarkSlatePrimary
                                )
                                Text(
                                    text = "duration",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                // Student Signature Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Art by $studentName",
                        style = MaterialTheme.typography.labelMedium,
                        color = DarkSlateSecondary
                    )
                    Text(
                        text = "#PathCanvas #CampusArt",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentLavender
                    )
                }
            }
        }
    }
}
