package com.example.ui.profile

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToArtworks: () -> Unit,
    onNavigateToColourGallery: () -> Unit,
    onNavigateToQuests: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showAccountDetailsDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isLoggedOutState by remember { mutableStateOf(false) }

    // Account Details Edit State
    var editName by remember(uiState.userProfile.username) { mutableStateOf(uiState.userProfile.username.ifEmpty { "James" }) }
    var editStudentId by remember(uiState.userProfile.studentId) { mutableStateOf(uiState.userProfile.studentId) }
    var editHostel by remember(uiState.userProfile.hostelBlock) { mutableStateOf(uiState.userProfile.hostelBlock) }
    var editDepartment by remember(uiState.userProfile.department) { mutableStateOf(uiState.userProfile.department) }

    // Account Details Dialog
    if (showAccountDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDetailsDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentMintLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = AccentTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Account Details",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkSlatePrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profile Level Badge
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SoftLavenderRecent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Explorer Rank",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Text(
                                    text = uiState.userProfile.explorerRank,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = AccentLavender
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AccentLavender
                            ) {
                                Text(
                                    text = "Lv. ${uiState.userProfile.currentLevel}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Editable Name
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Editable Student ID
                    OutlinedTextField(
                        value = editStudentId,
                        onValueChange = { editStudentId = it },
                        label = { Text("Student ID") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Editable Hostel
                    OutlinedTextField(
                        value = editHostel,
                        onValueChange = { editHostel = it },
                        label = { Text("Hostel / Residence") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Editable Department
                    OutlinedTextField(
                        value = editDepartment,
                        onValueChange = { editDepartment = it },
                        label = { Text("Department") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Stats Quick Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftMintRecent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Artworks", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(
                                    "${uiState.totalArtworksCount}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DarkSlatePrimary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftMintRecent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Total Steps", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(
                                    "${uiState.userProfile.totalSteps}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DarkSlatePrimary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(editName, editStudentId, editHostel, editDepartment)
                        showAccountDetailsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountDetailsDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text("Log Out", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DarkSlatePrimary)
            },
            text = {
                Text(
                    "Are you sure you want to log out from PathCanvas? Your on-device walking artworks remain saved safely.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkSlateSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logout()
                        isLoggedOutState = true
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LogoutRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Log Out", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    Scaffold(
        containerColor = MintBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            // Centered Share Button at bottom as visible in reference design
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Check out my walking route artworks on PathCanvas! Created by ${uiState.userProfile.username.ifEmpty { "James" }} with ${uiState.totalArtworksCount} generative artworks."
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Profile"))
                        }
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .testTag("profile_bottom_share_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = DarkSlatePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Share",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = DarkSlatePrimary
                    )
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // 1. Top Bar: "Your Profile" + Wavy Brush Stroke Underline (Left) & Circular Avatar Button (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: "Your Profile" Title with stylized green underline
                Column {
                    Text(
                        text = "Your Profile",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        ),
                        color = DarkSlatePrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    // Green brush stroke / wave underline
                    Canvas(modifier = Modifier.size(width = 46.dp, height = 5.dp)) {
                        val path = Path().apply {
                            moveTo(1f, size.height * 0.7f)
                            cubicTo(
                                size.width * 0.35f, 0f,
                                size.width * 0.7f, size.height,
                                size.width - 1f, size.height * 0.3f
                            )
                        }
                        drawPath(
                            path = path,
                            color = HeroTealTag,
                            style = Stroke(
                                width = 3.5f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // Right: Circular White Avatar Button
                Surface(
                    shape = CircleShape,
                    color = SurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable { showAccountDetailsDialog = true }
                        .testTag("profile_avatar_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "User Avatar",
                            tint = DarkSlatePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // 2. Greeting Header: "Hello James" & "Live. Move. Create."
            val displayName = uiState.userProfile.username.ifEmpty { "James" }
            Text(
                text = "Hello $displayName",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = DarkSlatePrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Live. Move. Create.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    color = TextMuted
                )
            )

            Spacer(modifier = Modifier.height(22.dp))

            // 3. Hero Gradient Button: "Account details" (Mint -> Lavender gradient)
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { showAccountDetailsDialog = true }
                    .testTag("account_details_button")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    AccountGradientStart,
                                    AccountGradientEnd
                                )
                            )
                        )
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Circular translucent frosted badge icon
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = DarkSlatePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Text: "Account details"
                        Text(
                            text = "Account details",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = DarkSlatePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 4. "Recent" Section Title
            Text(
                text = "Recent",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = DarkSlatePrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Recent Item 1: "Your artworks" (Soft Mint Pill Card)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SoftMintRecent,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onNavigateToArtworks)
                    .testTag("recent_your_artworks_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = HeroTealTag,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Your artworks",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = DarkSlatePrimary
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Go",
                        tint = DarkSlateSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Recent Item 2: "Colour gallery" (Soft Lavender Pill Card)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SoftLavenderRecent,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onNavigateToColourGallery)
                    .testTag("recent_colour_gallery_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = AccentLavender,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Colour gallery",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = DarkSlatePrimary
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Go",
                        tint = DarkSlateSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 7. Subtle Divider
            HorizontalDivider(
                color = BorderSubtle,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(22.dp))

            // 8. Logout Pill Card: "Log out" (Soft Light Red Pill Card)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SoftLogoutPill,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showLogoutDialog = true }
                    .testTag("profile_logout_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Log out",
                        tint = LogoutRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Log out",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = LogoutRed
                    )
                }
            }
        }
    }
}
