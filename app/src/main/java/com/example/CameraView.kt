package com.example

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.util.Log
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.math.pow
import androidx.compose.ui.Alignment
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

private val LINEAR_TABLE = DoubleArray(256) { i ->
    val f = i / 255.0
    if (f <= 0.04045) f / 12.92 else Math.pow((f + 0.055) / 1.055, 2.4)
}

@androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
@Composable
fun CameraView(
    viewModel: LightMeterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val passepartout by viewModel.passepartout.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()

    // State to hold the processed frame bitmap to render on the Canvas
    var processedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    DisposableEffect(lifecycleOwner) {
        val analysisExecutor = Executors.newSingleThreadExecutor()
        var isDisposed = false
        val applicationContext = context.applicationContext

        var lastFrameTime = 0L
        var lastProcessedTime = 0L
        val frameDurations = java.util.ArrayList<Long>()
        var cachedPixels: IntArray? = null
        var bitmapA: Bitmap? = null
        var bitmapB: Bitmap? = null
        var currentBitmapIsA = true
        var activeImageAnalysis: ImageAnalysis? = null
        var cameraProviderFuture: com.google.common.util.concurrent.ListenableFuture<ProcessCameraProvider>? = null

        // Add a small delay to ensure AppOps state is updated after permission grant
        // and that the lifecycle is at least STARTED
        var job: kotlinx.coroutines.Job? = null
        
        val lifecycle = lifecycleOwner.lifecycle
        
        fun startCamera() {
            job?.cancel()
            job = (lifecycleOwner as? androidx.lifecycle.LifecycleOwner)?.lifecycleScope?.launch {
                // Stabilize delay: give the system time to propagate permission granting state to AppOps
                kotlinx.coroutines.delay(350)
                if (isDisposed || !lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) return@launch

                try {
                    val future = ProcessCameraProvider.getInstance(applicationContext)
                cameraProviderFuture = future
                future.addListener({
                    if (isDisposed || !lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) return@addListener
                    try {
                        val cameraProvider = future.get()

                    val imageAnalysisBuilder = ImageAnalysis.Builder()
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    
                    val interopExtender = Camera2Interop.Extender(imageAnalysisBuilder)
                    interopExtender.setSessionCaptureCallback(object : CameraCaptureSession.CaptureCallback() {
                        override fun onCaptureCompleted(
                            session: CameraCaptureSession,
                            request: CaptureRequest,
                            result: TotalCaptureResult
                        ) {
                            if (!isDisposed) {
                                runCatching { viewModel.updateFromCaptureResult(result) }
                            }
                        }
                    })

                    val imageAnalysis = imageAnalysisBuilder.build()
                    activeImageAnalysis = imageAnalysis

                    imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                        try {
                            if (isDisposed) {
                                imageProxy.close()
                                return@setAnalyzer
                            }
                            val currentTimeMillis = System.currentTimeMillis()
                            if (currentTimeMillis - lastProcessedTime < 66) {
                                imageProxy.close()
                                return@setAnalyzer
                            }
                            lastProcessedTime = currentTimeMillis

                            if (imageProxy.planes.isEmpty() || imageProxy.planes[0] == null) {
                                imageProxy.close()
                                return@setAnalyzer
                            }

                        val rotation = imageProxy.imageInfo.rotationDegrees
                        val width = imageProxy.width
                        val height = imageProxy.height

                        // Determine output dimensions based on rotation
                        val dW: Int
                        val dH: Int
                        if (rotation == 90 || rotation == 270) {
                            dW = 120
                            dH = 160
                        } else {
                            dW = 160
                            dH = 120
                        }

                        // Pre-allocate or reuse arrays
                        val totalSize = dW * dH
                        val outPixels = if (cachedPixels == null || cachedPixels!!.size != totalSize) {
                            val newArr = IntArray(totalSize)
                            cachedPixels = newArr
                            newArr
                        } else {
                            cachedPixels!!
                        }

                        val rawBuffer = imageProxy.planes[0].buffer
                        val rowStride = imageProxy.planes[0].rowStride
                        val pixelStride = imageProxy.planes[0].pixelStride
                        val bufferLimit = rawBuffer.limit()

                        val rect = passepartout
                        var sumLuma = 0.0
                        var countLuma = 0

                        // Perform combined rotation, downsampling, linearization, and color-marking in a single pass using integer arithmetic
                        for (dy in 0 until dH) {
                            val fy = dy.toFloat() / dH
                            for (dx in 0 until dW) {
                                val fx = dx.toFloat() / dW

                                // Integer mapping to original sensor coordinates to completely bypass float operations in the hot path
                                val srcX: Int
                                val srcY: Int
                                when (rotation) {
                                    90 -> {
                                        srcX = (dy * width) / dH
                                        srcY = ((dW - 1 - dx) * height) / dW
                                    }
                                    270 -> {
                                        srcX = ((dH - 1 - dy) * width) / dH
                                        srcY = (dx * height) / dW
                                    }
                                    180 -> {
                                        srcX = ((dW - 1 - dx) * width) / dW
                                        srcY = ((dH - 1 - dy) * height) / dH
                                    }
                                    else -> {
                                        srcX = (dx * width) / dW
                                        srcY = (dy * height) / dH
                                    }
                                }

                                val sX = srcX.coerceIn(0, width - 1)
                                val sY = srcY.coerceIn(0, height - 1)

                                val pixelOffset = sY * rowStride + sX * pixelStride
                                if (pixelOffset >= 0 && pixelOffset + 2 < bufferLimit) {
                                    val r = rawBuffer.get(pixelOffset).toInt() and 0xFF
                                    val g = rawBuffer.get(pixelOffset + 1).toInt() and 0xFF
                                    val b = rawBuffer.get(pixelOffset + 2).toInt() and 0xFF

                                    // Check if this pixel is inside the normalized selection rectangle
                                    if (fx >= rect.left && fx <= rect.right && fy >= rect.top && fy <= rect.bottom) {
                                        val rLin = LINEAR_TABLE[r]
                                        val gLin = LINEAR_TABLE[g]
                                        val bLin = LINEAR_TABLE[b]
                                        val pixelLuma = 0.2126 * rLin + 0.7152 * gLin + 0.0722 * bLin
                                        sumLuma += pixelLuma
                                        countLuma++
                                    }

                                    // Grayscale conversion value for display backdrop (non-linear fast approximation is perfectly fine)
                                    val gray = ((r * 2126 + g * 7152 + b * 722) / 10000).coerceIn(0, 255)

                                    // Overexposure or Underexposure highlights
                                    val outColor = when {
                                        r >= 245 || g >= 245 || b >= 245 -> 0xFFEF4444.toInt() // Overexposed red highlight
                                        r <= 12 && g <= 12 && b <= 12 -> 0xFF3B82F6.toInt()    // Underexposed blue highlight
                                        else -> 0xFF000000.toInt() or (gray shl 16) or (gray shl 8) or gray
                                    }
                                    outPixels[dy * dW + dx] = outColor
                                } else {
                                    outPixels[dy * dW + dx] = 0xFF000000.toInt()
                                }
                            }
                        }

                        // Compute average linearized luma and update ViewModel's relative luminance
                        if (countLuma > 0) {
                            val avgLuma = sumLuma / countLuma
                            
                            // Phyphox physical normalization factors
                            val currentIso = viewModel.iso.value.coerceAtLeast(1)
                            val currentExposureTimeSec = (viewModel.exposureTime.value / 1000.0).coerceAtLeast(1e-6)
                            val currentAperture = viewModel.aperture.value.coerceAtLeast(1.0f)

                            // Formula: L_rel = y_avg * (5.0 * N^2) / (3.0 * t * ISO)
                            val relativeLuminance = avgLuma * (5.0 * currentAperture * currentAperture) / (3.0 * currentExposureTimeSec * currentIso)
                            viewModel.updateLuminanceValue(relativeLuminance.toFloat())
                        }

                        // Double-buffered switching to avoid concurrent modification crashes and GC pressure
                        val targetBmp = if (currentBitmapIsA) {
                            val b = bitmapB
                            if (b == null || b.width != dW || b.height != dH) {
                                val newB = Bitmap.createBitmap(dW, dH, Bitmap.Config.ARGB_8888)
                                bitmapB = newB
                                newB
                            } else {
                                b
                            }
                        } else {
                            val a = bitmapA
                            if (a == null || a.width != dW || a.height != dH) {
                                val newA = Bitmap.createBitmap(dW, dH, Bitmap.Config.ARGB_8888)
                                bitmapA = newA
                                newA
                            } else {
                                a
                            }
                        }

                        targetBmp.setPixels(outPixels, 0, dW, 0, 0, dW, dH)
                        
                        // PERFORMANCE: Avoid .copy() which allocations a new bitmap every frame.
                        // Double buffering ensures we are not writing to the one currently being drawn.
                        processedImageBitmap = targetBmp.asImageBitmap()
                        currentBitmapIsA = !currentBitmapIsA

                        // FPS counting update
                        val now = System.currentTimeMillis()
                        if (lastFrameTime > 0) {
                            val duration = now - lastFrameTime
                            frameDurations.add(duration)
                            if (frameDurations.size > 20) {
                                frameDurations.removeAt(0)
                            }
                            val avgDuration = frameDurations.average()
                            if (avgDuration > 0) {
                                viewModel.updateFrameRate(1000f / avgDuration.toFloat())
                            }
                        }
                        lastFrameTime = now

                        imageProxy.close()
                        } catch (e: Exception) {
                            Log.e("CameraView", "Analyzer error", e)
                            try { imageProxy.close() } catch (ex: Exception) {}
                        }
                    }

                    val hasBackCamera = cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                    val cameraSelector = if (hasBackCamera) {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    } else {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("CameraView", "ImageAnalysis binding failed", e)
                }
            }, ContextCompat.getMainExecutor(applicationContext))
        } catch (e: Exception) {
            Log.e("CameraView", "ProcessCameraProvider.getInstance failed", e)
                }
            }
        }

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
        startCamera()
    }

    onDispose {
            isDisposed = true
            job?.cancel()
            try {
                activeImageAnalysis?.clearAnalyzer()
            } catch (e: Exception) {}
            try {
                cameraProviderFuture?.let { future ->
                    if (future.isDone) {
                        future.get().unbindAll()
                    }
                }
            } catch (e: Exception) {
                Log.e("CameraView", "Unbinding camera on dispose failed", e)
            }
            analysisExecutor.shutdown()
        }
    }

    // Interactive Drag and Resize logic
    var touchMode: TouchMode by remember { mutableStateOf(TouchMode.NONE) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isRecording) {
                    if (isRecording) return@pointerInput
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            val width = size.width
                            val height = size.height
                            if (width <= 0 || height <= 0) return@detectDragGestures

                            val rectPx = Rect(
                                left = passepartout.left * width,
                                top = passepartout.top * height,
                                right = passepartout.right * width,
                                bottom = passepartout.bottom * height
                            )

                            val touchTolerance = 60f // pixel radius for handle dragging

                            val dist = { ox: Float, oy: Float ->
                                kotlin.math.sqrt((startOffset.x - ox) * (startOffset.x - ox) + (startOffset.y - oy) * (startOffset.y - oy))
                            }

                            // Determine which corner handle was touched
                            touchMode = when {
                                dist(rectPx.left, rectPx.top) < touchTolerance -> TouchMode.RESIZE_TOP_LEFT
                                dist(rectPx.right, rectPx.top) < touchTolerance -> TouchMode.RESIZE_TOP_RIGHT
                                dist(rectPx.left, rectPx.bottom) < touchTolerance -> TouchMode.RESIZE_BOTTOM_LEFT
                                dist(rectPx.right, rectPx.bottom) < touchTolerance -> TouchMode.RESIZE_BOTTOM_RIGHT
                                startOffset.x in rectPx.left..rectPx.right && startOffset.y in rectPx.top..rectPx.bottom -> TouchMode.MOVE
                                else -> TouchMode.NONE
                            }
                        },
                        onDrag = { change: androidx.compose.ui.input.pointer.PointerInputChange, dragAmount: androidx.compose.ui.geometry.Offset ->
                            val width = size.width
                            val height = size.height
                            if (width <= 0 || height <= 0 || touchMode == TouchMode.NONE) return@detectDragGestures

                            val currentRect = passepartout
                            var left = currentRect.left * width
                            var top = currentRect.top * height
                            var right = currentRect.right * width
                            var bottom = currentRect.bottom * height

                            val minSize = 80f // Minimum rectangle size in pixels

                            when (touchMode) {
                                TouchMode.MOVE -> {
                                    val dx = dragAmount.x
                                    val dy = dragAmount.y
                                    val rectW = right - left
                                    val rectH = bottom - top

                                    left = (left + dx).coerceIn(0f, width - rectW)
                                    right = left + rectW
                                    top = (top + dy).coerceIn(0f, height - rectH)
                                    bottom = top + rectH
                                }
                                TouchMode.RESIZE_TOP_LEFT -> {
                                    left = (left + dragAmount.x).coerceIn(0f, right - minSize)
                                    top = (top + dragAmount.y).coerceIn(0f, bottom - minSize)
                                }
                                TouchMode.RESIZE_TOP_RIGHT -> {
                                    right = (right + dragAmount.x).coerceIn(left + minSize, width.toFloat())
                                    top = (top + dragAmount.y).coerceIn(0f, bottom - minSize)
                                }
                                TouchMode.RESIZE_BOTTOM_LEFT -> {
                                    left = (left + dragAmount.x).coerceIn(0f, right - minSize)
                                    bottom = (bottom + dragAmount.y).coerceIn(top + minSize, height.toFloat())
                                }
                                TouchMode.RESIZE_BOTTOM_RIGHT -> {
                                    right = (right + dragAmount.x).coerceIn(left + minSize, width.toFloat())
                                    bottom = (bottom + dragAmount.y).coerceIn(top + minSize, height.toFloat())
                                }
                                else -> {}
                            }

                            viewModel.updatePassepartout(
                                Rect(
                                    left = (left / width).coerceIn(0f, 1f),
                                    top = (top / height).coerceIn(0f, 1f),
                                    right = (right / width).coerceIn(0f, 1f),
                                    bottom = (bottom / height).coerceIn(0f, 1f)
                                )
                            )
                        },
                        onDragEnd = {
                            touchMode = TouchMode.NONE
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw camera processed source bitmap
            processedImageBitmap?.let { bitmap ->
                drawImage(
                    image = bitmap,
                    dstSize = androidx.compose.ui.unit.IntSize(width.toInt(), height.toInt())
                )
            }

            // 2. Draw selection box
            val rectPx = Rect(
                left = passepartout.left * width,
                top = passepartout.top * height,
                right = passepartout.right * width,
                bottom = passepartout.bottom * height
            )

            // Draw bounding rect border outline
            drawRect(
                color = if (isRecording) Color.Gray.copy(alpha = 0.5f) else Color.White,
                topLeft = Offset(rectPx.left, rectPx.top),
                size = Size(rectPx.right - rectPx.left, rectPx.bottom - rectPx.top),
                style = Stroke(width = 4f)
            )

            // Draw corner selection handles
            val handleRadius = 14f
            listOf(
                Offset(rectPx.left, rectPx.top),
                Offset(rectPx.right, rectPx.top),
                Offset(rectPx.left, rectPx.bottom),
                Offset(rectPx.right, rectPx.bottom)
            ).forEach { point ->
                drawCircle(
                    color = if (isRecording) Color.Gray.copy(alpha = 0.5f) else Color.White,
                    radius = handleRadius,
                    center = point
                )
                drawCircle(
                    color = Color.Transparent,
                    radius = handleRadius + 2f,
                    center = point,
                    style = Stroke(width = 2f)
                )
            }
        }

        // Reset camera box portion button
        IconButton(
            onClick = {
                viewModel.updatePassepartout(Rect(0.4f, 0.4f, 0.6f, 0.6f))
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset Selection",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

enum class TouchMode {
    NONE, MOVE, RESIZE_TOP_LEFT, RESIZE_TOP_RIGHT, RESIZE_BOTTOM_LEFT, RESIZE_BOTTOM_RIGHT
}
