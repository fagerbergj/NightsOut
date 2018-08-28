package com.example.jasonfagerberg.nightsout

import java.util.*


class Drink(var image: ByteArray, var name: String, var aav: Double,
            var amount: Double, var measurement: String){
    override fun toString(): String {
        return "Drink(image=${Arrays.toString(image)}, name='$name', aav=$aav, amount=$amount, measurement='$measurement')"
    }
}