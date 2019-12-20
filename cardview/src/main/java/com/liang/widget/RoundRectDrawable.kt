package com.liang.widget

import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class RoundRectDrawable(
    backgroundColor: ColorStateList?,
    private val radius: Float
) : Drawable() {

    var background: ColorStateList = ColorStateList.valueOf(Color.TRANSPARENT)
        set(value) {
            field = value
            paint.color = value.getColorForState(state, value.defaultColor)
            invalidateSelf()
        }

    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
    }

    private val rectBounds = Rect()

    init {
        background = backgroundColor ?: ColorStateList.valueOf(Color.TRANSPARENT)
    }

    override fun draw(canvas: Canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(
                rectBounds.left.toFloat(),
                rectBounds.top.toFloat(),
                rectBounds.right.toFloat(),
                rectBounds.bottom.toFloat(), radius, radius, paint
            )
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun onBoundsChange(bounds: Rect?) {
        rectBounds.set(bounds)
    }

    override fun getOutline(outline: Outline) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outline.setRoundRect(rectBounds, radius)
        }
    }
}