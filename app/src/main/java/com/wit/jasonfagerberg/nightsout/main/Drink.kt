package com.wit.jasonfagerberg.nightsout.main

import java.util.*

class Drink(var id: UUID, var name: String, var abv: Double, var amount: Double,
            var measurement: String, var favorited: Boolean, var recent: Boolean,
            var modifiedTime: Long) {

    override fun toString(): String {
        return "Drink(id = $id, name='$name', abv=$abv, amount=$amount, measurement='$measurement')"
    }

    override fun equals(other: Any?): Boolean {
        return (other as Drink).name == this.name
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun isExactDrink(other: Drink): Boolean {
        return this.id == other.id
    }
}