package com.liang.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.graphics.drawable.DrawableCompat
import com.liang.cardview.CardView
import com.liang.cardview.R


/**
 * TODO: document your custom view class.
 */
class CardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val colorBackgroundAttr = intArrayOf(android.R.attr.colorBackground)

    private val paint by lazy {
        Paint().apply {
            style = Paint.Style.FILL_AND_STROKE
            isAntiAlias = true
        }
    }

    private val backgroundDrawable by lazy {
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
        }
    }

    private val roundRectView = RoundRectView(context)

    var cardCornerRadius: Float = 50F
        set(value) {
            field = value
            postInvalidate()
        }

    var cardShadowColor: Int = 0
        set(value) {
            field = value
            postInvalidate()
        }

    var cardElevation: Int = 20
        set(value) {
            field = value
            postInvalidate()
        }

    var cardBackgroundColor: Int = 0
        set(value) {
            field = value
            postInvalidate()
        }

    init {
        super.addView(roundRectView, 0, LayoutParams(-2, -1))
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
        cardBackgroundColor =
            a.getColor(R.styleable.CardView_cardBackgroundColor, Color.TRANSPARENT)
        cardShadowColor = a.getColor(R.styleable.CardView_cardShadowColor, Color.GRAY)

        val backgroundColor = if (a.hasValue(R.styleable.CardView_cardBackgroundColor)) {
            a.getColorStateList(R.styleable.CardView_cardBackgroundColor)
        } else { // There isn't one set, so we'll compute one based on the theme
            val aa =
                context.obtainStyledAttributes(colorBackgroundAttr)
            val themeColorBackground = aa.getColor(0, 0)
            aa.recycle()
            // If the theme colorBackground is light, use our own light color, otherwise dark
            val hsv = FloatArray(3)
            Color.colorToHSV(themeColorBackground, hsv)
            ColorStateList.valueOf(
                if (hsv[2] > 0.5f) resources.getColor(R.color.cardview_light_background) else resources.getColor(
                    R.color.cardview_dark_background
                )
            )
        }

        a.recycle()

        roundRectView.setBackgroundDrawable(
            RoundRectDrawable(
                backgroundColor,
                cardCornerRadius
            )
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
        if (roundRectView.childCount >= 1) {
            throw IllegalStateException("Can't add more than 1 views to a CardView")
        }

        roundRectView.post {
            roundRectView.addView(child, 0, params)
        }
    }


    override fun setBackground(background: Drawable?) {
    }


    private inner class RoundRectView(context: Context) : FrameLayout(context) {

        init {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                clipToOutline = true
            }
            setBackgroundResource(R.color.cardview_shadow_start_color)
        }

        override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
            if (childCount >= 1) {
                throw IllegalStateException("Can't add more than 1 views to a CardView")
            }

            if (params is MarginLayoutParams) {

                val layoutParams =
                    layoutParams ?: LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT
                    )

                layoutParams.apply {
                    if (this is LayoutParams) {
                        this.leftMargin += params.leftMargin + cardElevation
                        this.topMargin += params.topMargin + cardElevation
                        this.rightMargin += params.rightMargin + cardElevation
                        this.bottomMargin += params.bottomMargin + cardElevation
                    }
                }

                this.layoutParams = layoutParams

                params.leftMargin = 0
                params.topMargin = 0
                params.rightMargin = 0
                params.bottomMargin = 0

                super.addView(child, 0, params)
            }

        }
    }

    @SuppressLint("NewApi")
    override fun dispatchDraw(canvas: Canvas) {

        if (layerType != View.LAYER_TYPE_SOFTWARE) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        val child = getChildAt(0)
        val rectF = RectF(
            child.left.toFloat() + cardElevation,
            child.top.toFloat() + cardElevation,
            child.right.toFloat() - cardElevation,
            child.bottom.toFloat() - cardElevation
        )

        backgroundDrawable.setBounds(child.left, child.top, child.right, child.bottom)
        backgroundDrawable.cornerRadius = cardCornerRadius
        if (Build.VERSION.SDK_INT == 21) {
            backgroundDrawable.colorFilter =
                PorterDuffColorFilter(cardBackgroundColor, PorterDuff.Mode.SRC_IN)
        } else {
            DrawableCompat.setTint(backgroundDrawable, cardBackgroundColor)
        }
        backgroundDrawable.draw(canvas)

        paint.color = cardShadowColor
        paint.maskFilter = BlurMaskFilter(cardElevation.toFloat(), BlurMaskFilter.Blur.OUTER)


//        canvas.drawRoundRect(rectF, cardCornerRadius, cardCornerRadius, paint)

//        super.dispatchDraw(canvas)

    }

}
