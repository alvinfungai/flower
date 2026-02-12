package com.alvinfungai.flower.ui.common

import android.content.res.Resources
import android.util.TypedValue

/**
 * User defined for View-based fragments
 * Usage: 24.dp
 */
val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

// Also useful for floats
val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )