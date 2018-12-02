package com.wit.jasonfagerberg.nightsout.addDrink

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wit.jasonfagerberg.nightsout.R
import com.google.android.material.button.MaterialButton
import java.text.DecimalFormat

class ComplexDrinkHelper(val parent: AddDrinkFragment) {
    private lateinit var btnAddAnotherAlcoholSource: MaterialButton
    private lateinit var recyclerAlcoholSource: RecyclerView
    private val listAlcoholSources: ArrayList<AlcoholSource> = ArrayList()
    private lateinit var alcoholSourceAdapter: AddDrinkFragmentAlcoholSourceAdapter

    fun findViews() {
        btnAddAnotherAlcoholSource = parent.view!!.findViewById(R.id.btn_add_drink_add_alc_source)
        recyclerAlcoholSource = parent.view!!.findViewById(R.id.recycler_add_drink_alcohol_source_list)
        alcoholSourceAdapter = AddDrinkFragmentAlcoholSourceAdapter(parent.context!!, listAlcoholSources)

        recyclerAlcoholSource.adapter = alcoholSourceAdapter
        val linearLayoutManagerRecents = LinearLayoutManager(parent.context)
        linearLayoutManagerRecents.orientation = RecyclerView.VERTICAL
        recyclerAlcoholSource.layoutManager = linearLayoutManagerRecents

        btnAddAnotherAlcoholSource.setOnClickListener {
            addToAlcoholSourceList()
        }
    }

    fun addToAlcoholSourceList() {
        if (parent.isInputErrors()) return
        val abv = parent.mConverter.stringToDouble(parent.mEditAbv.text.toString())
        val amount = parent.mConverter.stringToDouble(parent.mEditAmount.text.toString())
        val measurement = parent.mSpinnerAmount.selectedItem.toString()

        val source = AlcoholSource(abv, amount, measurement)
        listAlcoholSources.add(source)
        alcoholSourceAdapter.notifyItemInserted(listAlcoholSources.size)
        parent.mEditAbv.setText("")
        parent.mEditAmount.setText("")
    }

    fun weightedAverageAbv(): Double {
        if (listAlcoholSources.isEmpty()) return Double.NaN
        var ave = 0.0
        val sum = sumAmount()
        for (alcSource in listAlcoholSources) {
            val weight = parent.mConverter.drinkVolumeToFluidOz(alcSource.amount, alcSource.measurement) / sum
            ave += alcSource.abv * weight
        }

        val twoDForm = DecimalFormat("#.##")
        return java.lang.Double.valueOf(twoDForm.format(ave))
    }

    fun sumAmount(): Double {
        if (listAlcoholSources.isEmpty()) return Double.NaN
        var sum = 0.0
        for (alcSource in listAlcoholSources) {
            sum += parent.mConverter.drinkVolumeToFluidOz(alcSource.amount, alcSource.measurement)
        }
        return sum
    }

    fun listIsEmpty(): Boolean {
        return listAlcoholSources.isEmpty()
    }

    inner class AlcoholSource(val abv: Double, val amount: Double, val measurement: String) {
        override fun equals(other: Any?): Boolean {
            val o = other as AlcoholSource
            return this.abv == o.abv && this.amount == o.amount && this.measurement == o.measurement
        }

        override fun hashCode(): Int {
            return abv.hashCode() + amount.hashCode() + measurement.hashCode()
        }
    }
}