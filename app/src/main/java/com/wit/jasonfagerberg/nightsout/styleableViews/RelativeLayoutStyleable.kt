package com.wit.jasonfagerberg.nightsout.styleableViews

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.wit.jasonfagerberg.nightsout.R

class RelativeLayoutStyleable : RelativeLayout {
    constructor(context: Context) : super(context)

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet, defStyle: Int = R.attr.relativeLayoutViewStyle) : super(context, attrs, defStyle)
}