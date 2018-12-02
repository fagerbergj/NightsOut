package com.wit.jasonfagerberg.nightsout.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

class BacInfoDialog(
    context: Context
) {
    private val mainActivity = context as MainActivity
    private val homeFragment = mainActivity.homeFragment
    private val converter = homeFragment.mConverter

    fun showBacInfoDialog() {
        val builder = android.app.AlertDialog.Builder(homeFragment.context)
        val parent: ViewGroup? = null
        val dialogView = mainActivity.layoutInflater
                .inflate(R.layout.dialog_bac_info, parent, false)

        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()
        setupBacDeclineChart(dialog)

        dialog.findViewById<ImageView>(R.id.btn_bac_info_dismiss).setOnClickListener {
            dialog.dismiss()
        }

        val bacInfoTitleString = "BAC Level: " + String.format("%.3f", homeFragment.bac)
        dialog.findViewById<TextView>(R.id.text_bac_info_title).text = bacInfoTitleString

        var hoursMin = converter.decimalTimeToHoursAndMinuets(homeFragment.drinkingDuration)
        var hoursMinStrings = converter.hoursAndMinuetsToTwoDigitStrings(hoursMin)
        val durationString = "${hoursMinStrings.first} hours  ${hoursMinStrings.second} min"
        dialog.findViewById<TextView>(R.id.text_bac_info_duration).text = durationString

        val standardDrinksString = String.format("%.2f", homeFragment.standardDrinksConsumed) + " drinks"
        dialog.findViewById<TextView>(R.id.text_bac_info_standard_drinks).text = standardDrinksString

        val hoursToSober = if ((homeFragment.bac - 0.04) / 0.015 < 0) 0.0 else (homeFragment.bac - 0.04) / 0.015
        hoursMin = converter.decimalTimeToHoursAndMinuets(hoursToSober)
        hoursMinStrings = converter.hoursAndMinuetsToTwoDigitStrings(hoursMin)
        val hoursToSoberString = "${hoursMinStrings.first} hours  ${hoursMinStrings.second} min"
        dialog.findViewById<TextView>(R.id.text_bac_info_time_to_sober).text = hoursToSoberString
    }

    private fun setupBacDeclineChart(dialog: AlertDialog) {
        val graph = dialog.findViewById<GraphView>(R.id.graph_bac_info_declining_bac)
        graph.title = "BAC Decline Over Time"
        val points = ArrayList<DataPoint>()
        var projectedBac = homeFragment.bac
        var elapsedTime = 0.0

        while (projectedBac > 0.0075) {
            points.add(DataPoint(elapsedTime, projectedBac))
            elapsedTime += .5
            projectedBac -= 0.0075
        }
        val series = LineGraphSeries<DataPoint>(points.toTypedArray())
        series.setOnDataPointTapListener { _, dataPoint ->
            val pointBac = dataPoint.y.toString().substring(0, 4)
            val time = converter.decimalTimeToTwoDigitStrings(dataPoint.x)
            mainActivity.showToast("BAC after ${time.first} hours and ${time.second} minuets: $pointBac")
        }

        val soberLine = ArrayList<DataPoint>()
        if (points.size > 0) {
            soberLine.add(DataPoint(0.0, 0.04))
            soberLine.add(DataPoint(100.0, 0.04))
        }
        val soberLineSeries = LineGraphSeries<DataPoint>(soberLine.toTypedArray())
        soberLineSeries.color = ContextCompat.getColor(homeFragment.context!!, R.color.colorLightGreen)
        soberLineSeries.backgroundColor = ContextCompat.getColor(homeFragment.context!!, R.color.colorLightGreen)
        soberLineSeries.isDrawBackground = true

        graph.addSeries(soberLineSeries)
        graph.addSeries(series)

        graph.gridLabelRenderer.labelVerticalWidth = 96

        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.isYAxisBoundsManual = true
        graph.viewport.setMaxY(homeFragment.bac + .0008)
        graph.viewport.setMaxX(elapsedTime + .5)
    }
}