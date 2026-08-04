package com.wit.jasonfagerberg.nightsout.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.wit.jasonfagerberg.nightsout.R
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

class BacInfoDialog(
    context: Context
) {
    private var _bac: Double = 0.0
    private var _drinkingDuration: Double = 0.0
    private var _standardDrinksConsumed: Double = 0.0
    private lateinit var _converter: com.wit.jasonfagerberg.nightsout.utils.Converter
    private lateinit var _context: Context

    fun setParams(
        bac: Double,
        drinkingDuration: Double,
        standardDrinksConsumed: Double,
        converter: com.wit.jasonfagerberg.nightsout.utils.Converter,
        context: Context
    ) {
        _bac = bac
        _drinkingDuration = drinkingDuration
        _standardDrinksConsumed = standardDrinksConsumed
        _converter = converter
        _context = context
    }

    fun showBacInfoDialog() {
        val builder = AlertDialog.Builder(_context)
        val parent: ViewGroup? = null
        val dialogView = (_context as android.app.Activity).layoutInflater
                .inflate(R.layout.dialog_bac_info, parent, false)

        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()
        setupBacDeclineChart(dialog)

        dialog.findViewById<TextView>(R.id.text_bac_info_title).text = "BAC Level: " + String.format("%.3f", _bac)

        var hoursMin = _converter.decimalTimeToHoursAndMinuets(_drinkingDuration)
        var hoursMinStrings = _converter.hoursAndMinuetsToTwoDigitStrings(hoursMin)
        val durationString = "${hoursMinStrings.first} hours  ${hoursMinStrings.second} min"
        dialog.findViewById<TextView>(R.id.text_bac_info_duration).text = durationString

        val standardDrinksString = String.format("%.2f", _standardDrinksConsumed) + " drinks"
        dialog.findViewById<TextView>(R.id.text_bac_info_standard_drinks).text = standardDrinksString

        val hoursToSober = if ((_bac - 0.04) / 0.015 < 0) 0.0 else (_bac - 0.04) / 0.015
        hoursMin = _converter.decimalTimeToHoursAndMinuets(hoursToSober)
        hoursMinStrings = _converter.hoursAndMinuetsToTwoDigitStrings(hoursMin)
        val hoursToSoberString = "${hoursMinStrings.first} hours  ${hoursMinStrings.second} min"
        dialog.findViewById<TextView>(R.id.text_bac_info_time_to_sober).text = hoursToSoberString

        dialog.findViewById<ImageView>(R.id.btn_bac_info_dismiss).setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun setupBacDeclineChart(dialog: AlertDialog) {
        val graph = dialog.findViewById<GraphView>(R.id.graph_bac_info_declining_bac)
        graph.title = "BAC Decline Over Time"
        val points = ArrayList<DataPoint>()
        var projectedBac = _bac
        var elapsedTime = 0.0

        while (projectedBac > 0.0075) {
            points.add(DataPoint(elapsedTime, projectedBac))
            elapsedTime += .5
            projectedBac -= 0.0075
        }
        val series = LineGraphSeries(points.toTypedArray())
        series.setOnDataPointTapListener { _, dataPoint ->
            val pointBac = dataPoint.y.toString().substring(0, 4)
            val time = _converter.decimalTimeToTwoDigitStrings(dataPoint.x)
            (_context as android.app.Activity).let { ctx ->
                val toast = android.widget.Toast.makeText(ctx, "BAC after ${time.first} hours and ${time.second} minuets: $pointBac", android.widget.Toast.LENGTH_SHORT)
                toast.setGravity(android.view.Gravity.CENTER, 0, 450)
                toast.show()
            }
        }

        val soberLine = ArrayList<DataPoint>()
        if (points.size > 0) {
            soberLine.add(DataPoint(0.0, 0.04))
            soberLine.add(DataPoint(100.0, 0.04))
        }
        val soberLineSeries = LineGraphSeries(soberLine.toTypedArray())
        soberLineSeries.color = ContextCompat.getColor(_context, R.color.colorLightGreen)
        soberLineSeries.backgroundColor = ContextCompat.getColor(_context, R.color.colorLightGreen)
        soberLineSeries.isDrawBackground = true

        graph.addSeries(soberLineSeries)
        graph.addSeries(series)

        graph.gridLabelRenderer.labelVerticalWidth = 96

        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.isYAxisBoundsManual = true
        graph.viewport.setMaxY(_bac + .0008)
        graph.viewport.setMaxX(elapsedTime + .5)
    }
}
