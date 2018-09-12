package com.example.jasonfagerberg.nightsout



class Drink(var name: String, var abv: Double, var amount: Double,
            var measurement: String, var favorited: Boolean, var recent: Boolean){
    override fun toString(): String {
        return "Drink(name='$name', abv=$abv, amount=$amount, measurement='$measurement')"
    }
}