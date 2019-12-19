package com.liang.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.graphics.drawable.DrawableCompat
import com.liang.cardview.R


/**
 * TODO: document your custom view class.
 */
class CardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

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
        setWillNotDraw(false)
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
        a.recycle()

    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (childCount >= 1) {
            throw IllegalStateException("Can't add more than 1 views to a CardView")
        }

        if (params is MarginLayoutParams) {

            val layoutParams =
                layoutParams ?: LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

            layoutParams.apply {
                if (this is LayoutParams) {
                    this.leftMargin += params.leftMargin
                    this.topMargin += params.topMargin
                    this.rightMargin += params.rightMargin
                    this.bottomMargin += params.bottomMargin
                }
            }

            this.layoutParams = layoutParams

            params.leftMargin = cardElevation
            params.topMargin = cardElevation
            params.rightMargin = cardElevation
            params.bottomMargin = cardElevation

        }

        super.addView(child, index, params)
    }

    override fun setBackground(background: Drawable?) {
    }

    @SuppressLint("NewApi")
    override fun dispatchDraw(canvas: Canvas) {

        if (layerType != View.LAYER_TYPE_SOFTWARE) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        val child = getChildAt(0)
        val left = child.left
        val top = child.top
        val right = child.right
        val bottom = child.bottom
        backgroundDrawable.setBounds(left, top, right, bottom)
        backgroundDrawable.cornerRadius = cardCornerRadius
        if (Build.VERSION.SDK_INT == 21) {
            backgroundDrawable.colorFilter =
                PorterDuffColorFilter(cardBackgroundColor, PorterDuff.Mode.SRC_IN)
        } else {
            DrawableCompat.setTint(backgroundDrawable, cardBackgroundColor)
        }
        backgroundDrawable.draw(canvas)

        super.dispatchDraw(canvas)

        paint.color = cardShadowColor
        paint.maskFilter = BlurMaskFilter(cardElevation.toFloat(), BlurMaskFilter.Blur.OUTER)

        canvas.drawRoundRect(
            left.toFloat(),
            top.toFloat(),
            right.toFloat(),
            bottom.toFloat(), cardCornerRadius, cardCornerRadius, paint
        )

    }

}
