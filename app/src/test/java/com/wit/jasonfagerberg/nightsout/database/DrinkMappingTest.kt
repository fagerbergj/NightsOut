package com.wit.jasonfagerberg.nightsout.database

import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DrinkMappingTest {

    private val id = UUID.randomUUID()
    private val entity = DrinkEntity(
        id.toString(), "Beer", 5.0, 12.0, "oz", 1, 123456789L, null)

    @Test
    fun `entity to drink maps fields and parses the uuid`() {
        val drink = entity.toDrink(favorited = true)
        assertThat(drink.id).isEqualTo(id)
        assertThat(drink.name).isEqualTo("Beer")
        assertThat(drink.abv).isEqualTo(5.0)
        assertThat(drink.amount).isEqualTo(12.0)
        assertThat(drink.measurement).isEqualTo("oz")
        assertThat(drink.favorited).isTrue()
        assertThat(drink.recent).isTrue()
        assertThat(drink.modifiedTime).isEqualTo(123456789L)
    }

    @Test
    fun `entity to drink defaults null columns like the old cursor reads`() {
        val sparse = DrinkEntity(id.toString(), null, null, null, null, null, null, null)
        val drink = sparse.toDrink(favorited = false)
        assertThat(drink.name).isEmpty()
        assertThat(drink.abv).isEqualTo(0.0)
        assertThat(drink.amount).isEqualTo(0.0)
        assertThat(drink.measurement).isEmpty()
        assertThat(drink.recent).isFalse()
        assertThat(drink.modifiedTime).isEqualTo(0)
    }

    @Test
    fun `drink to entity leaves dontSuggest null like the old insert`() {
        val entity = entity.toDrink(favorited = false).toEntity()
        assertThat(entity.id).isEqualTo(id.toString())
        assertThat(entity.recent).isEqualTo(1)
        assertThat(entity.dontSuggest).isNull()
    }

    @Test
    fun `recent flag maps from the entity int`() {
        assertThat(entity.copy(recent = 0).toDrink(favorited = false).recent).isFalse()
        assertThat(entity.copy(recent = 1).toDrink(favorited = false).recent).isTrue()
        assertThat(entity.copy(recent = null).toDrink(favorited = false).recent).isFalse()
    }
}
