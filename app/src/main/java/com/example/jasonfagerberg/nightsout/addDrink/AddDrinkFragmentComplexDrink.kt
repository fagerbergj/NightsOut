package com.example.jasonfagerberg.nightsout.addDrink

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jasonfagerberg.nightsout.R
import com.google.android.material.button.MaterialButton

class AddDrinkFragmentComplexDrink(val parent: AddDrinkFragment) {
    private val btnAddAnotherAlcoholSource: MaterialButton = parent.view!!.findViewById(R.id.btn_add_drink_add_alc_source)
    private val recyclerAlcoholSource: RecyclerView = parent.view!!.findViewById(R.id.recycler_add_drink_alcohol_source_list)
    private val listAlcoholSources: ArrayList<AlcoholSource> = ArrayList()
    private val alcoholSourceAdapter = AddDrinkFragmentAlcoholSourceAdapter(parent.context!!, listAlcoholSources)

    init {
        recyclerAlcoholSource.adapter = alcoholSourceAdapter
        val linearLayoutManagerRecents = LinearLayoutManager(parent.context)
        linearLayoutManagerRecents.orientation = LinearLayoutManager.HORIZONTAL
        recyclerAlcoholSource.layoutManager = linearLayoutManagerRecents

        btnAddAnotherAlcoholSource.setOnClickListener { _ ->
            if (!parent.isInputErrors()){
                val abv = parent.mConverter.stringToDouble(parent.mEditAbv.text.toString())
                val amount = parent.mConverter.stringToDouble(parent.mEditAmount.text.toString())
                val measurement = parent.mSpinnerAmount.selectedItem.toString()
                addToAlcoholSourceList(abv, amount, measurement)
            }
        }
    }

    private fun addToAlcoholSourceList(abv: Double, amount: Double, measurement: String){
        val source = AlcoholSource(abv, amount, measurement)
        listAlcoholSources.add(source)
        alcoholSourceAdapter.notifyItemInserted(listAlcoholSources.size)
        parent.mEditAbv.setText("")
        parent.mEditAmount.setText("")
    }

    inner class AlcoholSource(val abv: Double, val amount: Double, val measurement: String)
}