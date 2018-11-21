package com.wit.jasonfagerberg.nightsout.main

import java.util.UUID

class Drink(
    var id: UUID,
    var name: String,
    var abv: Double = 0.0,
    var amount: Double = 0.0,
    var measurement: String = "",
    var favorited: Boolean = false,
    var recent: Boolean = false,
    var modifiedTime: Long = 0
) {

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