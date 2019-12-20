package com.liang.widget

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import com.liang.cardview.R
import kotlin.math.cos

class RoundRectDrawableWithShadow(
    val resources: Resources,
    val mRadius: Float,
    val mShadowColor: Int,
    val mElevation: Int
) :
    Drawable() {

    private val cos45 = cos(Math.toRadians(25.0))
    private val shadowMultiplier = 1.5f

    private val mPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG) }
    private val mCornerShadowPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
            style = Paint.Style.FILL
        }
    }
    private val mEdgeShadowPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
            style = Paint.Style.FILL
            isAntiAlias = false
        }
    }

    // actual value set by developer
    private var mRawShadowSize = 0f
    private var mDirty = true
    private val mAddPaddingForCorners = true

    // multiplied value to account for shadow offset
    private var mShadowSize = 0f

    // extra shadow to avoid gaps between card and shadow
    private val mInsetShadow = resources.getDimensionPixelSize(R.dimen.cardview_compat_inset_shadow)
    /**
     * If shadow size is set to a value above max shadow, we print a warning
     */
    private var mPrintedShadowClipWarning = false

    private val mCornerShadowPath by lazy { Path() }

    // actual value set by developer
    private var mRawMaxShadowSize = 0f

    private val mCornerRadius = (mRadius + .5f).toInt()

    private val mBounds = Rect()
    private var mBackground: ColorStateList = ColorStateList.valueOf(Color.TRANSPARENT)

    private val mShadowStartColor = resources.getColor(R.color.cardview_shadow_start_color)
    private val mShadowEndColor = resources.getColor(R.color.cardview_shadow_end_color)

    init {
        setShadowSize(mElevation.toFloat(), mElevation.toFloat())
    }

    private fun setBackground(color: ColorStateList?) {
        mBackground = color ?: ColorStateList.valueOf(Color.TRANSPARENT)
        mPaint.color = mBackground.getColorForState(state, mBackground.defaultColor)
    }

    /**
     * Casts the value to an even integer.
     */
    private fun toEven(value: Float): Int {
        val i = (value + .5f).toInt()
        return if (i % 2 == 1) {
            i - 1
        } else i
    }

    override fun draw(canvas: Canvas) {

        if (mDirty) {
            buildComponents(bounds)
            mDirty = false
        }

        canvas.translate(0f, mRawShadowSize / 2)
        drawShadow(canvas)
        canvas.translate(0f, -mRawShadowSize / 2)


        mPaint.color = mShadowColor
        mPaint.maskFilter = BlurMaskFilter(mElevation.toFloat(), BlurMaskFilter.Blur.OUTER)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(
                mBounds.left.toFloat() + mElevation,
                mBounds.top.toFloat() + mElevation,
                mBounds.right.toFloat() - mElevation,
                mBounds.bottom.toFloat() - mElevation, mRadius, mRadius, mPaint
            )
        }
    }

    private fun buildComponents(bounds: Rect) { // Card is offset SHADOW_MULTIPLIER * maxShadowSize to account for the shadow shift.
// We could have different top-bottom offsets to avoid extra gap above but in that case
// center aligning Views inside the CardView would be problematic.
        val verticalOffset =
            mRawMaxShadowSize * shadowMultiplier
        mBounds.set(
            (bounds.left + mRawMaxShadowSize).toInt(), (bounds.top + verticalOffset).toInt(),
            (bounds.right - mRawMaxShadowSize).toInt(), (bounds.bottom - verticalOffset).toInt()
        )
        buildShadowCorners()
    }

    private fun buildShadowCorners() {
        val innerBounds = RectF(
            (-mCornerRadius).toFloat(),
            (-mCornerRadius).toFloat(),
            mCornerRadius.toFloat(),
            mCornerRadius.toFloat()
        )
        val outerBounds = RectF(innerBounds)
        outerBounds.inset(-mShadowSize, -mShadowSize)
        mCornerShadowPath.reset()
        mCornerShadowPath.setFillType(Path.FillType.EVEN_ODD)
        mCornerShadowPath.moveTo((-mCornerRadius).toFloat(), 0f)
        mCornerShadowPath.rLineTo(-mShadowSize, 0f)
        // outer arc
        mCornerShadowPath.arcTo(outerBounds, 180f, 90f, false)
        // inner arc
        mCornerShadowPath.arcTo(innerBounds, 270f, -90f, false)
        mCornerShadowPath.close()
        val startRatio = mCornerRadius / (mCornerRadius + mShadowSize)
        mCornerShadowPaint.shader = RadialGradient(
            0f,
            0f,
            mCornerRadius + mShadowSize,
            intArrayOf(mShadowStartColor, mShadowStartColor, mShadowEndColor),
            floatArrayOf(0f, startRatio, 1f),
            Shader.TileMode.CLAMP
        )
        // we offset the content shadowSize/2 pixels up to make it more realistic.
// this is why edge shadow shader has some extra space
// When drawing bottom edge shadow, we use that extra space.
        mEdgeShadowPaint.shader = LinearGradient(
            0f,
            -mCornerRadius + mShadowSize,
            0f,
            -mCornerRadius - mShadowSize,
            intArrayOf(mShadowStartColor, mShadowStartColor, mShadowEndColor),
            floatArrayOf(0f, .5f, 1f),
            Shader.TileMode.CLAMP
        )
        mEdgeShadowPaint.isAntiAlias = false
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
        mCornerShadowPaint.alpha = alpha
        mEdgeShadowPaint.alpha = alpha
    }

    private fun setShadowSize(
        shadowSize: Float,
        maxShadowSize: Float
    ) {
        var shadowSize1 = shadowSize
        var maxShadowSize1 = maxShadowSize
        require(shadowSize1 >= 0f) {
            ("Invalid shadow size " + shadowSize1
                    + ". Must be >= 0")
        }
        require(maxShadowSize1 >= 0f) {
            ("Invalid max shadow size " + maxShadowSize1
                    + ". Must be >= 0")
        }
        shadowSize1 = toEven(shadowSize1).toFloat()
        maxShadowSize1 = toEven(maxShadowSize1).toFloat()
        if (shadowSize1 > maxShadowSize1) {
            shadowSize1 = maxShadowSize1
            if (!mPrintedShadowClipWarning) {
                mPrintedShadowClipWarning = true
            }
        }
        if (mRawShadowSize == shadowSize1 && mRawMaxShadowSize == maxShadowSize1) {
            return
        }
        mRawShadowSize = shadowSize1
        mRawMaxShadowSize = maxShadowSize1
        mShadowSize = (shadowSize1 * shadowMultiplier + mInsetShadow + .5f)
        mDirty = true
        invalidateSelf()
    }

//    override fun getPadding(padding: Rect): Boolean {
//        padding[20, 20, 20] = 20
//        return false
//    }

    private fun calculateVerticalPadding(
        maxShadowSize: Float, cornerRadius: Float,
        addPaddingForCorners: Boolean
    ): Float {
        return if (addPaddingForCorners) {
            (maxShadowSize * shadowMultiplier + (1 - cos45) * cornerRadius).toFloat()
        } else {
            maxShadowSize * shadowMultiplier
        }
    }

    private fun calculateHorizontalPadding(
        maxShadowSize: Float, cornerRadius: Float,
        addPaddingForCorners: Boolean
    ): Float {
        return if (addPaddingForCorners) {
            (maxShadowSize + (1 - cos45) * cornerRadius).toFloat()
        } else {
            maxShadowSize
        }
    }

    override fun onStateChange(state: IntArray?): Boolean {
        val newColor = mBackground.getColorForState(state, mBackground.defaultColor)
        if (mPaint.color == newColor) {
            return false
        }
        mPaint.color = newColor
        mDirty = true
        invalidateSelf()
        return true
    }

    override fun isStateful(): Boolean {
        return mBackground.isStateful || super.isStateful()
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mPaint.colorFilter = colorFilter
    }

    override fun onBoundsChange(bounds: Rect?) {
        Log.e(javaClass.simpleName, "bounds: $bounds")
        mBounds.set(bounds)
    }

    override fun getOutline(outline: Outline) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outline.setRoundRect(
                mBounds.left + mElevation,
                mBounds.top + mElevation,
                mBounds.right - mElevation,
                mBounds.bottom - mElevation, mRadius
            )
        }
    }

    private fun drawShadow(canvas: Canvas) {
        val edgeShadowTop = -mCornerRadius - mShadowSize
        val inset = mCornerRadius + mInsetShadow + mRawShadowSize / 2
        val drawHorizontalEdges: Boolean = mBounds.width() - 2 * inset > 0
        val drawVerticalEdges: Boolean = mBounds.height() - 2 * inset > 0
        // LT
        var saved = canvas.save()
        canvas.translate(mBounds.left + inset, mBounds.top + inset)
        //        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        if (drawHorizontalEdges) {
            canvas.drawRect(
                0f, edgeShadowTop,
                mBounds.width() - 2 * inset, (-mCornerRadius).toFloat(),
                mEdgeShadowPaint
            )
        }
        canvas.restoreToCount(saved)
        // RB
        saved = canvas.save()
        canvas.translate(mBounds.right - inset, mBounds.bottom - inset)
        canvas.rotate(180f)
        canvas.drawPath(mCornerShadowPath!!, mCornerShadowPaint)
        if (drawHorizontalEdges) {
            canvas.drawRect(
                0f, edgeShadowTop,
                mBounds.width() - 2 * inset, -mCornerRadius + mShadowSize,
                mEdgeShadowPaint
            )
        }
        canvas.restoreToCount(saved)
        // LB
        saved = canvas.save()
        canvas.translate(mBounds.left + inset, mBounds.bottom - inset)
        canvas.rotate(270f)
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint)
        if (drawVerticalEdges) {
            canvas.drawRect(
                0f, edgeShadowTop,
                mBounds.height() - 2 * inset, (-mCornerRadius).toFloat(), mEdgeShadowPaint
            )
        }
        canvas.restoreToCount(saved)
        // RT
        saved = canvas.save()
        canvas.translate(mBounds.right - inset, mBounds.top + inset)
        canvas.rotate(90f)
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint)
        if (drawVerticalEdges) {
            canvas.drawRect(
                0f, edgeShadowTop,
                mBounds.height() - 2 * inset, (-mCornerRadius).toFloat(), mEdgeShadowPaint
            )
        }
        canvas.restoreToCount(saved)
    }
}