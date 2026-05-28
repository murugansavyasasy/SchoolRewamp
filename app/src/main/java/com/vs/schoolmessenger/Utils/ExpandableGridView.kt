package com.vs.schoolmessenger.Utils

import android.content.Context
import android.util.AttributeSet
import android.widget.GridView

class ExpandableGridView(context: Context, attrs: AttributeSet?) : GridView(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val expandSpec = MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, expandSpec)
    }
}
