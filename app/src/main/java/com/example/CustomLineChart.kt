package com.example

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import java.io.OutputStream

import androidx.compose.ui.graphics.toArgb

private fun <T> List<T>.binarySearchFirstIndex(predicate: (T) -> Boolean): Int {
    var low = 0
    var high = size - 1
    var result = size
    while (low <= high) {
        val mid = (low + high) ushr 1
        if (predicate(this[mid])) {
            result = mid
            high = mid - 1
        } else {
            low = mid + 1
        }
    }
    return result
}

@Composable
fun CustomLineChart(
    points: List<Pair<Long, Float>>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF2563EB),
    showGrid: Boolean = true,
    maxDurationSeconds: Int = 30,
    textColor: Color = Color.Gray,
    gridColor: Color = Color.LightGray
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (points.isEmpty()) return@Canvas

        val listToDraw = if (maxDurationSeconds > 0 && points.isNotEmpty()) {
            val lastTime = points.last().first
            val startTimeLimit = lastTime - (maxDurationSeconds * 1000)
            val startIndex = points.binarySearchFirstIndex { it.first >= startTimeLimit }
            if (startIndex < points.size) {
                points.subList(startIndex, points.size)
            } else {
                emptyList()
            }
        } else {
            points
        }

        if (listToDraw.isEmpty()) return@Canvas

        // Downsample listToDraw if it has too many elements to draw efficiently
        val maxSamples = 300
        val finalPoints = if (listToDraw.size > maxSamples) {
            val step = listToDraw.size.toDouble() / maxSamples
            val sampledList = ArrayList<Pair<Long, Float>>(maxSamples)
            for (i in 0 until maxSamples) {
                val idx = (i * step).toInt().coerceIn(0, listToDraw.size - 1)
                sampledList.add(listToDraw[idx])
            }
            if (sampledList.isNotEmpty() && sampledList.last().first != listToDraw.last().first) {
                sampledList[sampledList.size - 1] = listToDraw.last()
            }
            sampledList
        } else {
            listToDraw
        }

        val minTime = finalPoints.first().first
        val maxTime = finalPoints.last().first
        val timeRange = (maxTime - minTime).toFloat().coerceAtLeast(1f)

        val minVal = finalPoints.minOf { it.second }
        val maxVal = finalPoints.maxOf { it.second }.coerceAtLeast(minVal + 1f)
        val valRange = (maxVal - minVal)

        val paddingX = 80f
        val paddingY = 60f
        
        val width = size.width
        val height = size.height

        val chartWidth = width - (paddingX * 2)
        val chartHeight = height - (paddingY * 2)

        // Draw background grid lines
        if (showGrid) {
            val gridPaint = Paint().apply {
                color = gridColor.toArgb()
                strokeWidth = 1f
                style = Paint.Style.STROKE
                pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
            }
            val textPaint = Paint().apply {
                color = textColor.toArgb()
                textSize = 24f
                isAntiAlias = true
            }

            // Draw horizontal lines (values)
            val verticalGridCount = 4
            for (i in 0..verticalGridCount) {
                val y = paddingY + chartHeight - (i * chartHeight / verticalGridCount)
                val value = minVal + (i * valRange / verticalGridCount)
                
                drawContext.canvas.nativeCanvas.drawLine(
                    paddingX, y, width - paddingX, y, gridPaint
                )
                
                drawContext.canvas.nativeCanvas.drawText(
                    String.format("%.1f", value),
                    10f, y + 8f, textPaint
                )
            }

            // Draw vertical lines (time offsets)
            val horizontalGridCount = 3
            for (i in 0..horizontalGridCount) {
                val x = paddingX + (i * chartWidth / horizontalGridCount)
                val relativeTime = (minTime + (i * (maxTime - minTime) / horizontalGridCount) - minTime) / 1000.0
                
                drawContext.canvas.nativeCanvas.drawLine(
                    x, paddingY, x, paddingY + chartHeight, gridPaint
                )
                
                drawContext.canvas.nativeCanvas.drawText(
                    String.format("%.1fs", relativeTime),
                    x - 30f, height - 15f, textPaint
                )
            }
        }

        // Plot the points path
        val path = Path()
        finalPoints.forEachIndexed { index, pair ->
            val x = paddingX + ((pair.first - minTime) / timeRange) * chartWidth
            val y = paddingY + chartHeight - ((pair.second - minVal) / valRange) * chartHeight

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 6f)
        )
    }
}

/**
 * Renders a session chart to a Bitmap and saves it to the Downloads gallery
 */
fun saveChartToDownloadsGallery(
    context: Context,
    points: List<Pair<Long, Float>>,
    sessionTitle: String,
    lineColorHex: String = "#2563EB",
    showGrid: Boolean = true
): String? {
    if (points.isEmpty()) {
        Toast.makeText(context, "No points to save in chart", Toast.LENGTH_SHORT).show()
        return null
    }

    try {
        val width = 1200
        val height = 800
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        // Clear canvas with white background
        canvas.drawColor(android.graphics.Color.WHITE)

        val paddingX = 100f
        val paddingY = 120f
        val chartWidth = width - (paddingX * 2)
        val chartHeight = height - (paddingY * 2)

        val minTime = points.first().first
        val maxTime = points.last().first
        val timeRange = (maxTime - minTime).toFloat().coerceAtLeast(1f)

        val minVal = points.minOf { it.second }
        val maxVal = points.maxOf { it.second }.coerceAtLeast(minVal + 1f)
        val valRange = (maxVal - minVal)

        // Write Header
        val titlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0F172A")
            textSize = 38f
            isAntiAlias = true
            isFakeBoldText = true
        }
        canvas.drawText(sessionTitle, paddingX, 60f, titlePaint)

        val subtitlePaint = Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 24f
            isAntiAlias = true
        }
        canvas.drawText("Light Meter Recording Session Details", paddingX, 95f, subtitlePaint)

        // Draw Grid
        if (showGrid) {
            val gridPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#E2E8F0")
                strokeWidth = 2f
            }
            val valTextPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#64748B")
                textSize = 22f
                isAntiAlias = true
            }

            // Horizontal Lines
            val verticalGridCount = 5
            for (i in 0..verticalGridCount) {
                val y = paddingY + chartHeight - (i * chartHeight / verticalGridCount)
                val value = minVal + (i * valRange / verticalGridCount)
                canvas.drawLine(paddingX, y, width - paddingX, y, gridPaint)
                canvas.drawText(String.format("%.1f lx", value), 20f, y + 8f, valTextPaint)
            }

            // Vertical Lines
            val horizontalGridCount = 4
            for (i in 0..horizontalGridCount) {
                val x = paddingX + (i * chartWidth / horizontalGridCount)
                val relativeTime = (minTime + (i * (maxTime - minTime) / horizontalGridCount) - minTime) / 1000.0
                canvas.drawLine(x, paddingY, x, paddingY + chartHeight, gridPaint)
                canvas.drawText(String.format("%.1fs", relativeTime), x - 40f, height - 50f, valTextPaint)
            }
        }

        // Draw line path
        val pathPaint = Paint().apply {
            color = android.graphics.Color.parseColor(lineColorHex)
            strokeWidth = 6f
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val path = android.graphics.Path()
        points.forEachIndexed { index, pair ->
            val x = paddingX + ((pair.first - minTime) / timeRange) * chartWidth
            val y = paddingY + chartHeight - ((pair.second - minVal) / valRange) * chartHeight

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        canvas.drawPath(path, pathPaint)

        // Save Bitmap into MediaStore
        val filename = "LightMeter_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/LightMeter")
            }
        }

        val resolver = context.contentResolver
        val uri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            val outputStream: OutputStream? = resolver.openOutputStream(uri)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
                Toast.makeText(context, "Chart downloaded to Gallery successfully!", Toast.LENGTH_LONG).show()
                return uri.toString()
            }
        }

        Toast.makeText(context, "Failed to save chart to storage", Toast.LENGTH_SHORT).show()
        return null
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error sharing chart: ${e.message}", Toast.LENGTH_SHORT).show()
        return null
    }
}
