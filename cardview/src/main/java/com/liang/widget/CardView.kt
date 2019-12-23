package com.liang.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.liang.cardview.R


/**
 * TODO: document your custom view class.
 */
@SuppressLint("NewApi")
class CardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var useClipToOutline = true
    private var sight = 0

    private val contentPadding = Rect()
    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
    }

    private val roundRectDrawable by lazy {
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
        }
    }

    var cardCornerRadius: Float = 0F
        set(value) {
            field = value
            roundRectDrawable.cornerRadius = value
            postInvalidate()
        }

    var cardShadowColor: Int = 0
        set(value) {
            field = value
            postInvalidate()
        }

    var cardElevation: Int = 0
        set(value) {
            field = value
            postInvalidate()
        }

    var cardBackgroundColor: ColorStateList = ColorStateList.valueOf(Color.TRANSPARENT)
        set(value) {
            if (field != value) {
                field = value
                roundRectDrawable.color = value
            }
        }

    init {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.CardView, defStyle, 0
        )
        cardCornerRadius =
            a.getDimensionPixelSize(R.styleable.CardView_cardCornerRadius, 0).toFloat()
        cardElevation = a.getDimensionPixelSize(R.styleable.CardView_cardElevation, 5)

        cardShadowColor = a.getColor(R.styleable.CardView_cardShadowColor, Color.GRAY)

        if (a.hasValue(R.styleable.CardView_cardBackgroundColor)) {
            cardBackgroundColor = a.getColorStateList(R.styleable.CardView_cardBackgroundColor)
                ?: ColorStateList.valueOf(Color.TRANSPARENT)
        }

        sight = a.getInt(R.styleable.CardView_cardSight, 0)

        useClipToOutline = a.getBoolean(R.styleable.CardView_cardPreventCornerOverlap, true)

        val defaultPadding = a.getDimensionPixelSize(R.styleable.CardView_contentPadding, 0)
        contentPadding.left = a.getDimensionPixelSize(
            R.styleable.CardView_contentPaddingLeft,
            defaultPadding
        )
        contentPadding.top = a.getDimensionPixelSize(
            R.styleable.CardView_contentPaddingTop,
            defaultPadding
        )
        contentPadding.right = a.getDimensionPixelSize(
            R.styleable.CardView_contentPaddingRight,
            defaultPadding
        )
        contentPadding.bottom = a.getDimensionPixelSize(
            R.styleable.CardView_contentPaddingBottom,
            defaultPadding
        )

        a.recycle()

        super.setPadding(
            cardElevation,
            cardElevation,
            cardElevation,
            cardElevation
        )

    }

    override fun addView(
        child: View,
        index: Int,
        params: ViewGroup.LayoutParams?
    ) {
        addViewInternal(child, params)
    }

    private fun addViewInternal(child: View, params: ViewGroup.LayoutParams?) {

        if (childCount >= 1) {
            throw IllegalStateException("Can't add more than 1 views to a CardView")
        }

        val layoutParams = params ?: LayoutParams(-2, -1)
        if (layoutParams is LayoutParams) {
            layoutParams.gravity = Gravity.CENTER
        }

        when (sight) {
            2 -> addAlongChild(child, layoutParams)
            1 -> {
                if (child is RoundRectView) {
                    addRoundRectChild(child, layoutParams)
                } else {
                    throw IllegalStateException("Child of CardView must be wrapped with RoundRectView")
                }
            }
            0 -> {
                val roundRectView = RoundRectView(context).apply {
                    post {
                        addView(child, 0, params)
                    }
                }
                addRoundRectChild(roundRectView, layoutParams)
            }
        }
    }

    private fun addRoundRectChild(child: RoundRectView, layoutParams: ViewGroup.LayoutParams) {
        super.addView(child, 0, layoutParams)
        child.background = roundRectDrawable
        child.clipToOutline = useClipToOutline
        child.setContentPadding(contentPadding)
    }


    private fun addAlongChild(child: View, layoutParams: ViewGroup.LayoutParams) {
        if (useClipToOutline) {
            child.background = roundRectDrawable
            child.clipToOutline = useClipToOutline
        }
        super.addView(child, 0, layoutParams)
    }


    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
    }

    override fun setBackgroundDrawable(background: Drawable?) {
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (cardElevation > 0) {
            val child = getChildAt(0)
            child?.let {
                val rectF = RectF(
                    it.left.toFloat(),
                    it.top.toFloat(),
                    it.right.toFloat(),
                    it.bottom.toFloat()
                )
                paint.color = cardShadowColor
                paint.maskFilter =
                    BlurMaskFilter(cardElevation.toFloat(), BlurMaskFilter.Blur.OUTER)
                canvas.drawRoundRect(rectF, cardCornerRadius, cardCornerRadius, paint)
            }
        }
        super.dispatchDraw(canvas)
    }
}

class RoundRectView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    fun setContentPadding(contentPadding: Rect) {
        val params = layoutParams
        if (params is MarginLayoutParams) {
            params.leftMargin = contentPadding.left
            params.rightMargin = contentPadding.right
            params.topMargin = contentPadding.top
            params.bottomMargin = contentPadding.bottom
        }
        layoutParams = params
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (childCount >= 1) {
            throw IllegalStateException("Can't add more than 1 views to a RoundRectView")
        }
        super.addView(child, 0, params)
    }
}