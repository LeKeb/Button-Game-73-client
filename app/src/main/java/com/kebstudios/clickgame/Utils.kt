package com.kebstudios.clickgame

import android.content.res.Resources
import android.graphics.Rect
import android.view.View

object Utils {
    fun convertDpToPx(dp: Float): Float {
        val metrics = Resources.getSystem().displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return Math.round(px).toFloat()
    }

    fun bounds(view: View): Rect {
        val l = IntArray(2) {0}
        view.getLocationOnScreen(l)
        return Rect(l[0], l[1], l[0] + view.width, l[1] + view.height)
    }
}