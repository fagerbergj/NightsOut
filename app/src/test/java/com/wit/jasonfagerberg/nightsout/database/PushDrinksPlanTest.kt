package com.wit.jasonfagerberg.nightsout.database

import com.wit.jasonfagerberg.nightsout.models.Drink
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

// pushDrinks() delegates to planPushDrinks() so its dedup logic (added when the per-drink
// isFavoritedInDB() lookups were batched into one query) is testable without a real DB.
class PushDrinksPlanTest {

    private fun drink(name: String, favorited: Boolean = false) =
        Drink(UUID.randomUUID(), name, favorited = favorited)

    @Test
    fun `session inserts and drink updates cover every current drink in order`() {
        val beer = drink("Beer")
        val wine = drink("Wine")

        val plan = planPushDrinks(listOf(beer, wine), emptyList(), emptySet())

        assertThat(plan.sessionInserts).containsExactly(
            beer.id.toString() to 0,
            wine.id.toString() to 1
        )
        assertThat(plan.drinkUpdates).containsExactly(beer, wine)
    }

    @Test
    fun `unfavoriting a current drink that was favorited queues exactly one delete`() {
        val beer = drink("Beer", favorited = false)

        val plan = planPushDrinks(listOf(beer), emptyList(), setOf("Beer"))

        assertThat(plan.favoritesToDelete).containsExactly("Beer")
        assertThat(plan.favoritesToInsert).isEmpty()
    }

    @Test
    fun `a drink already favorited is not deleted or re-inserted`() {
        val beer = drink("Beer", favorited = true)

        val plan = planPushDrinks(listOf(beer), listOf(beer), setOf("Beer"))

        assertThat(plan.favoritesToDelete).isEmpty()
        assertThat(plan.favoritesToInsert).isEmpty()
    }

    @Test
    fun `duplicate favorite names insert only once`() {
        val first = drink("Beer")
        val second = drink("Beer")

        val plan = planPushDrinks(emptyList(), listOf(first, second), emptySet())

        assertThat(plan.favoritesToInsert).containsExactly("Beer" to first.id.toString())
    }

    @Test
    fun `a drink removed from the session in this same call is still deleted once`() {
        // current has the drink twice under the old (already-favorited) name, unfavorited both times
        val beer1 = drink("Beer", favorited = false)
        val beer2 = drink("Beer", favorited = false)

        val plan = planPushDrinks(listOf(beer1, beer2), emptyList(), setOf("Beer"))

        assertThat(plan.favoritesToDelete).containsExactly("Beer")
    }
}
