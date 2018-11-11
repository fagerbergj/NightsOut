package com.wit.jasonfagerberg.nightsout.addDrink.drinkSuggestion

import android.content.Context
import android.util.AttributeSet
import android.widget.AutoCompleteTextView

class DrinkSuggestionAutoCompleteView : AutoCompleteTextView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    // this is how to disable AutoCompleteTextView filter
    override fun performFiltering(text: CharSequence, keyCode: Int) {
        val filterText = ""
        super.performFiltering(filterText, keyCode)
    }
}