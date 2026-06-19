package com.example

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.blur
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.fadeIn
import androidx.compose.animation.togetherWith
import kotlinx.coroutines.delay
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<LightMeterViewModel>()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep screen on continuously to prevent automatic dimming/closing
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        setContent {
            val darkModeSetting by viewModel.darkModeSetting.collectAsState()
            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDark = when (darkModeSetting) {
                1 -> false
                2 -> true
                else -> systemDark
            }
            MyApplicationTheme(darkTheme = isDark) {
                val cameraPermissionState = rememberPermissionState(
                    Manifest.permission.CAMERA
                )

                LaunchedEffect(Unit) {
                    if (!cameraPermissionState.status.isGranted) {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }

                if (cameraPermissionState.status.isGranted) {
                    MainAppLayout(viewModel)
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                "Camera Access Required",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "The app uses the phone camera lens and exposure data to accurately sense and measure ambient light (Lux levels).",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { cameraPermissionState.launchPermissionRequest() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Grant Camera Permission", color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class AppHapticType {
    CLICK,       // Touch/Confirm click
    TICK,        // Light clock tick
    LOW_TICK,    // Very subtle low-frequency tick
    THUD,        // Strong confirmation or toggle thud
    ERROR        // Discard or warning thud
}

@Composable
fun rememberAppHaptic(): (AppHapticType) -> Unit {
    val view = LocalView.current
    return { type ->
        val constant = when (type) {
            AppHapticType.CLICK -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                HapticFeedbackConstants.CONFIRM
            } else {
                HapticFeedbackConstants.KEYBOARD_TAP
            }
            AppHapticType.TICK -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                HapticFeedbackConstants.SEGMENT_TICK
            } else {
                HapticFeedbackConstants.CLOCK_TICK
            }
            AppHapticType.LOW_TICK -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
            } else {
                HapticFeedbackConstants.CLOCK_TICK
            }
            AppHapticType.THUD -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                HapticFeedbackConstants.CONTEXT_CLICK
            } else {
                HapticFeedbackConstants.LONG_PRESS
            }
            AppHapticType.ERROR -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                HapticFeedbackConstants.REJECT
            } else {
                HapticFeedbackConstants.LONG_PRESS
            }
        }
        view.performHapticFeedback(constant)
    }
}

@Composable
fun MainAppLayout(viewModel: LightMeterViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val viewingSession by viewModel.viewingSession.collectAsState()
    val triggerHaptics = rememberAppHaptic()

    val isRecording by viewModel.isRecording.collectAsState()
    val stealthModeEnabled by viewModel.stealthModeEnabled.collectAsState()

    val showRoleSelectionPrompt by viewModel.showRoleSelectionPrompt.collectAsState()
    if (showRoleSelectionPrompt) {
        AlertDialog(
            onDismissRequest = { /* Force selection */ },
            title = {
                Text(
                    text = "Multiple Devices Detected",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "Another device has joined the workspace on the network. Please select the role for this device:",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        triggerHaptics(AppHapticType.CLICK)
                        viewModel.setDeviceRole(LightMeterViewModel.DeviceRole.PRIMARY_SENSOR)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Set as Primary")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        triggerHaptics(AppHapticType.CLICK)
                        viewModel.setDeviceRole(LightMeterViewModel.DeviceRole.REMOTE_CONTROLLER)
                    }
                ) {
                    Text("Backup")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp
        )
    }

    val view = androidx.compose.ui.platform.LocalView.current
    val context = LocalContext.current
    DisposableEffect(isRecording) {
        view.keepScreenOn = isRecording
        onDispose {
            view.keepScreenOn = false
        }
    }

    LaunchedEffect(isRecording, stealthModeEnabled) {
        val activity = context as? android.app.Activity
        val window = activity?.window
        if (isRecording && stealthModeEnabled) {
            window?.let {
                val params = it.attributes
                params.screenBrightness = 0.0f // Absolute minimum
                it.attributes = params
            }
        } else {
            window?.let {
                val params = it.attributes
                params.screenBrightness = -1f // Reset to system default
                it.attributes = params
            }
        }
    }

    // Single unified robust back key coordinator incorporating Material 3 haptics
    val canGoBack = selectedTab != 0 || viewingSession != null
    BackHandler(enabled = canGoBack) {
        triggerHaptics(AppHapticType.LOW_TICK) // Play a gentle low tick back confirmation
        if (selectedTab == 1 && viewingSession != null) {
            viewModel.closeSessionDetails()
        } else {
            viewModel.selectTab(0)
        }
    }

    val deviceRole by viewModel.deviceRole.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Timer, contentDescription = "Measure") },
                        label = { Text("Measure") },
                        selected = selectedTab == 0,
                        onClick = {
                            triggerHaptics(AppHapticType.CLICK)
                            viewModel.selectTab(0)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History") },
                        selected = selectedTab == 1,
                        enabled = !isRecording,
                        onClick = {
                            triggerHaptics(AppHapticType.CLICK)
                            viewModel.selectTab(1)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = selectedTab == 2,
                        enabled = !isRecording,
                        onClick = {
                            triggerHaptics(AppHapticType.CLICK)
                            viewModel.selectTab(2)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> MeasureTab(viewModel)
                    1 -> HistoryTab(viewModel)
                    2 -> SettingsTab(viewModel)
                }
            }
        }

        if (isRecording && stealthModeEnabled) {
            StealthRecordingScreen(viewModel = viewModel)
        }
    }
}

@Composable
fun StealthRecordingScreen(viewModel: LightMeterViewModel) {
    val haptic = rememberAppHaptic()
    val lux by viewModel.lux.collectAsState()
    val frameRate by viewModel.frameRate.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures {}
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "RECORDING",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.5.sp
                )
                
                Text(
                    text = String.format("%.3f lx", lux),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Text(
                    text = String.format("%.1f Hz", frameRate),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptic(AppHapticType.ERROR)
                        viewModel.clearRecording()
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .background(Color.Transparent, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Discard Recording",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(
                    onClick = {
                        haptic(AppHapticType.CLICK)
                        viewModel.saveRecording("")
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop & Save Recording",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasureTab(viewModel: LightMeterViewModel) {
    val deviceRole by viewModel.deviceRole.collectAsState()
    val context = LocalContext.current
    val lux by viewModel.lux.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val recordedPoints by viewModel.recordedPoints.collectAsState()
    val showGridLines by viewModel.showGridLines.collectAsState()
    val frameRate by viewModel.frameRate.collectAsState()
    val triggerHaptics = rememberAppHaptic()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    val isRecordButtonEnabled = true

    val isFirebaseConnected by viewModel.isFirebaseConnected.collectAsState()

    var showImportantInfo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Uniform stylized top header (align with history & settings)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "LIGHT METER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isFirebaseConnected) Color(0xFF10B981) else Color(0xFFEF4444))
                    )
                    Text(
                        text = if (isFirebaseConnected) "Connected" else "Disconnected",
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (isFirebaseConnected) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                    IconButton(
                        onClick = {
                            viewModel.recheckFirebaseConnection()
                            triggerHaptics(AppHapticType.LOW_TICK)
                            Toast.makeText(context, "Rechecking connection status...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Connection",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Action triggers for play, info, clear
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/Pause (Recording) button
                IconButton(
                    onClick = {
                        triggerHaptics(AppHapticType.CLICK)
                        if (isRecording) {
                            viewModel.saveRecording("")
                        } else {
                            viewModel.startRecording()
                        }
                    },
                    enabled = isRecordButtonEnabled
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRecording) "Pause & Save" else "Start Measurement",
                        tint = if (!isRecordButtonEnabled) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        } else if (isRecording) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Info button
                IconButton(
                    onClick = {
                        triggerHaptics(AppHapticType.CLICK)
                        showImportantInfo = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Important Information",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Delete/Clear button
                IconButton(
                    onClick = {
                        triggerHaptics(AppHapticType.ERROR)
                        viewModel.clearRecording()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Session",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Unified Live Metrics Card Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Luminance",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    val deviceRole by viewModel.deviceRole.collectAsState()
                    val isPrimarySensorOnline by viewModel.isPrimarySensorOnline.collectAsState()
                    val formattedLuma = if (deviceRole == LightMeterViewModel.DeviceRole.REMOTE_CONTROLLER) {
                        if (isPrimarySensorOnline) {
                            String.format("%.3f", lux)
                        } else {
                            "-"
                        }
                    } else {
                        if (recordedPoints.isNotEmpty() || isRecording) {
                            String.format("%.3f", lux)
                        } else {
                            "-"
                        }
                    }
                    Text(
                        text = formattedLuma,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Frame rate",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    val fpsDisplay = if (isRecording || recordedPoints.isNotEmpty()) {
                        String.format("%.1f Hz", frameRate)
                    } else {
                        "-"
                    }
                    Text(
                        text = fpsDisplay,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Live Plot Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Luminance",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Lv",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.weight(1f)) {
                    CustomLineChart(
                        points = recordedPoints,
                        lineColor = MaterialTheme.colorScheme.primary,
                        showGrid = showGridLines,
                        maxDurationSeconds = 45,
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        gridColor = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "t (s)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Camera Analyzer Preview
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.4f)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (deviceRole == LightMeterViewModel.DeviceRole.PRIMARY_SENSOR) {
                    CameraView(
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(9f / 16f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(16.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(9f / 16f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Camera Idle\n(Backup Mode)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                NamingSelectionUI(
                    viewModel = viewModel,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Important Info Modal Sheet
    if (showImportantInfo) {
        ModalBottomSheet(
            onDismissRequest = { showImportantInfo = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding() // Keeps the knob below status bar & camera punch hole!
                        .padding(top = 12.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    )
                }
            },
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) } // Makes background go full screen!
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Important Information",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "What is being measured?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "This feature measures relative luminance with the phone's camera. It takes into account the brightness of the camera image for a given set of camera settings (like exposure time). The absolute value depends on the camera and unfortunately also some postprocessing from the phone that we cannot prevent.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Text(
                    text = "What errors are introduced by my phone?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Some phones optimize the data from the camera to achieve a nice image by changing the brightness of some parts of the image. We expect that the following are the most common:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Text(
                        text = "• Contrast enhancement: Dark parts of the image become darker and bright parts become brighter. Phones do this if the entire camera image does not have a wide range of brightness.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "• Dynamic range compression: Dark parts become brighter and bright parts become darker. This is done by the phone if the brightest and/or darkest parts of the image are so extreme that details would be lost. On phones that heavily do this, you will barely notice a change in relative luminance when changing the settings of the camera as the postprocessed image keeps a similar brightness.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "• Brightness boost: Most phones will increase the brightness of the image if it is much too dark.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Text(
                    text = "How to measure reliably despite post-processing?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "We recommend to keep the exposure such that the measured area is at medium brightness (i.e. gray). The auto exposure from phyphox will adapt the camera settings to achieve this and this brightness level is mostly unaffected by the post-processing of your phone. The relative luminance is then almost entirely based on the camera settings (especially ISO and exposure time) that are required to achieve this brightness. However, the auto exposure takes a moment to settle, so you should wait a moment before taking relative luminance measure and observe if the preview image settles in a medium gray brightness.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Text(
                    text = "Details for advanced users",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "The raw data from the camera is converted to RGB and the individual RGB channels are linearized assuming the gamma curve of sRGB, and then converted to luma. The relative luminance is the luma value normalized to the camera settings such that a pure white image yields a relative luminance of 1 if the camera is set to an exposure time of 1/60s an ISO of 100 and an aperture of f/1 (Note, that this still does not allow for comparison across different models.).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Button(
                    onClick = { showImportantInfo = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SmallInfoTile(title: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableDeletedCard(
    session: com.example.db.MeasurementSession,
    onRestoreClick: () -> Unit,
    onPermanentlyDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * 0.8f },
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onPermanentlyDeleteClick()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onRestoreClick()
                    true
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        modifier = modifier,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary // Restore color
                SwipeToDismissBoxValue.EndToStart -> Color(0xFFEF4444) // Delete color
                else -> Color.Transparent
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Refresh // Restore icon
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete // Delete icon
                else -> null
            }
            val label = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> "Restore"
                SwipeToDismissBoxValue.EndToStart -> "Delete"
                else -> ""
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                if (icon != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (direction == SwipeToDismissBoxValue.StartToEnd) {
                            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onPrimary)
                            Text(label, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        } else {
                            Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Icon(icon, contentDescription = label, tint = Color.White)
                        }
                    }
                }
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textDecoration = TextDecoration.LineThrough
                    )
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy  •  HH:mm", Locale.getDefault()).format(Date(session.timestamp)),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SwipeableHistoryCard(
    session: com.example.db.MeasurementSession,
    onCardClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDeleteClick()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onRenameClick()
                    false // Return false so it snaps back elegantly!
                }
                else -> false
            }
        }
    )

    val triggerHaptics = rememberAppHaptic()

    val scale = 1.0f
    val extraPadding = 0.dp
    val elevation = 1.dp

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = !isSelectionMode, // Disable swiping when selecting to avoid interaction conflicts!
        enableDismissFromEndToStart = !isSelectionMode,
        modifier = modifier,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                SwipeToDismissBoxValue.EndToStart -> Color(0xFFEF4444)
                else -> Color.Transparent
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                else -> null
            }
            val label = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> "Rename"
                SwipeToDismissBoxValue.EndToStart -> "Delete"
                else -> ""
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                if (icon != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (direction == SwipeToDismissBoxValue.StartToEnd) {
                            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onPrimary)
                            Text(label, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        } else {
                            Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Icon(icon, contentDescription = label, tint = Color.White)
                        }
                    }
                }
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = extraPadding)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .combinedClickable(
                    onClick = {
                        triggerHaptics(AppHapticType.CLICK)
                        onCardClick()
                    },
                    onLongClick = {
                        triggerHaptics(AppHapticType.THUD)
                        onLongClick()
                    }
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
                                 else MaterialTheme.colorScheme.surface
            ),
            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy  •  HH:mm", Locale.getDefault()).format(Date(session.timestamp)),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        Text(
                            text = "Avg: ${String.format("%.0f", session.avgLux)} lx",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Duration: ${String.format("%.1fs", session.durationMs / 1000.0)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Open session details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryTab(viewModel: LightMeterViewModel) {
    val context = LocalContext.current
    val allSessions by viewModel.allSessions.collectAsState()
    val viewingSession by viewModel.viewingSession.collectAsState()
    val viewingSessionPoints by viewModel.viewingSessionPoints.collectAsState()
    val triggerHaptics = rememberAppHaptic()

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedSessions by remember { mutableStateOf(setOf<Long>()) }

    // Chart Design Customization Config States
    val chartColorHex by viewModel.chartColorHex.collectAsState()
    val showGridLines by viewModel.showGridLines.collectAsState()

    var editingSessionForRename by remember { mutableStateOf<com.example.db.MeasurementSession?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var showRecentlyDeleted by remember { mutableStateOf(true) }
    var showDeleteAllConfirmation by remember { mutableStateOf(false) }
    val recentlyDeletedSessions by viewModel.recentlyDeletedSessions.collectAsState()

    if (editingSessionForRename != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(editingSessionForRename) {
            delay(350)
            try {
                focusRequester.requestFocus()
            } catch (e: Exception) {
                // ignore
            }
        }

        ModalBottomSheet(
            onDismissRequest = { editingSessionForRename = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Rename Session",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Enter a new title for this light recording:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    singleLine = true,
                    placeholder = { Text("Session Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { editingSessionForRename = null },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            editingSessionForRename?.let { session ->
                                val finalTitle = renameInput.trim()
                                if (finalTitle.isNotEmpty()) {
                                    viewModel.updateSessionTitle(session.id, finalTitle)
                                }
                            }
                            editingSessionForRename = null
                            renameInput = ""
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showDeleteAllConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirmation = false },
            title = {
                Text(
                    text = "Clear Recently Deleted?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "This will permanently delete all recently deleted sessions. This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllPermanently()
                        showDeleteAllConfirmation = false
                    }
                ) {
                    Text(
                        text = "Delete All",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAllConfirmation = false }
                ) {
                    Text(text = "Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (viewingSession != null) {
        // Detailed session page inside history
        val session = viewingSession!!
        
        BackHandler {
            viewModel.closeSessionDetails()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.closeSessionDetails() }) {
                    Icon(
                        Icons.Default.ArrowBack, 
                        contentDescription = "Go back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(session.timestamp)),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = {
                        viewModel.deleteSession(session.id)
                    }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Session", tint = Color(0xFFEF4444))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Readout Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MIN LEVEL", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(String.format("%.0f lx", session.minLux), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AVG LEVEL", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(String.format("%.0f lx", session.avgLux), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MAX LEVEL", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(String.format("%.0f lx", session.maxLux), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HISTORICAL MEASUREMENTS PLOT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val mappedPoints = viewingSessionPoints.map { Pair(it.timeMillis, it.lux) }
                    val currentPrimaryColor = MaterialTheme.colorScheme.primary
                    Box(modifier = Modifier.weight(1f)) {
                        if (mappedPoints.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Rendering datapoints...")
                            }
                        } else {
                            CustomLineChart(
                                points = mappedPoints,
                                lineColor = currentPrimaryColor,
                                showGrid = showGridLines,
                                maxDurationSeconds = 0
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action triggers including Download / Export image!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Download Image (Left Button)
                Button(
                    onClick = {
                        triggerHaptics(AppHapticType.CLICK)
                        val rawPoints = viewingSessionPoints.map { Pair(it.timeMillis, it.lux) }
                        saveChartToDownloadsGallery(
                            context = context,
                            points = rawPoints,
                            sessionTitle = session.title,
                            lineColorHex = chartColorHex,
                            showGrid = showGridLines
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Download Image"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Download Image",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    }
                }

                // Download Excel (Right Button)
                Button(
                    onClick = {
                        triggerHaptics(AppHapticType.CLICK)
                        viewModel.exportSelectedSessionsToExcel(setOf(session.id), context)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Excel"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Download Excel",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    } else {
        // Sessions listing view
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (isSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedSessions = emptySet()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Exit selection", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${selectedSessions.size} selected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
                    Row {
                        IconButton(onClick = {
                            if (selectedSessions.size == allSessions.size) {
                                selectedSessions = emptySet()
                                isSelectionMode = false
                            } else {
                                selectedSessions = allSessions.map { it.id }.toSet()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.SelectAll,
                                contentDescription = "Select All",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        IconButton(onClick = {
                            if (selectedSessions.isNotEmpty()) {
                                viewModel.exportSelectedSessionsToExcel(selectedSessions, context)
                            } else {
                                android.widget.Toast.makeText(context, "No sessions selected to export", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Download, contentDescription = "Download Excel", tint = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(onClick = {
                            if (selectedSessions.isNotEmpty()) {
                                selectedSessions.forEach { id ->
                                    viewModel.deleteSession(id)
                                }
                                isSelectionMode = false
                                selectedSessions = emptySet()
                                android.widget.Toast.makeText(context, "Selected sessions deleted", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = Color(0xFFEF4444))
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SAVED HISTORIC RUNS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Measurements History",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            val isRefreshing by viewModel.isRefreshing.collectAsState()

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshFirebaseSessions() },
                modifier = Modifier.fillMaxSize()
            ) {
                if (allSessions.isEmpty() && recentlyDeletedSessions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 40.dp)) {
                            Text(
                                text = "No saved sessions yet",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Go to 'Measure' tab to begin recording measurements and save your calibrations.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 32.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (allSessions.isNotEmpty()) {
                            items(
                                items = allSessions,
                                key = { it.id }
                            ) { session ->
                                SwipeableHistoryCard(
                                    session = session,
                                    isSelected = selectedSessions.contains(session.id),
                                    isSelectionMode = isSelectionMode,
                                    onCardClick = {
                                        if (isSelectionMode) {
                                            selectedSessions = if (selectedSessions.contains(session.id)) {
                                                val next = selectedSessions - session.id
                                                if (next.isEmpty()) {
                                                    isSelectionMode = false
                                                }
                                                next
                                            } else {
                                                selectedSessions + session.id
                                            }
                                        } else {
                                            viewModel.loadSessionDetails(session)
                                        }
                                    },
                                    onLongClick = {
                                        if (!isSelectionMode) {
                                            isSelectionMode = true
                                            selectedSessions = setOf(session.id)
                                        }
                                    },
                                    onRenameClick = {
                                        editingSessionForRename = session; renameInput = ""
                                    },
                                    onDeleteClick = {
                                        viewModel.deleteSession(session.id)
                                    },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        } else {
                            item {
                                Text(
                                    "No active measurements history.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        }

                    if (recentlyDeletedSessions.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Recently Deleted",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Recently Deleted (${recentlyDeletedSessions.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                TextButton(
                                    onClick = { showDeleteAllConfirmation = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete all",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Delete All",
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        if (showRecentlyDeleted) {
                            items(
                                items = recentlyDeletedSessions,
                                key = { "del_${it.id}" }
                            ) { session ->
                                SwipeableDeletedCard(
                                    session = session,
                                    onRestoreClick = { viewModel.restoreSession(session.id) },
                                    onPermanentlyDeleteClick = { viewModel.deleteSessionPermanently(session.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun SettingsTab(viewModel: LightMeterViewModel) {
    val showGridLines by viewModel.showGridLines.collectAsState()
    val darkModeSetting by viewModel.darkModeSetting.collectAsState()
    val isFirebaseConnected by viewModel.isFirebaseConnected.collectAsState()
    
    val iso by viewModel.iso.collectAsState()
    val exp by viewModel.exposureTime.collectAsState()
    val ev by viewModel.ev.collectAsState()
    
    val haptic = LocalHapticFeedback.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showResetConfirmation by remember { mutableStateOf(false) }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = {
                Text(
                    text = "Reset Firebase Data",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to clear the real-time lux reading and the live data stream on Firebase? This will also stop any active remote recording.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetFirebaseData()
                        showResetConfirmation = false
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "APP CONFIGURATION",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Firebase Cloud Sync Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "FIREBASE CLOUD SYNC",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Connection Status",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isFirebaseConnected) Color(0xFF10B981) else Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFirebaseConnected) "Connected" else "Disconnected",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFirebaseConnected) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                viewModel.recheckFirebaseConnection()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                Toast.makeText(context, "Rechecking connection status...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Connection",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showResetConfirmation = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Firebase Data",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Remote Firebase Data", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Experiment Type Configuration
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXPERIMENT IDENTIFICATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    var showAddSheet by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { 
                            showAddSheet = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add type",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    if (showAddSheet) {
                        AddExperimentTypeBottomSheet(
                            viewModel = viewModel,
                            onDismiss = { showAddSheet = false }
                        )
                    }
                }
                
                ExperimentTypeConfigUI(viewModel = viewModel)
            }
        }

        // Stealth Mode Toggle
        val stealthModeEnabled by viewModel.stealthModeEnabled.collectAsState()
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "STEALTH MODE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Switch(
                    checked = stealthModeEnabled,
                    onCheckedChange = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.setStealthModeEnabled(it) 
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }

        // Data Monitoring Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Data Monitoring",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DATA MONITORING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "https://alex-ckshen.github.io/blue-bottle/",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = {
                                val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clipData = android.content.ClipData.newPlainText("Data Monitoring URL", "https://alex-ckshen.github.io/blue-bottle/")
                                clipboardManager.setPrimaryClip(clipData)
                                Toast.makeText(context, "Link copied!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Copy", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        
                        IconButton(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://alex-ckshen.github.io/blue-bottle/")).apply {
                                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Unable to open web browser", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Launch,
                                contentDescription = "Open Web Dashboard",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section 1: Live Hardware Metrics (Moved from Measure Tab)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "LIVE CAMERA METRICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        SmallInfoTile("ISO SENSITIVITY", iso.toString())
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SmallInfoTile("SHUTTER SPEED", String.format("%.1f ms", exp))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SmallInfoTile("EXPOSURE VALUE", String.format("%.1f EV", ev))
                    }
                }
            }
        }

        // Section 2: Appearance Theme Control Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "APPEARANCE THEME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themeOptions = listOf(
                        0 to "System",
                        1 to "Light",
                        2 to "Dark"
                    )
                    themeOptions.forEach { option ->
                        val isSelected = darkModeSetting == option.first
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setDarkModeSetting(option.first)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(option.second, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 3: Phyphox Credits Card (Replaces Manual Calibration Card)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "PHYPHOX SYSTEM CREDITS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "This application incorporates de.rwth_aachen.phyphox relative camera luminance analyzer algorithms. It is designed for academic, scientific, and educational sensor research.\n\nAll rights, patents, and credits for the original Phyphox experiment definitions and analyzer structures are held by RWTH Aachen University.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * Maps the calculated Lux metric to user friendly standard environments descriptors
 */
fun getApproximatedEnvironment(lux: Float): String {
    return when {
        lux < 2f -> "Very Dark (Moonlit Outdoors)"
        lux < 40f -> "Low Light (Ambient Night Corridor)"
        lux < 100f -> "Dim Room (Residential Storage Space)"
        lux < 250f -> "Standard Indoor (Residential Living Area)"
        lux < 600f -> "Optimal Working (Office / Task Workspace)"
        lux < 1500f -> "Bright Indoor (Showrooms / Gymnasiums)"
        lux < 8000f -> "Indirect Outdoor Daylight"
        else -> "Very Bright (Direct Sunny Outdoors)"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamingSelectionUI(viewModel: LightMeterViewModel, modifier: Modifier = Modifier) {
    val experimentTypes by viewModel.experimentTypes.collectAsState()
    val selectedExperiment by viewModel.selectedExperimentType.collectAsState()
    val selectedTrial by viewModel.selectedTrial.collectAsState()

    var showDropdown by remember { mutableStateOf(false) }
    var showCustomTrialSheet by remember { mutableStateOf(false) }
    var customTrialText by remember { mutableStateOf("") }

    val currentDisplay = selectedExperiment.ifEmpty { "Select Type" }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    Column(
        modifier = modifier
            .padding(vertical = 4.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dropdown box for experiment type
        Text("Experiment Type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showDropdown = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(currentDisplay, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select")
                }
            }
            androidx.compose.material3.DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false }
            ) {
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("None (Clear)") },
                    onClick = {
                        viewModel.setExperimentType("")
                        showDropdown = false
                    }
                )
                if (experimentTypes.isEmpty()) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("No types added yet") },
                        enabled = false,
                        onClick = { showDropdown = false }
                    )
                } else {
                    experimentTypes.forEach { type ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                viewModel.setExperimentType(type)
                                showDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // Segmented Trial Control
        Text("Trial Select", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val trials = listOf("1", "2", "3", "4", "5", "custom")
            trials.forEach { trial ->
                val isCustomAction = trial == "custom"
                val isSelected = if (isCustomAction) {
                    !listOf("1", "2", "3", "4", "5").contains(selectedTrial) && selectedTrial.isNotEmpty()
                } else {
                    selectedTrial == trial
                }
                
                Card(
                    modifier = Modifier.weight(1.0f).clickable {
                        if (isCustomAction) {
                            customTrialText = ""
                            showCustomTrialSheet = true
                        } else {
                            viewModel.setTrial(trial)
                        }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(modifier = Modifier.padding(vertical = 12.dp).fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (isCustomAction && !isSelected) {
                             Icon(Icons.Default.Edit, contentDescription = "Custom", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text(
                                text = if (isCustomAction && isSelected) selectedTrial else trial,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCustomTrialSheet) {
        val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
        val focusRequester = remember { FocusRequester() }
        val keyboard = LocalSoftwareKeyboardController.current

        ModalBottomSheet(
            onDismissRequest = { showCustomTrialSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Custom Trial Number",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = customTrialText,
                    onValueChange = { customTrialText = it },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    singleLine = true,
                    placeholder = { Text("Enter number") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = {
                            if (customTrialText.isNotBlank()) {
                                viewModel.setTrial(customTrialText)
                                showCustomTrialSheet = false
                            }
                        }
                    )
                )

                LaunchedEffect(Unit) {
                    delay(300)
                    focusRequester.requestFocus()
                    keyboard?.show()
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showCustomTrialSheet = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (customTrialText.isNotEmpty()) {
                                viewModel.setTrial(customTrialText)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            showCustomTrialSheet = false
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Set Trial")
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AddExperimentTypeBottomSheet(viewModel: LightMeterViewModel, onDismiss: () -> Unit) {
    var newType by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add Experiment Type",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = newType,
                onValueChange = { newType = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = { Text("Enter type name...") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = {
                        if (newType.isNotBlank()) {
                            viewModel.addExperimentType(newType)
                            onDismiss()
                        }
                    }
                )
            )
            Button(
                onClick = {
                    if (newType.isNotBlank()) {
                        viewModel.addExperimentType(newType)
                        onDismiss()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Type", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ExperimentTypeConfigUI(viewModel: LightMeterViewModel) {
    val experimentTypes by viewModel.experimentTypes.collectAsState()
    val haptic = LocalHapticFeedback.current
    val selectedExperiment by viewModel.selectedExperimentType.collectAsState()
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (experimentTypes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No custom types added yet",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontStyle = FontStyle.Italic
                )
            }
        } else {
            Column(
                modifier = Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                experimentTypes.forEach { type ->
                    val selected = selectedExperiment == type
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setExperimentType(if (selected) "" else type)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = type, 
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                viewModel.removeExperimentType(type)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete, 
                                    contentDescription = "Remove", 
                                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, 
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
