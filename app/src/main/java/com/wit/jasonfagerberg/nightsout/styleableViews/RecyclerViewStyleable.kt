package com.wit.jasonfagerberg.nightsout.styleableViews

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.wit.jasonfagerberg.nightsout.R


class RecyclerViewStyleable : RecyclerView {
    constructor(context: Context) : super(context)

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet, defStyle: Int = R.attr.recyclerViewStyle) : super(context, attrs, defStyle)
}