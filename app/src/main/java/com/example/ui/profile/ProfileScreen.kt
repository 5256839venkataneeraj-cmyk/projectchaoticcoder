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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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

// Colors specifically matching the screenshot design
private val CanvasBackground = Color(0xFFF7F5EE)
private val DarkTitleColor = Color(0xFF16211B)
private val SubtitleMuted = Color(0xFF75857C)
private val GreenUnderlineColor = Color(0xFF639D75)

private val GradientStartMint = Color(0xFFA5E6CF)
private val GradientEndLavender = Color(0xFFBCC6FA)

private val RecentCardBorder = Color(0xFF38493F)
private val ItemArtworksMint = Color(0xFFD7F5E4)
private val ItemArtworksIconDark = Color(0xFF1E4833)

private val ItemGalleryLavender = Color(0xFFE2E6FF)
private val ItemGalleryIconDark = Color(0xFF383387)

private val ItemThemesPeach = Color(0xFFFFECC7)
private val ItemThemesIconDark = Color(0xFF7A5112)

private val LogoutPillBackground = Color(0xFFFFDDE0)
private val LogoutPillTextColor = Color(0xFFDC2626)

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
    var showThemesDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Account Details Edit State
    var editName by remember(uiState.userProfile.username) { mutableStateOf(uiState.userProfile.username.ifEmpty { "Neeraj" }) }
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
                        color = DarkTitleColor
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
                        color = ItemGalleryLavender,
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
                                    color = SubtitleMuted
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
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
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
                            color = ItemArtworksMint,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Artworks", style = MaterialTheme.typography.labelSmall, color = SubtitleMuted)
                                Text(
                                    "${uiState.totalArtworksCount}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DarkTitleColor
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ItemArtworksMint,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Total Steps", style = MaterialTheme.typography.labelSmall, color = SubtitleMuted)
                                Text(
                                    "${uiState.userProfile.totalSteps}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DarkTitleColor
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
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("save_profile_button")
                ) {
                    Text("Save Changes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountDetailsDialog = false }) {
                    Text("Cancel", color = SubtitleMuted)
                }
            }
        )
    }

    // Themes Dialog
    if (showThemesDialog) {
        AlertDialog(
            onDismissRequest = { showThemesDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ItemThemesPeach),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = ItemThemesIconDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Visual Themes",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkTitleColor
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val themes = listOf(
                        Triple("Classic Mint", "Clean pastel palette inspired by campus greens", listOf(Color(0xFFD7F5E4), Color(0xFFA5E6CF))),
                        Triple("Sunset Pastel", "Warm tones inspired by twilight hostel walks", listOf(Color(0xFFFFECC7), Color(0xFFFF9E79))),
                        Triple("Lavender Breeze", "Serene violet and periwinkle gradients", listOf(Color(0xFFE2E6FF), Color(0xFFBCC6FA))),
                        Triple("Obsidian Neon", "Dark high-contrast canvas with neon glow accents", listOf(Color(0xFF16211B), Color(0xFF00F5D4)))
                    )

                    themes.forEach { (name, desc, colors) ->
                        val isSelected = uiState.selectedTheme == name
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) AccentMintLight else CreamSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) AccentTeal else BorderSubtle
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    viewModel.setTheme(name)
                                    showThemesDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(colors)
                                            )
                                    )
                                    Column {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = DarkTitleColor
                                        )
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SubtitleMuted
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = AccentTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemesDialog = false }) {
                    Text("Close", color = DarkTitleColor)
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text("Log Out", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DarkTitleColor)
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
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LogoutPillTextColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text("Log Out", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = SubtitleMuted)
                }
            }
        )
    }

    Scaffold(
        containerColor = CanvasBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 12.dp)
        ) {
            // 1. Top Bar: "Your Profile" + Soft Wavy Brush Stroke Underline (Left) & Circular Avatar Button (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: "Your Profile" Title with green organic brush stroke underline
                Column {
                    Text(
                        text = "Your Profile",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        ),
                        color = DarkTitleColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    // Subtle organic curved green underline as seen in screenshot
                    Canvas(modifier = Modifier.size(width = 64.dp, height = 6.dp)) {
                        val path = Path().apply {
                            moveTo(1f, size.height * 0.7f)
                            cubicTo(
                                size.width * 0.35f, 0f,
                                size.width * 0.75f, size.height * 0.95f,
                                size.width - 1f, size.height * 0.4f
                            )
                        }
                        drawPath(
                            path = path,
                            color = GreenUnderlineColor,
                            style = Stroke(
                                width = 3.2f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // Right: Circular Avatar Button with subtle border
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, BorderSubtle),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { showAccountDetailsDialog = true }
                        .testTag("profile_avatar_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "User Avatar",
                            tint = DarkTitleColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 2. Greeting Header: "Hi Neeraj" & "Live. Move. Create."
            val displayName = uiState.userProfile.username.ifEmpty { "Neeraj" }
            Text(
                text = "Hi $displayName",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 34.sp,
                    letterSpacing = (-0.6).sp
                ),
                color = DarkTitleColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Live. Move. Create.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    color = SubtitleMuted
                )
            )

            Spacer(modifier = Modifier.height(22.dp))

            // 3. Hero Card: "Account details" (Smooth Mint-to-Lavender Gradient Pill)
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .clickable { showAccountDetailsDialog = true }
                    .testTag("account_details_button")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    GradientStartMint,
                                    GradientEndLavender
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
                        // Semi-transparent rounded white circular icon container
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.55f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.CreditCard,
                                    contentDescription = "Account details",
                                    tint = DarkTitleColor,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        // Label: "Account details"
                        Text(
                            text = "Account details",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = DarkTitleColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 4. "Recent" Framed Container Card with outline
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(1.4.dp, RecentCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recent_card_container")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Header: "Recent"
                    Text(
                        text = "Recent",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        ),
                        color = DarkTitleColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Row 1: "Your artworks" (Pastel Soft Mint Pill)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = ItemArtworksMint,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(onClick = onNavigateToArtworks)
                            .testTag("recent_your_artworks_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Public,
                                            contentDescription = null,
                                            tint = ItemArtworksIconDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Your artworks",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = DarkTitleColor
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Go",
                                tint = SubtitleMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 2: "Colour gallery" (Pastel Soft Lilac Pill)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = ItemGalleryLavender,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(onClick = onNavigateToColourGallery)
                            .testTag("recent_colour_gallery_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.MenuBook,
                                            contentDescription = null,
                                            tint = ItemGalleryIconDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Colour gallery",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = DarkTitleColor
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Go",
                                tint = SubtitleMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 3: "Themes" (Pastel Soft Peach/Cream-Orange Pill)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = ItemThemesPeach,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { showThemesDialog = true }
                            .testTag("recent_themes_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Public,
                                            contentDescription = null,
                                            tint = ItemThemesIconDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Themes",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = DarkTitleColor
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Go",
                                tint = SubtitleMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. "Log out" Button (Soft Pastel Pink Pill)
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = LogoutPillBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { showLogoutDialog = true }
                    .testTag("profile_logout_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Log out",
                        tint = LogoutPillTextColor,
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        text = "Log out",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = LogoutPillTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
