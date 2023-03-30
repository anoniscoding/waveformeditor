package com.anu.waveformeditor

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class WaveformView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val waveformPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLUE
        strokeWidth = 0f
        style = Paint.Style.FILL_AND_STROKE
        strokeJoin = Paint.Join.ROUND
    }
    private val selectionBarPaint = Paint().apply {
        isAntiAlias = true
        color = Color.RED
        style = Paint.Style.FILL
        alpha = 128
    }

    private var waveformData = emptyList<Pair<Float, Float>>()

    //the width of the waveform in pixels
    private var waveformWidth = 0f
    //the height of the waveform in pixels.
    private var waveformHeight = 0f
    //the x-coordinate of the left edge of the waveform.
    private var waveformStartX = 0f
    //the y-coordinate of the top edge of the waveform.
    private var waveformStartY = 0f
    //the x-coordinate of the right edge of the waveform.
    private var waveformEndX = 0f
    //the y-coordinate of the bottom edge of the waveform.
    private var waveformEndY = 0f
    //the distance between two adjacent points on the waveform.
    private var waveformStep = 0f
    //the x-coordinate of the left edge of the selected range.
    private var selectedRangeStart = 0f
    //the x-coordinate of the right edge of the selected range.
    private var selectedRangeEnd = 0f
    //the width of the selection bars.
    private val selectionBarWidth = 10f

    private val startBarRect = RectF()
    private val endBarRect = RectF()
    private val waveformPath = Path()
    private val selectedPath = Path()
    private  val startBarTouchRect = RectF()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initView()
    }

    private fun initView() {
        waveformStartX = paddingLeft.toFloat()
        waveformEndX = (width - paddingRight).toFloat()
        waveformWidth = waveformEndX - waveformStartX

        val availableHeight = height - paddingTop - paddingBottom
        //sets the height of the waveform to 90% of the available height,
        //divided by two to ensure that the waveform is centered vertically within the view.
        waveformHeight = 0.9f * availableHeight / 2f

        waveformStartY = (height / 2f - waveformHeight)
        waveformEndY = (height / 2f + waveformHeight)

        //the distance between two adjacent data points on the x-axis, 
        // which is the width of the view divided by the number of data points.
        waveformStep = (waveformWidth) / waveformData.size.toFloat()

        setSelectedRange(waveformStartX, waveformEndX)
    }

    fun setWaveformData(waveformData: List<Pair<Float, Float>>) {
        if (this.waveformData == waveformData) {
            return
        }

        this.waveformData = waveformData
        initView()
        invalidate()
    }

    private fun setSelectedRange(startX: Float, endX: Float) {
        selectedRangeStart = startX
        selectedRangeEnd = endX
    }

    fun updateSelectedRange(normalizedStartX: Float, normalizedEndX: Float) {
        //Ensure that the view has been measured and laid out before updating selected range
        post {
            val startXInPixel = normalizedStartX * waveformWidth
            val endXInPixel = normalizedEndX * waveformWidth
            setSelectedRange(startXInPixel, endXInPixel)
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        if (waveformData.isEmpty()) {
            return
        }

        drawWaveForm(canvas)

        if (selectedRangeStart < selectedRangeEnd) {
            drawSelectionBars(canvas)
        }
    }

    private fun drawWaveForm(canvas: Canvas) {
        waveformPath.reset()

        var currentX = waveformStartX

        val midPointOfViewHeight = height / 2f
        // Move to the first point
        val firstMax = waveformData.first().second
        //The y-coordinate at which the path should start drawing for the first point of the waveform
        val firstY = midPointOfViewHeight - firstMax * waveformHeight
        waveformPath.moveTo(currentX, firstY)

        // Draw a line graph for the minimum values
        waveformData.forEach { (min, _) ->
            val minY = midPointOfViewHeight - min * waveformHeight
            waveformPath.lineTo(currentX, minY)
            currentX += waveformStep
        }

        // Draw a line graph for the maximum values
        currentX = waveformStartX
        //the x-coordinate of the last point in the waveform
        val lastX = currentX + waveformStep * (waveformData.size - 1)
        var lastMaxY = firstY
        waveformData.forEach { (_, max) ->
            val maxY = midPointOfViewHeight - max * waveformHeight
            waveformPath.lineTo(currentX, maxY)
            currentX += waveformStep
            lastMaxY = maxY
        }

        val lastMinY = midPointOfViewHeight - waveformData.last().first * waveformHeight

        // Ensure that the waveform is fully enclosed as a shape
        //Draw a line from the last point in the waveform to the last Y coordinate of the maximum values.
        waveformPath.lineTo(lastX, lastMaxY)
        //Draw a line from the last point in the waveform to the last Y coordinate of the minimum values.
        waveformPath.lineTo(lastX, lastMinY)
        //Draw a line from the last point in the waveform to the first Y coordinate of the maximum values.
        waveformPath.lineTo(waveformStartX, firstY)
        //Close the shape by connecting the last point in the path to the first point
        waveformPath.close()

        // Draw the waveform
        waveformPaint.color = Color.GRAY
        canvas.drawPath(waveformPath, waveformPaint)

        //Draw the selected part of the waveform
        waveformPaint.color = Color.BLUE
        selectedPath.reset()
        //Add a rectangle to the selected path that covers the selected region of the waveform.
        selectedPath.addRect(RectF(selectedRangeStart, waveformStartY, selectedRangeEnd, waveformEndY), Path.Direction.CW)
        //Combine the selected path with the waveform path using the intersection operation,
        // so that only the part of the waveform that falls within the selected region is drawn in blue.
        selectedPath.op(waveformPath, Path.Op.INTERSECT)
        canvas.drawPath(selectedPath, waveformPaint)
    }

    private fun drawSelectionBars(canvas: Canvas) {
        val startX = selectedRangeStart.coerceIn(waveformStartX, waveformEndX)
        val endX = selectedRangeEnd.coerceIn(waveformStartX, waveformEndX)

        // Update the positions of the start and end bar RectFs
        startBarRect.apply {
            left = max(startX - selectionBarWidth / 2f, waveformStartX)
            right = min(startX + selectionBarWidth / 2f, waveformEndX)
            top = waveformStartY
            bottom = waveformEndY
        }

        endBarRect.apply {
            left = max(endX - selectionBarWidth / 2f, waveformStartX)
            right = min(endX + selectionBarWidth / 2f, waveformEndX)
            top = waveformStartY
            bottom = waveformEndY
        }

        // Draw the selection bars
        canvas.drawRect(startBarRect, selectionBarPaint)
        canvas.drawRect(endBarRect, selectionBarPaint)
    }

    //Tracks x coordinate of the previous touch point
    private var prevX: Float = 0f
    private var isMovingStartBar = false

    var onSelectedRangeChanged: ((normalizedStartX: Float, normalizedEndX: Float) -> Unit)? = null
    private val selectionBarTouchPadding = 30f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                // Create RectF for the start bar with added padding
                startBarTouchRect.apply {
                    left = startBarRect.left - selectionBarTouchPadding
                    top = startBarRect.top - selectionBarTouchPadding
                    right = startBarRect.right + selectionBarTouchPadding
                    bottom = startBarRect.bottom + selectionBarTouchPadding
                }
                //calculate the distance from the touch point to the start and end of the selection bar
                val startXDist = abs(selectedRangeStart - event.x)
                val endXDist = abs(selectedRangeEnd - event.x)
                // Check if the touch event is within the start bar touch target
                isMovingStartBar = startBarTouchRect.contains(event.x, event.y) || startXDist <= endXDist
                prevX = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - prevX
                if (isMovingStartBar) {
                    selectedRangeStart = (selectedRangeStart + dx).coerceIn(waveformStartX, selectedRangeEnd - selectionBarWidth)
                } else  {
                    selectedRangeEnd = (selectedRangeEnd + dx).coerceIn(selectedRangeStart + selectionBarWidth, waveformEndX)
                }
                prevX = event.x
            }
            MotionEvent.ACTION_UP -> {
                isMovingStartBar = false
                //Normalization is done to make the values independent of the view's dimensions, allowing them
                // to be used in different contexts or screen sizes. The resulting values are between 0 and 1,
                // where 0 represents the start of the waveform, and 1 represents the end.
                val normalizedStartX = selectedRangeStart / waveformWidth
                val normalizedEndX = selectedRangeEnd / waveformWidth
                onSelectedRangeChanged?.invoke(normalizedStartX, normalizedEndX)
            }
        }

        invalidate()
        return true
    }


    fun getAllPairsInSelectedRange(): List<Pair<Float, Float>> {
        return waveformData.filterIndexed { index, _ ->
            val x = index.toFloat() * waveformStep + waveformStartX
            x in selectedRangeStart..selectedRangeEnd
        }
    }

}