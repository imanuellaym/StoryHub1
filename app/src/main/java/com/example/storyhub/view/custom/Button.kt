package com.example.storyhub.view.custom

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.storyhub.R

class MyButton : AppCompatButton {

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        setTextColor(ContextCompat.getColor(context, android.R.color.white))
        textSize = 14f
        gravity = Gravity.CENTER
    }

    private fun initialize() {
        setBackgroundColor(ContextCompat.getColor(context, if (isEnabled) R.color.primary_button else R.color.gray_button))
    }
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        setBackgroundColor(ContextCompat.getColor(context, if (enabled) R.color.primary_button else R.color.gray_button))
    }
}
