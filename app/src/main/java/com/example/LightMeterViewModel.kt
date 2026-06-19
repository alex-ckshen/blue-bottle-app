package com.example

import android.app.Application
import android.hardware.camera2.CaptureResult
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.db.AppDatabase
import com.example.db.MeasurementDatapoint
import com.example.db.MeasurementSession
import com.example.db.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.resume
import kotlin.math.log2
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ChildEventListener

class LightMeterViewModel(application: Application) : AndroidViewModel(application) {
    private val deviceId: String by lazy {
        val prefs = getApplication<Application>().getSharedPreferences("light_meter_prefs", android.content.Context.MODE_PRIVATE)
        var id = prefs.getString("device_id", null)
        if (id == null) {
            id = java.util.UUID.randomUUID().toString()
            prefs.edit().putString("device_id", id).apply()
        }
        id
    }

    private val database = AppDatabase.getDatabase(application)
    val repository = SessionRepository(database.sessionDao())

    private val firebaseDb: DatabaseReference by lazy {
        FirebaseDatabase.getInstance("https://blue-bottle-experiment-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    }
    private var remoteSessionName = "Remote Session"

    enum class DeviceRole { UNKNOWN, PRIMARY_SENSOR, REMOTE_CONTROLLER }
    private val _deviceRole = MutableStateFlow(DeviceRole.UNKNOWN)
    val deviceRole: StateFlow<DeviceRole> = _deviceRole.asStateFlow()

    private val _showRoleSelectionPrompt = MutableStateFlow(false)
    val showRoleSelectionPrompt: StateFlow<Boolean> = _showRoleSelectionPrompt.asStateFlow()

    private val _isFirebaseConnected = MutableStateFlow(false)
    val isFirebaseConnected = _isFirebaseConnected.asStateFlow()

    private var pendingRoleSelectionDialog = false
    private var hasPromptedForMultipleDevices = false
    private var lastOtherDevicesCount = 0

    fun setDeviceRole(role: DeviceRole) {
        _deviceRole.value = role
        _showRoleSelectionPrompt.value = false
        
        try {
            val myDeviceRef = firebaseDb.child("devices").child(deviceId)
            if (role == DeviceRole.PRIMARY_SENSOR) {
                myDeviceRef.setValue("primary")
                myDeviceRef.onDisconnect().removeValue()
                
                val presenceRef = firebaseDb.child("status").child("appOnline")
                presenceRef.setValue(true)
                presenceRef.onDisconnect().setValue(false)
                
                firebaseDb.child("currentLux").onDisconnect().removeValue()
            } else if (role == DeviceRole.REMOTE_CONTROLLER) {
                myDeviceRef.setValue("remote")
                myDeviceRef.onDisconnect().removeValue()
            } else if (role == DeviceRole.UNKNOWN) {
                myDeviceRef.setValue("unknown")
                myDeviceRef.onDisconnect().removeValue()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initializeFirebaseAndSync() {
        viewModelScope.launch {
            val sevenDaysAgo = System.currentTimeMillis() - 7L * 24L * 60L * 60L * 1000L
            repository.purgeOldDeleted(sevenDaysAgo)
        }

        try {
            // Signal presence initially as unknown to negotiate role
            setDeviceRole(DeviceRole.UNKNOWN)

            firebaseDb.child("status").child("appOnline").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val online = snapshot.getValue(Boolean::class.java) ?: false
                    _isPrimarySensorOnline.value = online
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            firebaseDb.child("currentLux").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_deviceRole.value == DeviceRole.REMOTE_CONTROLLER) {
                        _lux.value = snapshot.getValue(Float::class.java) ?: 0f
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            firebaseDb.child("devices").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val deviceRolesMap = snapshot.children.associate { it.key to (it.value as? String ?: "") }
                    val otherDevices = deviceRolesMap.filter { it.key != deviceId }
                    val otherDevicesCount = otherDevices.size
                    
                    // 1) If we are the only device on the network
                    if (otherDevicesCount == 0) {
                        if (_deviceRole.value != DeviceRole.PRIMARY_SENSOR) {
                            setDeviceRole(DeviceRole.PRIMARY_SENSOR)
                        }
                        _showRoleSelectionPrompt.value = false
                        lastOtherDevicesCount = 0
                        return
                    }
                    
                    val otherHasPrimary = otherDevices.values.contains("primary")
                    
                    // 2) If a new device joins (count dynamically grows) and we are not currently recording
                    if (otherDevicesCount > lastOtherDevicesCount && !_isRecording.value) {
                        if (_deviceRole.value != DeviceRole.UNKNOWN) {
                            setDeviceRole(DeviceRole.UNKNOWN)
                        }
                        _showRoleSelectionPrompt.value = true
                    } else if (otherHasPrimary) {
                        // 3) Automatic backup configuration if another device claims or is selected as primary
                        if (_deviceRole.value != DeviceRole.REMOTE_CONTROLLER) {
                            setDeviceRole(DeviceRole.REMOTE_CONTROLLER)
                        }
                        _showRoleSelectionPrompt.value = false
                    }
                    
                    lastOtherDevicesCount = otherDevicesCount
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            firebaseDb.database.getReference(".info/connected").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val connected = snapshot.getValue(Boolean::class.java) ?: false
                    _isFirebaseConnected.value = connected
                    if (connected) {
                        try {
                            val role = _deviceRole.value
                            if (role != DeviceRole.UNKNOWN) {
                                setDeviceRole(role)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            firebaseDb.child("commands").child("sessionName").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.getValue(String::class.java)
                    if (!name.isNullOrEmpty()) {
                        remoteSessionName = name
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            firebaseDb.child("commands").child("experimentTypes").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val types = mutableListOf<String>()
                    for (child in snapshot.children) {
                        child.getValue(String::class.java)?.let { types.add(it) }
                    }
                    _experimentTypes.value = types
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            firebaseDb.child("commands").child("selectedExperimentType").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _selectedExperimentType.value = snapshot.getValue(String::class.java) ?: ""
                    updateSessionName()
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            firebaseDb.child("commands").child("selectedTrial").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _selectedTrial.value = snapshot.getValue(String::class.java) ?: "1"
                    updateSessionName()
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            firebaseDb.child("commands").child("isRecording").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val firebaseIsRecording = snapshot.getValue(Boolean::class.java) ?: false
                    if (firebaseIsRecording != _isRecording.value) {
                        viewModelScope.launch {
                            if (firebaseIsRecording) {
                                selectTab(0) // Switch to measure tab first to force screen state
                                startRecording()
                            } else {
                                saveRecording(remoteSessionName)
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            firebaseDb.child("commands").child("clearAllAction").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val actionTime = snapshot.getValue(Long::class.java) ?: return
                    viewModelScope.launch {
                        deleteAllPermanently()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            firebaseDb.child("sessions").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<MeasurementSession>()
                    for (child in snapshot.children) {
                        try {
                            val id = child.child("id").getValue(Long::class.java)
                                ?: child.key?.removePrefix("session_")?.toLongOrNull() ?: continue
                            val title = child.child("title").getValue(String::class.java) ?: "Remote Session"
                            val timestamp = child.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()
                            val durationMs = child.child("durationMs").getValue(Long::class.java) ?: 0L
                            val avgLux = child.child("avgLux").value?.let {
                                if (it is Number) it.toFloat() else 0f
                            } ?: 0f
                            val maxLux = child.child("maxLux").value?.let {
                                if (it is Number) it.toFloat() else 0f
                            } ?: 0f
                            val minLux = child.child("minLux").value?.let {
                                if (it is Number) it.toFloat() else 0f
                            } ?: 0f
                            
                            list.add(
                                MeasurementSession(
                                    id = id,
                                    title = title,
                                    timestamp = timestamp,
                                    durationMs = durationMs,
                                    avgLux = avgLux,
                                    maxLux = maxLux,
                                    minLux = minLux,
                                    deletedAt = null
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    list.sortByDescending { it.timestamp }
                    _allSessionsFirebase.value = list
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Tabs navigation: 0 = Measure, 1 = History, 2 = Settings
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    // Dark Mode settings: 0 = System Default, 1 = Always Light, 2 = Always Dark
    private val _darkModeSetting = MutableStateFlow(0)
    val darkModeSetting = _darkModeSetting.asStateFlow()

    fun setDarkModeSetting(setting: Int) {
        _darkModeSetting.value = setting
    }

    // Realtime metrics
    private val _isPrimarySensorOnline = MutableStateFlow(false)
    val isPrimarySensorOnline = _isPrimarySensorOnline.asStateFlow()

    private val _lux = MutableStateFlow(0f)
    val lux = _lux.asStateFlow()

    private val _ev = MutableStateFlow(0f)
    val ev = _ev.asStateFlow()
    
    private val _iso = MutableStateFlow(0)
    val iso = _iso.asStateFlow()
    
    private val _exposureTime = MutableStateFlow(0f)
    val exposureTime = _exposureTime.asStateFlow()

    private val _aperture = MutableStateFlow(1.8f)
    val aperture = _aperture.asStateFlow()

    // Interactive passepartout measuring region (normalized screen space)
    private val _passepartout = MutableStateFlow(androidx.compose.ui.geometry.Rect(0.4f, 0.4f, 0.6f, 0.6f))
    val passepartout = _passepartout.asStateFlow()

    // Realtime analysis metrics from Phyphox
    private val _frameRate = MutableStateFlow(0f)
    val frameRate = _frameRate.asStateFlow()

    // Recording States
    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _recordingStartTime = MutableStateFlow(0L)
    val recordingStartTime = _recordingStartTime.asStateFlow()

    private val _recordedPoints = MutableStateFlow<List<Pair<Long, Float>>>(emptyList())
    val recordedPoints = _recordedPoints.asStateFlow()

    private var lastSampleTime = 0L
    private var lastUiUpdateTime = 0L

    private val recordedPointsList = java.util.Collections.synchronizedList(ArrayList<Pair<Long, Float>>())
    private var samplingJob: Job? = null

    // For active session stats
    private val _currentMin = MutableStateFlow(Float.MAX_VALUE)
    val currentMin = _currentMin.asStateFlow()

    private val _currentMax = MutableStateFlow(0f)
    val currentMax = _currentMax.asStateFlow()

    private val _currentAvg = MutableStateFlow(0f)
    val currentAvg = _currentAvg.asStateFlow()

    private var currentSum = 0f
    private var currentCount = 0

    // Firebase flow for all saved sessions
    private val _allSessionsFirebase = MutableStateFlow<List<MeasurementSession>>(emptyList())
    val allSessions = _allSessionsFirebase.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    // Database flow for recently deleted sessions
    val recentlyDeletedSessions = repository.recentlyDeletedSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Viewing specific saved session detail
    private val _viewingSession = MutableStateFlow<MeasurementSession?>(null)
    val viewingSession = _viewingSession.asStateFlow()

    private val _viewingSessionPoints = MutableStateFlow<List<MeasurementDatapoint>>(emptyList())
    val viewingSessionPoints = _viewingSessionPoints.asStateFlow()

    // Custom Chart Settings
    private val _chartColorHex = MutableStateFlow("#2563EB") // Solid blue default
    val chartColorHex = _chartColorHex.asStateFlow()

    private val _showGridLines = MutableStateFlow(true)
    val showGridLines = _showGridLines.asStateFlow()

    private val _chartMaxDurationSeconds = MutableStateFlow(30) // Trailing view limit
    val chartMaxDurationSeconds = _chartMaxDurationSeconds.asStateFlow()

    private val _stealthModeEnabled = MutableStateFlow(true)
    val stealthModeEnabled = _stealthModeEnabled.asStateFlow()

    fun setStealthModeEnabled(enabled: Boolean) {
        _stealthModeEnabled.value = enabled
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
        if (tabIndex != 1) {
            // clear session viewer when leaving history tab
            _viewingSession.value = null
            _viewingSessionPoints.value = emptyList()
        }
    }

    fun updatePassepartout(rect: androidx.compose.ui.geometry.Rect) {
        _passepartout.value = rect
    }

    fun updateFrameRate(fps: Float) {
        _frameRate.value = fps
    }

    // Naming config states
    private val _experimentTypes = MutableStateFlow<List<String>>(emptyList())
    val experimentTypes: StateFlow<List<String>> = _experimentTypes.asStateFlow()

    private val _selectedExperimentType = MutableStateFlow("")
    val selectedExperimentType: StateFlow<String> = _selectedExperimentType.asStateFlow()

    private val _selectedTrial = MutableStateFlow("1")
    val selectedTrial: StateFlow<String> = _selectedTrial.asStateFlow()

    fun setExperimentType(type: String) {
        _selectedExperimentType.value = type
        updateSessionName()
        try {
            firebaseDb.child("commands").child("selectedExperimentType").setValue(type)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun setTrial(trial: String) {
        _selectedTrial.value = trial
        updateSessionName()
        try {
            firebaseDb.child("commands").child("selectedTrial").setValue(trial)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun addExperimentType(type: String) {
        val current = _experimentTypes.value.toMutableList()
        if (!current.contains(type)) {
            current.add(type)
            _experimentTypes.value = current
            try {
                firebaseDb.child("commands").child("experimentTypes").setValue(current)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun removeExperimentType(type: String) {
        val current = _experimentTypes.value.toMutableList()
        if (current.remove(type)) {
            _experimentTypes.value = current
            try {
                firebaseDb.child("commands").child("experimentTypes").setValue(current)
            } catch (e: Exception) { e.printStackTrace() }
        }
        if (_selectedExperimentType.value == type) {
            setExperimentType("")
        }
    }

    private fun updateSessionName() {
        val base = _selectedExperimentType.value.ifEmpty { "Session" }
        val suffix = _selectedTrial.value.ifEmpty { "" }
        val newName = if (suffix.isNotEmpty()) "$base - $suffix" else base
        try {
            firebaseDb.child("commands").child("sessionName").setValue(newName)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private var lastFirebasePushTime = 0L

    fun updateLuminanceValue(luminanceValue: Float) {
        _lux.value = luminanceValue
    }

    private fun startSamplingTask() {
        samplingJob?.cancel()
        samplingJob = viewModelScope.launch(Dispatchers.Default) {
            val startTime = System.currentTimeMillis()
            var tickCount = 0L
            var lastUiUpdate = 0L
            while (true) {
                val targetTime = startTime + (tickCount * 10L)
                val now = System.currentTimeMillis()
                val sleepTime = targetTime - now
                if (sleepTime > 0) {
                    delay(sleepTime)
                } else {
                    yield()
                    // Re-align click count to avoid hot spin-loop catch up
                    val elapsed = System.currentTimeMillis() - startTime
                    tickCount = elapsed / 10L
                }

                val relativeTime = tickCount * 10L
                val luminanceValue = _lux.value

                // Add to internal synchronized list
                synchronized(recordedPointsList) {
                    recordedPointsList.add(Pair(relativeTime, luminanceValue))
                }

                // Update stats
                if (luminanceValue < _currentMin.value) _currentMin.value = luminanceValue
                if (luminanceValue > _currentMax.value) _currentMax.value = luminanceValue
                currentSum += luminanceValue
                currentCount++
                _currentAvg.value = currentSum / currentCount

                val loopNow = System.currentTimeMillis()

                // Push to Firebase for live web monitoring at an optimized interval (250ms)
                // Keeping this throttled prevents the device transmitter from overheating and draining battery.
                if (_deviceRole.value == DeviceRole.PRIMARY_SENSOR && loopNow - lastFirebasePushTime >= 250) {
                    lastFirebasePushTime = loopNow
                    try {
                        firebaseDb.child("currentLux").setValue(luminanceValue)
                        val datapoint = mapOf(
                            "timestamp" to loopNow,
                            "lux" to luminanceValue
                        )
                        firebaseDb.child("liveData").push().setValue(datapoint)
                    } catch (e: Exception) { e.printStackTrace() }
                }

                // Rate limit StateFlow updates for Compose smoothness (100ms limit, capped at last 50 seconds to prevent huge linear copy allocations)
                val currentNow = System.currentTimeMillis()
                if (currentNow - lastUiUpdate >= 100) {
                    lastUiUpdate = currentNow
                    val pointsCopy = synchronized(recordedPointsList) {
                        val size = recordedPointsList.size
                        if (size > 5000) {
                            ArrayList(recordedPointsList.subList(size - 5000, size))
                        } else {
                            ArrayList(recordedPointsList)
                        }
                    }
                    _recordedPoints.value = pointsCopy
                }

                tickCount++
            }
        }
    }

    private fun stopSamplingTask(flush: Boolean) {
        samplingJob?.cancel()
        samplingJob = null
        if (flush) {
            val pointsCopy = synchronized(recordedPointsList) {
                ArrayList(recordedPointsList)
            }
            _recordedPoints.value = pointsCopy
        }
    }

    fun setChartColorHex(hex: String) {
        _chartColorHex.value = hex
    }

    fun setShowGridLines(show: Boolean) {
        _showGridLines.value = show
    }

    fun setChartMaxDuration(seconds: Int) {
        _chartMaxDurationSeconds.value = seconds
    }

    fun updateFromCaptureResult(result: CaptureResult) {
        val exposureTimeNs = result.get(CaptureResult.SENSOR_EXPOSURE_TIME) ?: return
        val isoOpt = result.get(CaptureResult.SENSOR_SENSITIVITY) ?: return
        val aperture = result.get(CaptureResult.LENS_APERTURE) ?: 1.8f // fallback aperture f/1.8

        val exposureTimeSec = exposureTimeNs / 1_000_000_000.0
        
        // EV calculation: EV = log2(N^2 / t) where N=aperture, t=exposure seconds
        val evBase = log2((aperture * aperture) / exposureTimeSec)
        val ev100 = evBase - log2(isoOpt / 100.0)
        
        val now = System.currentTimeMillis()

        // Rate limit UI updates to 150ms to prevent main thread/composer starvation
        if (now - lastUiUpdateTime >= 150) {
            lastUiUpdateTime = now
            _iso.value = isoOpt
            _exposureTime.value = (exposureTimeSec * 1000).toFloat() // ms
            _ev.value = ev100.toFloat()
            _aperture.value = aperture
        }
    }

    fun startRecording() {
        if (!_isRecording.value) {
            updateSessionName() // Ensure naming is synced to latest state
            val now = System.currentTimeMillis()
            _recordingStartTime.value = now
            _isRecording.value = true
            _recordedPoints.value = emptyList()
            recordedPointsList.clear()
            _currentMin.value = Float.MAX_VALUE
            _currentMax.value = 0f
            _currentAvg.value = 0f
            currentSum = 0f
            currentCount = 0
            lastSampleTime = 0L
            startSamplingTask()

            try {
                firebaseDb.child("commands").child("isRecording").setValue(true)
                // Clear liveData to start fresh on the web dashboard
                firebaseDb.child("liveData").removeValue()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun pauseRecording() {
        _isRecording.value = false
        stopSamplingTask(true)
        try {
            firebaseDb.child("commands").child("isRecording").setValue(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resumeRecording() {
        if (!_isRecording.value) {
            _isRecording.value = true
            // adjust start time to keep relative offsets linear
            lastSampleTime = 0L
            startSamplingTask()
            try {
                firebaseDb.child("commands").child("isRecording").setValue(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearRecording() {
        _isRecording.value = false
        stopSamplingTask(false)
        _recordedPoints.value = emptyList()
        recordedPointsList.clear()
        _currentMin.value = Float.MAX_VALUE
        _currentMax.value = 0f
        _currentAvg.value = 0f
        currentSum = 0f
        currentCount = 0
        try {
            firebaseDb.child("commands").child("isRecording").setValue(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveRecording(title: String) {
        stopSamplingTask(true)
        val points = synchronized(recordedPointsList) { ArrayList(recordedPointsList) }
        if (points.isEmpty()) {
            _isRecording.value = false
            try {
                firebaseDb.child("commands").child("isRecording").setValue(false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return
        }

        val base = _selectedExperimentType.value.ifEmpty { "Session" }
        val suffix = _selectedTrial.value
        val stateName = if (suffix.isNotEmpty()) "$base - $suffix" else base

        val finalTitle = if (title == "Remote Session") stateName else title.trim().ifEmpty {
            stateName.ifEmpty {
                "Session - " + java.text.SimpleDateFormat(
                    "MMM dd, HH:mm",
                    java.util.Locale.getDefault()
                ).format(java.util.Date())
            }
        }
        val finalMin = if (_currentMin.value == Float.MAX_VALUE) 0f else _currentMin.value
        val duration = if (points.isNotEmpty()) points.last().first else 0L

        viewModelScope.launch {
            val session = MeasurementSession(
                title = finalTitle,
                timestamp = System.currentTimeMillis(),
                durationMs = duration,
                avgLux = _currentAvg.value,
                maxLux = _currentMax.value,
                minLux = finalMin
            )

            val datapoints = points.map { 
                MeasurementDatapoint(
                    sessionId = 0L, // will be overwritten in repository
                    timeMillis = it.first,
                    lux = it.second
                )
            }

            val sessionId = repository.insertSessionWithPoints(session, datapoints)
            try {
                val firebasePoints = datapoints.map { mapOf("timeMillis" to it.timeMillis, "lux" to it.lux) }
                val firebaseSession = mapOf(
                    "id" to sessionId,
                    "title" to session.title,
                    "timestamp" to session.timestamp,
                    "durationMs" to session.durationMs,
                    "avgLux" to session.avgLux,
                    "maxLux" to session.maxLux,
                    "minLux" to session.minLux,
                    "datapoints" to firebasePoints
                )
                firebaseDb.child("sessions").child("session_$sessionId").setValue(firebaseSession)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            clearRecording()
        }
    }

    suspend fun fetchSessionDatapointsFromFirebase(sessionId: Long): List<MeasurementDatapoint> {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            firebaseDb.child("sessions").child("session_$sessionId").child("datapoints")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val list = mutableListOf<MeasurementDatapoint>()
                        for (child in snapshot.children) {
                            val timeMillis = child.child("timeMillis").getValue(Long::class.java) ?: 0L
                            val luxVal = child.child("lux").value?.let {
                                if (it is Number) it.toFloat() else 0f
                            } ?: 0f
                            list.add(
                                MeasurementDatapoint(
                                    sessionId = sessionId,
                                    timeMillis = timeMillis,
                                    lux = luxVal
                                )
                            )
                        }
                        list.sortBy { it.timeMillis }
                        continuation.resume(list)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(emptyList())
                    }
                })
        }
    }

    fun loadSessionDetails(session: MeasurementSession) {
        _viewingSession.value = session
        _viewingSessionPoints.value = emptyList()
        viewModelScope.launch {
            val points = fetchSessionDatapointsFromFirebase(session.id)
            _viewingSessionPoints.value = points
        }
    }

    fun refreshFirebaseSessions() {
        _isRefreshing.value = true
        firebaseDb.child("sessions").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<MeasurementSession>()
                for (child in snapshot.children) {
                    try {
                        val id = child.child("id").getValue(Long::class.java)
                            ?: child.key?.removePrefix("session_")?.toLongOrNull() ?: continue
                        val title = child.child("title").getValue(String::class.java) ?: "Remote Session"
                        val timestamp = child.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()
                        val durationMs = child.child("durationMs").getValue(Long::class.java) ?: 0L
                        val avgLux = child.child("avgLux").value?.let {
                            if (it is Number) it.toFloat() else 0f
                        } ?: 0f
                        val maxLux = child.child("maxLux").value?.let {
                            if (it is Number) it.toFloat() else 0f
                        } ?: 0f
                        val minLux = child.child("minLux").value?.let {
                            if (it is Number) it.toFloat() else 0f
                        } ?: 0f
                        
                        list.add(
                            MeasurementSession(
                                id = id,
                                title = title,
                                timestamp = timestamp,
                                durationMs = durationMs,
                                avgLux = avgLux,
                                maxLux = maxLux,
                                minLux = minLux,
                                deletedAt = null
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                list.sortByDescending { it.timestamp }
                _allSessionsFirebase.value = list
                _isRefreshing.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _isRefreshing.value = false
            }
        })
    }

    fun exportSelectedSessionsToExcel(selectedSessionIds: Set<Long>, context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sb = java.lang.StringBuilder()
                sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
                sb.append("<?mso-application progid=\"Excel.Sheet\"?>\n")
                sb.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
                sb.append(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n")
                sb.append(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"\n")
                sb.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
                sb.append(" xmlns:html=\"http://www.w3.org/TR/REC-html40\">\n")
                
                sb.append(" <Styles>\n")
                sb.append("  <Style ss:ID=\"Default\" ss:Name=\"Normal\">\n")
                sb.append("   <Alignment ss:Vertical=\"Bottom\"/>\n")
                sb.append("   <Borders/>\n")
                sb.append("   <Font ss:FontName=\"Calibri\" x:CharSet=\"1\" ss:Size=\"11\" ss:Color=\"#000000\"/>\n")
                sb.append("   <Interior/>\n")
                sb.append("   <NumberFormat/>\n")
                sb.append("   <Protection/>\n")
                sb.append("  </Style>\n")
                sb.append("  <Style ss:ID=\"sHeader\">\n")
                sb.append("   <Font ss:FontName=\"Calibri\" ss:Bold=\"1\"/>\n")
                sb.append("   <Interior ss:Color=\"#E0E0E0\" ss:Pattern=\"Solid\"/>\n")
                sb.append("  </Style>\n")
                sb.append("  <Style ss:ID=\"sTime\">\n")
                sb.append("   <NumberFormat ss:Format=\"0.00\"/>\n")
                sb.append("  </Style>\n")
                sb.append("  <Style ss:ID=\"sValue\">\n")
                sb.append("   <NumberFormat ss:Format=\"0.000\"/>\n")
                sb.append("  </Style>\n")
                sb.append(" </Styles>\n")
                
                sb.append(" <Worksheet ss:Name=\"LightMeter Data\">\n")
                sb.append("  <Table>\n")
                
                // Headers Row
                sb.append("   <Row ss:Height=\"20\">\n")
                sb.append("    <Cell ss:StyleID=\"sHeader\"><Data ss:Type=\"String\">Session Title</Data></Cell>\n")
                sb.append("    <Cell ss:StyleID=\"sHeader\"><Data ss:Type=\"String\">Date Time</Data></Cell>\n")
                sb.append("    <Cell ss:StyleID=\"sHeader\"><Data ss:Type=\"String\">t</Data></Cell>\n")
                sb.append("    <Cell ss:StyleID=\"sHeader\"><Data ss:Type=\"String\">Lv</Data></Cell>\n")
                sb.append("   </Row>\n")
                
                for (id in selectedSessionIds) {
                    val session = allSessions.value.find { it.id == id } ?: continue
                    val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(session.timestamp))
                    
                    // Fetch directly from Firebase to guarantee full synchronization with cloud
                    val points = fetchSessionDatapointsFromFirebase(id)
                    for (p in points) {
                        val elapsed = (p.timeMillis / 1000.0)
                        val cleanTitleStr = session.title.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")
                        
                        sb.append("   <Row>\n")
                        sb.append("    <Cell><Data ss:Type=\"String\">$cleanTitleStr</Data></Cell>\n")
                        sb.append("    <Cell><Data ss:Type=\"String\">$dateStr</Data></Cell>\n")
                        sb.append("    <Cell ss:StyleID=\"sTime\"><Data ss:Type=\"Number\">${String.format(java.util.Locale.US, "%.3f", elapsed)}</Data></Cell>\n")
                        sb.append("    <Cell ss:StyleID=\"sValue\"><Data ss:Type=\"Number\">${String.format(java.util.Locale.US, "%.6f", p.lux)}</Data></Cell>\n")
                        sb.append("   </Row>\n")
                    }
                }
                
                sb.append("  </Table>\n")
                sb.append(" </Worksheet>\n")
                sb.append("</Workbook>\n")

                val finalTitle = if (selectedSessionIds.size == 1) {
                    val singleSession = allSessions.value.find { it.id == selectedSessionIds.first() }
                    singleSession?.title ?: "telemetry"
                } else {
                    "telemetry_multiple"
                }
                
                val cleanTitle = finalTitle.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()
                val filename = "$cleanTitle.xls"
                
                var fileSaved = false
                var savedPathMessage = ""

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val contentValues = android.content.ContentValues().apply {
                        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/vnd.ms-excel")
                        put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Download/LightMeter")
                    }
                    val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(sb.toString().toByteArray(Charsets.UTF_8))
                        }
                        fileSaved = true
                        savedPathMessage = "Downloads/LightMeter/$filename"
                    }
                }
                
                if (!fileSaved) {
                    val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                    val outDir = java.io.File(downloadsDir, "LightMeter")
                    if (!outDir.exists()) {
                        outDir.mkdirs()
                    }
                    val exportFile = java.io.File(outDir, filename)
                    exportFile.writeText(sb.toString())
                    fileSaved = true
                    savedPathMessage = exportFile.absolutePath
                }

                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    if (fileSaved) {
                        android.widget.Toast.makeText(context, "Downloaded successfully to: $savedPathMessage", android.widget.Toast.LENGTH_LONG).show()
                    } else {
                        android.widget.Toast.makeText(context, "Failed to download Excel", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Download failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun closeSessionDetails() {
        _viewingSession.value = null
        _viewingSessionPoints.value = emptyList()
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            try {
                firebaseDb.child("sessions").child("session_$sessionId").removeValue()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (_viewingSession.value?.id == sessionId) {
                closeSessionDetails()
            }
        }
    }

    fun restoreSession(sessionId: Long) {
        viewModelScope.launch {
            repository.restoreSession(sessionId)
            try {
                val session = repository.getSessionById(sessionId)
                if (session != null) {
                    val datapoints = repository.getDatapointsForSessionSync(sessionId)
                    val firebasePoints = datapoints.map { mapOf("timeMillis" to it.timeMillis, "lux" to it.lux) }
                    val firebaseSession = mapOf(
                        "id" to sessionId,
                        "title" to session.title,
                        "timestamp" to session.timestamp,
                        "durationMs" to session.durationMs,
                        "avgLux" to session.avgLux,
                        "maxLux" to session.maxLux,
                        "minLux" to session.minLux,
                        "datapoints" to firebasePoints
                    )
                    firebaseDb.child("sessions").child("session_$sessionId").setValue(firebaseSession)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteSessionPermanently(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSessionPermanently(sessionId)
            try {
                firebaseDb.child("sessions").child("session_$sessionId").removeValue()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (_viewingSession.value?.id == sessionId) {
                closeSessionDetails()
            }
        }
    }

    fun deleteAllPermanently() {
        viewModelScope.launch {
            repository.deleteAllPermanently()
            val currentViewing = _viewingSession.value
            if (currentViewing != null && currentViewing.deletedAt != null) {
                closeSessionDetails()
            }
        }
    }

    fun updateSessionTitle(sessionId: Long, newTitle: String) {
        viewModelScope.launch {
            repository.updateSessionTitle(sessionId, newTitle)
            try {
                firebaseDb.child("sessions").child("session_$sessionId").child("title").setValue(newTitle)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // If we are currently viewing this session, update the state too
            val currentViewing = _viewingSession.value
            if (currentViewing != null && currentViewing.id == sessionId) {
                _viewingSession.value = currentViewing.copy(title = newTitle)
            }
        }
    }

    fun recheckFirebaseConnection() {
        try {
            firebaseDb.database.goOffline()
            firebaseDb.database.goOnline()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetFirebaseData() {
        try {
            firebaseDb.child("liveData").removeValue()
            firebaseDb.child("currentLux").setValue(0f)
            firebaseDb.child("commands").child("isRecording").setValue(false)
            firebaseDb.child("commands").child("sessionName").setValue("Remote Session")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {
        initializeFirebaseAndSync()
    }
}
