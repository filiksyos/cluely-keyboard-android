package dev.cluely.keyboard.ui.ime

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.MeasureSpec

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var keyboardListener: ((String?, Int) -> Unit)? = null
    private var isFocused = true
    
    // Modern color scheme
    private val backgroundColor = Color.parseColor("#1E1E1E")
    private val keyBackgroundColor = Color.parseColor("#2D2D2D")
    private val keyPressedColor = Color.parseColor("#3A3A3A")
    private val specialKeyColor = Color.parseColor("#4A90E2")
    private val textColor = Color.WHITE
    private val specialTextColor = Color.WHITE
    
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 18f, context.resources.displayMetrics
        )
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    
    private val keyCornerRadius = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics
    )
    
    private var pressedKeyIndex: Int? = null

    private val keyMap = mapOf(
        // Row 1
        "q" to 0, "w" to 1, "e" to 2, "r" to 3, "t" to 4, "y" to 5, "u" to 6, "i" to 7, "o" to 8, "p" to 9,
        // Row 2
        "a" to 10, "s" to 11, "d" to 12, "f" to 13, "g" to 14, "h" to 15, "j" to 16, "k" to 17, "l" to 18,
        // Row 3
        "z" to 19, "x" to 20, "c" to 21, "v" to 22, "b" to 23, "n" to 24, "m" to 25,
        // Special keys
        "⌫" to 26, "📷" to 27
    )

    private val keyRects = mutableMapOf<Int, Rect>()
    private val keyLabels = listOf(
        // Row 1
        "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
        // Row 2
        "a", "s", "d", "f", "g", "h", "j", "k", "l",
        // Row 3
        "z", "x", "c", "v", "b", "n", "m",
        // Special keys
        "⌫", "📷"
    )

    fun setKeyboardListener(listener: (String?, Int) -> Unit) {
        keyboardListener = listener
    }

    fun setFocused(focused: Boolean) {
        isFocused = focused
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        
        // Calculate desired height (~250dp or ~30% of screen height, whichever is smaller)
        val screenHeight = context.resources.displayMetrics.heightPixels
        val desiredHeightDp = 250f
        val desiredHeightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, desiredHeightDp, context.resources.displayMetrics
        ).toInt()
        val maxHeight = (screenHeight * 0.3f).toInt()
        val preferredHeight = minOf(desiredHeightPx, maxHeight)
        
        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightMeasureSpec)
            MeasureSpec.AT_MOST -> minOf(preferredHeight, MeasureSpec.getSize(heightMeasureSpec))
            else -> preferredHeight
        }
        
        setMeasuredDimension(widthSize, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isFocused) return

        // Draw background
        canvas.drawColor(backgroundColor)

        val width = width.toFloat()
        val height = height.toFloat()
        
        // Calculate key dimensions with proper spacing
        val keyPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 6f, context.resources.displayMetrics
        )
        val rowPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics
        )
        
        // Define keyboard layout: 3 rows of letters + 1 row for special keys
        val numRows = 3
        val availableHeight = height - (rowPadding * (numRows + 1))
        val keyHeight = availableHeight / numRows
        
        keyRects.clear()

        // Draw keys row by row
        var keyIndex = 0
        
        // Row 1: q w e r t y u i o p
        val row1Keys = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
        drawKeyRow(canvas, row1Keys, 0, keyIndex, width, keyHeight, rowPadding, keyPadding)
        keyIndex += row1Keys.size
        
        // Row 2: a s d f g h j k l (with offset)
        val row2Keys = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
        val row2Offset = keyWidth(row1Keys.size, width, keyPadding) / 2f
        drawKeyRow(canvas, row2Keys, 1, keyIndex, width, keyHeight, rowPadding, keyPadding, row2Offset)
        keyIndex += row2Keys.size
        
        // Row 3: z x c v b n m ⌫ 📷
        val row3Keys = listOf("z", "x", "c", "v", "b", "n", "m", "⌫", "📷")
        val row3Offset = keyWidth(row2Keys.size, width, keyPadding) / 2f
        drawKeyRow(canvas, row3Keys, 2, keyIndex, width, keyHeight, rowPadding, keyPadding, row3Offset)
    }
    
    private fun keyWidth(numKeys: Int, totalWidth: Float, padding: Float): Float {
        val totalPadding = padding * (numKeys + 1)
        return (totalWidth - totalPadding) / numKeys
    }
    
    private fun drawKeyRow(
        canvas: Canvas,
        keys: List<String>,
        rowIndex: Int,
        startKeyIndex: Int,
        totalWidth: Float,
        keyHeight: Float,
        rowPadding: Float,
        keyPadding: Float,
        offset: Float = 0f
    ) {
        val numKeys = keys.size
        val keyWidth = keyWidth(numKeys, totalWidth, keyPadding)
        val rowTop = rowPadding + (rowIndex * (keyHeight + rowPadding))
        
        keys.forEachIndexed { colIndex, label ->
            val keyIndex = startKeyIndex + colIndex
            val left = offset + keyPadding + (colIndex * (keyWidth + keyPadding))
            val top = rowTop
            val right = left + keyWidth
            val bottom = top + keyHeight
            
            val rect = RectF(left, top, right, bottom)
            keyRects[keyIndex] = Rect(
                rect.left.toInt(),
                rect.top.toInt(),
                rect.right.toInt(),
                rect.bottom.toInt()
            )
            
            // Determine if this is a special key
            val isSpecial = label == "⌫" || label == "📷"
            val isPressed = pressedKeyIndex == keyIndex
            
            // Draw key background with rounded corners
            val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = when {
                    isPressed -> keyPressedColor
                    isSpecial -> specialKeyColor
                    else -> keyBackgroundColor
                }
            }
            
            canvas.drawRoundRect(rect, keyCornerRadius, keyCornerRadius, keyPaint)
            
            // Draw label
            val textPaint = Paint(paint).apply {
                color = if (isSpecial) specialTextColor else textColor
            }
            
            val textX = rect.centerX()
            val textY = rect.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2)
            canvas.drawText(label, textX, textY, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isFocused) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y

                for ((index, rect) in keyRects) {
                    if (rect.contains(x.toInt(), y.toInt())) {
                        pressedKeyIndex = index
                        invalidate()
                        return true
                    }
                }
                pressedKeyIndex = null
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val x = event.x
                val y = event.y
                
                pressedKeyIndex?.let { index ->
                    val rect = keyRects[index]
                    if (rect != null && rect.contains(x.toInt(), y.toInt())) {
                        val label = keyLabels.getOrNull(index) ?: ""
                        when (label) {
                            "⌫" -> keyboardListener?.invoke(null, KeyEvent.KEYCODE_DEL)
                            "📷" -> keyboardListener?.invoke(null, SCREENSHOT_KEY_CODE)
                            else -> keyboardListener?.invoke(label.lowercase(), 0)
                        }
                    }
                }
                pressedKeyIndex = null
                invalidate()
            }
        }

        return true
    }

    companion object {
        private const val SCREENSHOT_KEY_CODE = -9001
    }
}