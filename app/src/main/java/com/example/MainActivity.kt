package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import coil.compose.AsyncImage
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.ui.ChatMessage
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.NadeUiItem
import com.example.ui.NadeViewModel
import com.example.ui.PlaybookItem
import com.example.ui.L10n
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: NadeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold"),
                    containerColor = CsDarkBackground
                ) { innerPadding ->
                    NadeAppScreen(
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Global screen routes/tabs
enum class AppTab(val title: String) {
    CATALOG("Catalog"),
    PLAYBOOK("Playbook"),
    TACTICIAN("Tactician")
}

@Composable
fun NadeAppScreen(
    viewModel: NadeViewModel,
    modifier: Modifier = Modifier
) {
    val isAdminActive by viewModel.isAdminMode.collectAsState()
    val currentLang by viewModel.selectedLanguage.collectAsState()
    L10n.currentLang = currentLang
    var activeTab by remember { mutableStateOf(AppTab.CATALOG) }
    var selectedNadeForDetails by remember { mutableStateOf<NadeUiItem?>(null) }
    var showAddForm by remember { mutableStateOf(false) }
    var showAdminPasswordDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .background(CsDarkBackground)
            .drawBehind {
                // Background futuristic tactical grid lines
                val strokeWidth = 2f
                val sizeVal = 80.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = CsGridLines,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = strokeWidth
                    )
                    x += sizeVal
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = CsGridLines,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                    y += sizeVal
                }
            }
    ) {
        // App Header Bar
        AppHeader(
            activeTab = activeTab,
            onTabSelected = { activeTab = it },
            isAdminActive = isAdminActive,
            onToggleAdminClick = {
                if (isAdminActive) {
                    viewModel.isAdminMode.value = false
                } else {
                    showAdminPasswordDialog = true
                }
            },
            currentLang = currentLang,
            onLanguageSelected = { viewModel.selectedLanguage.value = it }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Screen content transition
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "screentransition"
            ) { tab ->
                when (tab) {
                    AppTab.CATALOG -> CatalogTabScreen(
                        viewModel = viewModel,
                        onNadeClicked = { selectedNadeForDetails = it },
                        onAddNadeClick = { showAddForm = true },
                        isAdminActive = isAdminActive
                    )
                    AppTab.PLAYBOOK -> PlaybookTabScreen(
                        viewModel = viewModel
                    )
                    AppTab.TACTICIAN -> TacticianTabScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // Overlay sheets/Dialogs
    selectedNadeForDetails?.let { nade ->
        NadeDetailsDialog(
            nade = nade,
            onDismiss = { selectedNadeForDetails = null },
            onToggleFavorite = { viewModel.toggleFavorite(nade) },
            onAddToPlaybook = { 
                viewModel.addNadeToPlaybook(nade)
                selectedNadeForDetails = null
            },
            onDeleteCustom = {
                viewModel.deleteCustomNade(nade.id)
                selectedNadeForDetails = null
            },
            isAdminActive = isAdminActive
        )
    }

    if (showAddForm) {
        AddCustomNadeDialog(
            onDismiss = { showAddForm = false },
            onSave = { map, type, side, title, standing, aim, desc, video, image ->
                viewModel.insertCustomNade(map, type, side, title, standing, aim, desc, video, image)
                showAddForm = false
            }
        )
    }

    if (showAdminPasswordDialog) {
        AdminUnlockDialog(
            onDismiss = { showAdminPasswordDialog = false },
            onUnlock = { passcode ->
                if (passcode.trim().equals("so2admin", ignoreCase = true) || passcode.trim().equals("admin", ignoreCase = true)) {
                    viewModel.isAdminMode.value = true
                    showAdminPasswordDialog = false
                    true
                } else {
                    false
                }
            }
        )
    }
}

@Composable
fun AppHeader(
    activeTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    isAdminActive: Boolean,
    onToggleAdminClick: () -> Unit,
    currentLang: String,
    onLanguageSelected: (String) -> Unit
) {
    Surface(
        color = CsSurface,
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(0.1f), Color.Transparent)
                ),
                shape = RoundedCornerShape(0.dp)
            )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tactical Brand logo group
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Small custom drawn tactical scopes
                    Canvas(
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("header_logo_canvas")
                    ) {
                        drawCircle(
                            color = CsOrange,
                            radius = size.width / 2f,
                            style = Stroke(width = 3.dp.toPx())
                        )
                        drawLine(
                            color = CsOrange,
                            start = Offset(size.width / 2f, 0f),
                            end = Offset(size.width / 2f, size.height),
                            strokeWidth = 2.dp.toPx()
                        )
                        drawLine(
                            color = CsOrange,
                            start = Offset(0f, size.height / 2f),
                            end = Offset(size.width, size.height / 2f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "so2nades",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp,
                        letterSpacing = 2.sp
                    )
                }

                // Language + Subtitle + Admin Mode Button Group
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language Selection Pill
                    var langMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CsSurfaceVariant),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .clickable { langMenuExpanded = true }
                                .border(1.dp, CsOrange.copy(0.3f), RoundedCornerShape(4.dp))
                                .testTag("language_selector")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (currentLang) {
                                        "Türkçe" -> "TR"
                                        "Русский" -> "RU"
                                        else -> "EN"
                                    },
                                    color = CsOrange,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Language",
                                    tint = CsOrange,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = langMenuExpanded,
                            onDismissRequest = { langMenuExpanded = false },
                            modifier = Modifier.background(CsSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("English", color = Color.White, fontSize = 13.sp) },
                                onClick = {
                                    onLanguageSelected("English")
                                    langMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Türkçe (Turkish)", color = Color.White, fontSize = 13.sp) },
                                onClick = {
                                    onLanguageSelected("Türkçe")
                                    langMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Русский (Russian)", color = Color.White, fontSize = 13.sp) },
                                onClick = {
                                    onLanguageSelected("Русский")
                                    langMenuExpanded = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CsSurfaceVariant),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = L10n.t("st2_pool"),
                            color = CsOrangeGlow,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAdminActive) CsOrange.copy(0.15f) else CsSurfaceVariant
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clickable { onToggleAdminClick() }
                            .border(
                                width = 1.dp,
                                color = if (isAdminActive) CsOrange else Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .testTag("admin_toggle_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isAdminActive) Icons.Default.Settings else Icons.Default.Lock,
                                contentDescription = if (isAdminActive) L10n.t("admin_active") else L10n.t("normal_user"),
                                tint = if (isAdminActive) CsOrange else CsTextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAdminActive) L10n.t("admin") else L10n.t("mgr"),
                                color = if (isAdminActive) CsOrange else CsTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // Tab bar switcher
            TabRow(
                selectedTabIndex = activeTab.ordinal,
                containerColor = CsSurface,
                contentColor = CsOrange,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                        color = CsOrange,
                        height = 3.dp
                    )
                },
                divider = {
                    HorizontalDivider(color = CsSurfaceVariant)
                },
                modifier = Modifier.height(48.dp)
            ) {
                AppTab.values().forEach { tab ->
                    Tab(
                        selected = activeTab == tab,
                        onClick = { onTabSelected(tab) },
                        text = {
                            Text(
                                text = L10n.t(tab.name.lowercase()).uppercase(),
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = 1.sp,
                                fontSize = 12.sp,
                                color = if (activeTab == tab) CsOrange else CsTextSecondary
                            )
                        },
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    }
}

// ======================= CATALOG TAB SCREEN =======================

@Composable
fun CatalogTabScreen(
    viewModel: NadeViewModel,
    onNadeClicked: (NadeUiItem) -> Unit,
    onAddNadeClick: () -> Unit,
    isAdminActive: Boolean
) {
    val items by viewModel.nadesUiList.collectAsState()
    val rawMap by viewModel.selectedMap.collectAsState()
    val rawType by viewModel.selectedType.collectAsState()
    val rawSide by viewModel.selectedSide.collectAsState()
    val query by viewModel.searchQuery.collectAsState()

    val maps = listOf("All", "Sandstone", "Hanami", "Rust", "Prison", "Dune", "Breeze", "Provinces")
    val types = listOf("All", "Smoke", "Flash", "Molotov")
    val sides = listOf("All", "T", "CT")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Search field
            TextField(
                value = query,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_field"),
                placeholder = { Text(L10n.t("search_setup"), color = CsTextSecondary, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = CsOrange) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = CsTextSecondary)
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = CsTextPrimary,
                    unfocusedTextColor = CsTextPrimary,
                    focusedContainerColor = CsSurface,
                    unfocusedContainerColor = CsSurface,
                    disabledContainerColor = CsSurface,
                    focusedIndicatorColor = CsOrange,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            // MAP Horizontal selective row
            Text(
                text = "SELECT MAP:",
                color = CsTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(maps) { map ->
                    FilterChip(
                        selected = rawMap == map,
                        onClick = { viewModel.selectedMap.value = map },
                        label = { Text(map, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CsOrange,
                            selectedLabelColor = CsDarkBackground,
                            containerColor = CsSurface,
                            labelColor = CsTextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = rawMap == map,
                            borderColor = CsSurfaceVariant,
                            selectedBorderColor = CsOrange
                        )
                    )
                }
            }

            // GRENADE TYPE Horizontal selective row
            Text(
                text = "UTILITY TYPE:",
                color = CsTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(types) { type ->
                    FilterChip(
                        selected = rawType == type,
                        onClick = { viewModel.selectedType.value = type },
                        label = { Text(type.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CsYellow,
                            selectedLabelColor = CsDarkBackground,
                            containerColor = CsSurface,
                            labelColor = CsTextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = rawType == type,
                            borderColor = CsSurfaceVariant,
                            selectedBorderColor = CsYellow
                        )
                    )
                }
            }

            // SIDES selectively
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SIDE:",
                    color = CsTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                sides.forEach { side ->
                    val isSelected = rawSide == side
                    val bkg = when {
                        !isSelected -> CsSurface
                        side == "T" -> CsTOrange
                        side == "CT" -> CsCtBlue
                        else -> CsOrange
                    }
                    val labelColor = if (isSelected) Color.White else CsTextPrimary

                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(bkg)
                            .clickable { viewModel.selectedSide.value = side }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (side == "All") "ALL SIDES" else "$side SIDE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = labelColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main result list
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty",
                            tint = CsTextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No tactical lineups matched filters.",
                            color = CsTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("lineups_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = { it.key }) { nade ->
                        NadeCardItem(
                            nade = nade,
                            onNadeClick = { onNadeClicked(nade) },
                            onToggleFav = { viewModel.toggleFavorite(nade) },
                            onLogPlaybook = { viewModel.addNadeToPlaybook(nade) }
                        )
                    }
                }
            }
        }

        // FAB to add Custom Nades
        if (isAdminActive) {
            FloatingActionButton(
                onClick = onAddNadeClick,
                containerColor = CsOrange,
                contentColor = CsDarkBackground,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("add_custom_nade_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log custom lineup", modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun NadeCardItem(
    nade: NadeUiItem,
    onNadeClick: () -> Unit,
    onToggleFav: () -> Unit,
    onLogPlaybook: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CsSurface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (nade.isFavorite) CsOrange.copy(0.4f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onNadeClick() }
            .testTag("nade_card_${nade.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Utility Icon + Spec badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when (nade.type) {
                                    "Smoke" -> Color.LightGray.copy(0.2f)
                                    "Flash" -> CsYellow.copy(0.2f)
                                    "Molotov" -> CsTOrange.copy(0.2f)
                                    else -> CsCtBlue.copy(0.2f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing custom little tactical symbols
                        Canvas(modifier = Modifier.size(16.dp)) {
                            when (nade.type) {
                                "Smoke" -> {
                                    drawCircle(color = Color.LightGray, radius = size.width / 2.5f)
                                    drawCircle(color = Color.White.copy(0.5f), radius = size.width / 4f)
                                }
                                "Flash" -> {
                                    drawCircle(color = CsYellow, radius = size.width / 4f)
                                    // Sun-like rays
                                    for (i in 0..7) {
                                        val angle = (i * 45) * Math.PI / 180
                                        val startX = (size.width / 2) + (size.width / 4) * Math.cos(angle).toFloat()
                                        val startY = (size.height / 2) + (size.height / 4) * Math.sin(angle).toFloat()
                                        val endX = (size.width / 2) + (size.width / 2.2f) * Math.cos(angle).toFloat()
                                        val endY = (size.height / 2) + (size.height / 2.2f) * Math.sin(angle).toFloat()
                                        drawLine(color = CsYellow, start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = 2f)
                                    }
                                }
                                "Molotov" -> {
                                    // Flame droplet shape
                                    drawCircle(color = CsTOrange, radius = size.width / 3.5f, center = Offset(size.width / 2, size.height * 0.6f))
                                    drawLine(color = CsTOrange, start = Offset(size.width / 2, size.height * 0.2f), end = Offset(size.width * 0.35f, size.height * 0.6f), strokeWidth = 2f)
                                    drawLine(color = CsTOrange, start = Offset(size.width / 2, size.height * 0.2f), end = Offset(size.width * 0.65f, size.height * 0.6f), strokeWidth = 2f)
                                }
                                else -> {
                                    drawCircle(color = CsCtBlue, radius = size.width / 2.5f)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = nade.map.uppercase(),
                            color = CsOrangeGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = nade.type.uppercase(),
                                color = CsTextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Side indicator
                            val sideCol = if (nade.side == "T") CsTOrange else CsCtBlue
                            Text(
                                text = "${nade.side}-SIDE",
                                color = sideCol,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Custom Indicator badge OR favoriting
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (nade.isCustom) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CsSurfaceVariant),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "CUSTOM",
                                color = CsYellow,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleFav,
                        modifier = Modifier.testTag("favorite_btn_${nade.id}")
                    ) {
                        Icon(
                            imageVector = if (nade.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (nade.isFavorite) CsOrange else CsTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Title
            Text(
                text = nade.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Short description snippet
            Text(
                text = nade.description,
                color = CsTextSecondary,
                fontSize = 12.sp,
                maxLines = 2
            )

            HorizontalDivider(
                color = CsSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = CsSurfaceVariant,
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            text = nade.throwType.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CsTextPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        color = CsSurfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = nade.difficulty.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (nade.difficulty) {
                                "Easy" -> CsSuccessGreen
                                "Medium" -> CsYellow
                                "Hard" -> CsOrange
                                else -> CsOrangeGlow
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Add to strategy playbook trigger
                Button(
                    onClick = onLogPlaybook,
                    colors = ButtonDefaults.buttonColors(containerColor = CsSurfaceVariant),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add playbook",
                        tint = CsOrangeGlow,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PLAYBOOK",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = CsOrangeGlow
                    )
                }
            }
        }
    }
}

// ======================= DETAILS DIALOG OVERLAY =======================

@Composable
fun NadeDetailsDialog(
    nade: NadeUiItem,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaybook: () -> Unit,
    onDeleteCustom: () -> Unit,
    isAdminActive: Boolean
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(CsDarkBackground),
            color = CsDarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Back & Top actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onDismiss() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close detailed view",
                            tint = CsOrange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = L10n.t("cancel").uppercase(),
                            color = CsOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Row {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (nade.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = L10n.t("favorite"),
                                tint = if (nade.isFavorite) CsOrange else CsTextPrimary
                            )
                        }

                        if (nade.isCustom && isAdminActive) {
                            IconButton(onClick = onDeleteCustom) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = L10n.t("delete_custom"),
                                    tint = CsErrorRed
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tactical drawing mockup of radar/crosshair lineup!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CsSurface)
                        .border(1.dp, CsOrange.copy(0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Custom drawings representing a Counter Strike 2 scope lineup!
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f

                        // Draw schematic floor grid
                        drawRect(
                            color = CsGridLines,
                            topLeft = Offset(20f, 20f),
                            size = size.copy(width = size.width - 40f, height = size.height - 40f),
                            style = Stroke(width = 1f)
                        )

                        // Outer radar circles
                        drawCircle(color = CsGridLines, radius = 240f, center = Offset(cx, cy), style = Stroke(width = 1f))
                        drawCircle(color = CsOrange.copy(0.15f), radius = 160f, center = Offset(cx, cy), style = Stroke(width = 2f))
                        drawCircle(color = CsOrange.copy(0.05f), radius = 80f, center = Offset(cx, cy), style = Stroke(width = 1f))

                        // Crosshair lines
                        drawLine(color = CsOrangeGlow, start = Offset(cx - 100f, cy), end = Offset(cx + 100f, cy), strokeWidth = 2f)
                        drawLine(color = CsOrangeGlow, start = Offset(cx, cy - 100f), end = Offset(cx, cy + 100f), strokeWidth = 2f)

                        // Core scope dot
                        drawCircle(color = CsOrange, radius = 6f, center = Offset(cx, cy))

                        // Text indicators
                        drawCircle(color = CsYellow, radius = 9f, center = Offset(cx + 130f, cy - 60f))
                    }
                    Text(
                        text = "LINEUP VIEW FINDER",
                        color = CsTextSecondary.copy(0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    )
                    Text(
                        text = nade.map.uppercase(),
                        color = CsOrange.copy(0.5f),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${nade.map.uppercase()} • ${nade.site.uppercase()}",
                                color = CsOrangeGlow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = nade.title,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        if (!nade.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = nade.imageUrl,
                                contentDescription = "Lineup Cover Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(vertical = 12.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, CsOrange.copy(0.3f), RoundedCornerShape(8.dp))
                            )
                        }

                        if (!nade.videoUrl.isNullOrBlank()) {
                            val context = LocalContext.current
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, nade.videoUrl.toUri())
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Handle gracefully
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CsOrange),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .height(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = L10n.t("play_video_guide"),
                                    tint = CsDarkBackground,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = L10n.t("play_video_guide"),
                                    color = CsDarkBackground,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Step-by-step instructions
                        Text(
                            text = L10n.t("standing"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CsYellow,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = CsSurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                              text = nade.standingSpot,
                              color = CsTextPrimary,
                              fontSize = 13.sp,
                              modifier = Modifier.padding(12.dp),
                              lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = L10n.t("aim"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CsYellow,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = CsSurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = nade.aimSpot,
                                color = CsTextPrimary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp),
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = L10n.t("details_and_effects"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CsTextSecondary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = nade.description,
                            color = CsTextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Add to strategy checklist action bottom bar
                Button(
                    onClick = onAddToPlaybook,
                    colors = ButtonDefaults.buttonColors(containerColor = CsOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Check", tint = CsDarkBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = L10n.t("add_to_playbook"),
                        color = CsDarkBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// ======================= PLAYBOOK / PLANNER TAB SCREEN =======================

@Composable
fun PlaybookTabScreen(viewModel: NadeViewModel) {
    val playlist by viewModel.playbookList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Playbook Stat Dashboard
        Surface(
            color = CsSurface,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ACTIVE PREPARATION PLAYBOOK",
                    color = CsOrangeGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val totalCount = playlist.size
                val readyCount = playlist.count { it.isReady }
                val ratio = if (totalCount > 0) readyCount.toFloat() / totalCount.toFloat() else 0f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "LIVE EXECUTE READINESS",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$readyCount of $totalCount utility items prepped",
                            color = CsTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "${(ratio * 100).toInt()}%",
                        color = CsOrange,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom styled raw bar
                LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = CsOrange,
                    trackColor = CsDarkBackground
                )

                if (playlist.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.clearPlaybook() },
                        colors = ButtonDefaults.buttonColors(containerColor = CsSurfaceVariant),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.align(Alignment.End).height(28.dp)
                    ) {
                        Text("RESET PLAYBOOK", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CsErrorRed)
                    }
                }
            }
        }

        // Checklist body
        if (playlist.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "Empty playbook",
                        tint = CsTextSecondary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = L10n.t("playbook_empty"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = L10n.t("playbook_empty_desc"),
                        color = CsTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Text(
                text = "UTILITY DRILLS TO EXECUTE:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CsTextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("playbook_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(playlist, key = { it.nadeKey }) { item ->
                    PlaybookRowItem(
                        item = item,
                        onCheckedChange = { viewModel.togglePlaybookItemReady(item.nadeKey) },
                        onRemove = { viewModel.removeNadeFromPlaybook(item.nadeKey) }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaybookRowItem(
    item: PlaybookItem,
    onCheckedChange: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        color = CsSurface,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (item.isReady) CsSuccessGreen.copy(0.3f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = item.isReady,
                    onCheckedChange = { onCheckedChange() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = CsSuccessGreen,
                        checkmarkColor = CsDarkBackground
                    ),
                    modifier = Modifier.testTag("checkbox_${item.nadeKey}")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = item.title,
                        color = if (item.isReady) CsTextSecondary else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = item.map.uppercase(),
                            color = CsOrangeGlow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = item.type.uppercase(),
                            color = CsTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove from playbook",
                    tint = CsTextSecondary
                )
            }
        }
    }
}

// ======================= TACTICIAN CHAT TAB SCREEN =======================

@Composable
fun TacticianTabScreen(viewModel: NadeViewModel) {
    val messages by viewModel.chatHistory.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()
    var rawText by remember { mutableStateOf("") }

    val presetPrompts = when (L10n.currentLang) {
        "Türkçe" -> listOf(
            "Sandstone B Apartmanları hızlı giriş taktikleri",
            "Provinces A Bölgesi stratejileri",
            "Rust haritasında mid kontrolü",
            "Sandstone uzun yolu nasıl savunulur?"
        )
        "Русский" -> listOf(
            "Тактика быстрого захода на Б аппартаменты Sandstone",
            "Стратегия атаки на А сайт Provinces",
            "Контроль мида на карте Rust",
            "Как защищать лонг на Sandstone?"
        )
        else -> listOf(
            "Sandstone B Apartments rush cover setups",
            "Provinces A Site execute tactics",
            "Rust Mid to B factory control layout",
            "How to defend long on Sandstone?"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Advisor title panel
        Surface(
            color = CsSurface,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CsOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "AI Tactician",
                        tint = CsDarkBackground
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "GEMINI TACTICAL ESCOUT",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Powered by Gemini 3.5 Flash",
                        color = CsOrangeGlow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Preset interactive prompt pills
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presetPrompts) { prompt ->
                Surface(
                    color = CsSurfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.clickable {
                        viewModel.sendChatMessage(prompt)
                    }
                ) {
                    Text(
                        text = prompt,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CsTextPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat lists
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("chat_messages_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(messages) { msg ->
                val isGemini = msg.sender == "gemini"
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isGemini) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    Surface(
                        color = if (isGemini) CsSurface else CsOrange.copy(0.15f),
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomStart = if (isGemini) 0.dp else 8.dp,
                            bottomEnd = if (isGemini) 8.dp else 0.dp
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .border(
                                width = 1.dp,
                                color = if (isGemini) CsSurfaceVariant else CsOrange.copy(0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isGemini) L10n.t("tactician").uppercase() else (if (L10n.currentLang == "Türkçe") "SİZ" else if (L10n.currentLang == "Русский") "ВЫ" else "YOU"),
                                color = if (isGemini) CsOrangeGlow else CsOrange,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                color = CsTextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = CsOrange,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (L10n.currentLang == "Türkçe") "Savaş senaryoları analiz ediliyor..." else if (L10n.currentLang == "Русский") "Анализ боевых сценариев..." else "Analyzing battle scenarios...",
                            color = CsTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Send bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = rawText,
                onValueChange = { rawText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input"),
                placeholder = { Text(L10n.t("ask_tactician"), color = CsTextSecondary, fontSize = 13.sp) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = CsTextPrimary,
                    unfocusedTextColor = CsTextPrimary,
                    focusedContainerColor = CsSurface,
                    unfocusedContainerColor = CsSurface,
                    focusedIndicatorColor = CsOrange,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    viewModel.sendChatMessage(rawText)
                    rawText = ""
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CsOrange)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send advice query",
                    tint = CsDarkBackground
                )
            }
        }
    }
}

// ======================= ADD CUSTOM NADE DIALOG =======================

@Composable
fun AddCustomNadeDialog(
    onDismiss: () -> Unit,
    onSave: (map: String, type: String, side: String, title: String, standing: String, aim: String, desc: String, video: String, image: String) -> Unit
) {
    var mapVal by remember { mutableStateOf("Sandstone") }
    var typeVal by remember { mutableStateOf("Smoke") }
    var sideVal by remember { mutableStateOf("T") }
    var titleVal by remember { mutableStateOf("") }
    var standingVal by remember { mutableStateOf("") }
    var aimVal by remember { mutableStateOf("") }
    var descVal by remember { mutableStateOf("") }
    var videoVal by remember { mutableStateOf("") }
    var imageVal by remember { mutableStateOf("") }

    val maps = listOf("Sandstone", "Hanami", "Rust", "Prison", "Dune", "Breeze", "Provinces")
    val types = listOf("Smoke", "Flash", "Molotov", "HE")
    var isError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            color = CsSurface,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .border(2.dp, CsOrange.copy(0.4f), RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "LOG CUSTOM UTILITY LINEUP",
                    color = CsOrange,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    item {
                        // Title
                        Text("LINEUP TITLE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CsTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        TextField(
                            value = titleVal,
                            onValueChange = { titleVal = it },
                            modifier = Modifier.fillMaxWidth().testTag("custom_title_input"),
                            placeholder = { Text("e.g. A Deep Jungle Screen Smoke", fontSize = 13.sp, color = CsTextSecondary) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = CsTextPrimary,
                                unfocusedTextColor = CsTextPrimary,
                                focusedContainerColor = CsDarkBackground,
                                unfocusedContainerColor = CsDarkBackground
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Select Map & Type selectors
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("STANDOFF 2 MAP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CsTextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                // Map selectors
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CsDarkBackground)
                                        .clickable { 
                                            // Toggle through maps
                                            val idx = maps.indexOf(mapVal)
                                            mapVal = maps[(idx + 1) % maps.size]
                                        }
                                        .padding(12.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(mapVal, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("UTILITY ITEM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CsTextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CsDarkBackground)
                                        .clickable { 
                                            val idx = types.indexOf(typeVal)
                                            typeVal = types[(idx + 1) % types.size]
                                        }
                                        .padding(12.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(typeVal.uppercase(), color = CsYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Side CT / T
                        Text("FACTION SIDE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CsTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("T", "CT").forEach { faction ->
                                val isChosen = sideVal == faction
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isChosen) (if (faction == "T") CsTOrange else CsCtBlue) else CsDarkBackground)
                                        .clickable { sideVal = faction }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (faction == "T") "TERRORIST (T)" else "COUNTER-TERRORIST (CT)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isChosen) Color.White else CsTextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Standing Location
                        Text("STEP 1: STANDING LOCATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CsTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        TextField(
                            value = standingVal,
                            onValueChange = { standingVal = it },
                            modifier = Modifier.fillMaxWidth().testTag("custom_standing_input"),
                            placeholder = { Text("e.g. Back central T-Spawn platform wall shadow corner...", fontSize = 13.sp, color = CsTextSecondary) },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = CsTextPrimary,
                                unfocusedTextColor = CsTextPrimary,
                                focusedContainerColor = CsDarkBackground,
                                unfocusedContainerColor = CsDarkBackground
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Crosshair Aim Spot
                        Text("STEP 2: CROSSHAIR LINING UP AIM SPOT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CsTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        TextField(
                            value = aimVal,
                            onValueChange = { aimVal = it },
                            modifier = Modifier.fillMaxWidth().testTag("custom_aim_input"),
                            placeholder = { Text("e.g. Point cursor to bottom left structural antenna rod tip...", fontSize = 13.sp, color = CsTextSecondary) },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = CsTextPrimary,
                                unfocusedTextColor = CsTextPrimary,
                                focusedContainerColor = CsDarkBackground,
                                unfocusedContainerColor = CsDarkBackground
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Short description
                        Text("STRATEGIC DESCRIPTION NOTES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CsTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        TextField(
                            value = descVal,
                            onValueChange = { descVal = it },
                            modifier = Modifier.fillMaxWidth().testTag("custom_desc_input"),
                            placeholder = { Text("e.g. Standard throw. Renders site entrance completely visually blocked.", fontSize = 13.sp, color = CsTextSecondary) },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = CsTextPrimary,
                                unfocusedTextColor = CsTextPrimary,
                                focusedContainerColor = CsDarkBackground,
                                unfocusedContainerColor = CsDarkBackground
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom Video URL Field
                        Text("CUSTOM VIDEO STREAM URL (OPTIONAL)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CsTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        TextField(
                            value = videoVal,
                            onValueChange = { videoVal = it },
                            modifier = Modifier.fillMaxWidth().testTag("custom_video_input"),
                            placeholder = { Text("e.g. https://www.youtube.com/watch?v=...", fontSize = 13.sp, color = CsTextSecondary) },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = CsTextPrimary,
                                unfocusedTextColor = CsTextPrimary,
                                focusedContainerColor = CsDarkBackground,
                                unfocusedContainerColor = CsDarkBackground
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom Cover Image Field
                        Text("COVER PHOTO / IMAGE URL (OPTIONAL)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CsTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        TextField(
                            value = imageVal,
                            onValueChange = { imageVal = it },
                            modifier = Modifier.fillMaxWidth().testTag("custom_image_input"),
                            placeholder = { Text("e.g. https://images.unsplash.com/photo-...", fontSize = 13.sp, color = CsTextSecondary) },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = CsTextPrimary,
                                unfocusedTextColor = CsTextPrimary,
                                focusedContainerColor = CsDarkBackground,
                                unfocusedContainerColor = CsDarkBackground
                            )
                        )
                    }
                }

                if (isError) {
                    Text(
                        text = "Title, Standing Spot, and Aim Spot cannot be empty!",
                        color = CsErrorRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, CsTextSecondary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CsTextPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCEL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (titleVal.isBlank() || standingVal.isBlank() || aimVal.isBlank()) {
                                isError = true
                            } else {
                                onSave(mapVal, typeVal, sideVal, titleVal, standingVal, aimVal, descVal.ifBlank { "Logged custom tactical setup." }, videoVal, imageVal)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CsOrange),
                        modifier = Modifier.weight(1f).testTag("custom_nade_save_btn")
                    ) {
                        Text("SAVE LINEUP", color = CsDarkBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ======================= ADMIN UNLOCK DIALOG =======================

@Composable
fun AdminUnlockDialog(
    onDismiss: () -> Unit,
    onUnlock: (String) -> Boolean
) {
    var passcodeVal by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(12.dp))
                .background(CsDarkBackground)
                .border(2.dp, CsOrange, RoundedCornerShape(12.dp)),
            color = CsDarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header with secure shield lock icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CsOrange.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Shield Locked",
                            tint = CsOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = L10n.t("admin_gateway"),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = L10n.t("admin_desc"),
                    color = CsTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = L10n.t("enter_key"),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = CsOrangeGlow,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                TextField(
                    value = passcodeVal,
                    onValueChange = {
                        passcodeVal = it
                        hasError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_passcode_input"),
                    placeholder = { Text(L10n.t("enter_key") + "...", fontSize = 13.sp, color = CsTextSecondary) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = CsSurface,
                        unfocusedContainerColor = CsSurface,
                        focusedIndicatorColor = CsOrange,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                if (hasError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = L10n.t("invalid_key"),
                        color = CsErrorRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, CsTextSecondary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CsTextPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(L10n.t("cancel"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val success = onUnlock(passcodeVal)
                            if (!success) {
                                hasError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CsOrange),
                        modifier = Modifier.weight(1f).testTag("admin_unlock_submit_btn")
                    ) {
                        Text(L10n.t("unlock"), color = CsDarkBackground, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
