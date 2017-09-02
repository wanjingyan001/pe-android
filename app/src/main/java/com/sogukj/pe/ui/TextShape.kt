package com.sogukj.ui

import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import java.util.*

/**
 * @author feifeiq
 */
class TextShape : ShapeDrawable {

    private val textPaint = Paint()
    private var text = ""
    private var color = Color.GRAY
    private val height = -1
    private val width = -1
    private val fontSize = -1

    constructor(text: String, color: Int = randomColor) {
        this.text = text.toUpperCase()
        this.color = color
        textPaint.color = Color.WHITE
        textPaint.isAntiAlias = true
        textPaint.style = Paint.Style.FILL
        textPaint.typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        textPaint.textAlign = Paint.Align.CENTER

        // drawable paint color
        val paint = paint
        paint.color = color

    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val r = bounds

        val count = canvas.save()
        canvas.translate(r.left.toFloat(), r.top.toFloat())

        // draw text
        val width = if (this.width < 0) r.width() else this.width
        val height = if (this.height < 0) r.height() else this.height
        val fontSize = if (this.fontSize < 0) Math.min(width, height) / 2 else this.fontSize
        textPaint.textSize = fontSize.toFloat()
        canvas.drawText(text, (width / 2).toFloat(), height / 2 - (textPaint.descent() + textPaint.ascent()) / 2, textPaint)

        canvas.restoreToCount(count)

    }


    override fun setAlpha(alpha: Int) {
        textPaint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        textPaint.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getIntrinsicWidth(): Int {
        return width
    }

    override fun getIntrinsicHeight(): Int {
        return height
    }

    companion object {
        private val SHADE_FACTOR = 0.9f
        private val RANDOM = Random(System.currentTimeMillis())
        private val COLOR_LIST = Arrays.asList(
                0xfff16364.toInt(),
                0xfff58559.toInt(),
                0xfff9a43e.toInt(),
                0xffe4c62e.toInt(),
                0xff67bf74.toInt(),
                0xff59a2be.toInt(),
                0xff2093cd.toInt(),
                0xffad62a7.toInt(),
                0xff805781.toInt()
        )
        val randomColor: Int
            get() = COLOR_LIST[RANDOM.nextInt(COLOR_LIST.size)]
    }


}