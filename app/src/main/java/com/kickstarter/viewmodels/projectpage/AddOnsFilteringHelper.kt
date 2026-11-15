package com.kickstarter.viewmodels.projectpage

import com.kickstarter.models.Location
import com.kickstarter.models.Reward

/**
 * Helper for filtering and updating add-ons based on location and backed quantities.
 */
internal object AddOnsFilteringHelper {

    /**
     * List of available addOns, updated for those backed addOns with the Backed information
     * such as quantity backed.
     */
    fun getUpdatedList(
        addOns: List<Reward>,
        backedAddOns: List<Reward>,
        location: Location,
        currentSelection: MutableMap<Long, Int>
    ): List<Reward> {
        val holder = mutableMapOf<Long, Reward>()
        val locationId = location.id()

        val filteredAddOns = addOns.filter { reward ->
            when (reward.shippingPreference()) {
                "unrestricted", "none", "local" -> true
                else -> reward.shippingRules()?.any { it.location()?.id() == locationId } ?: false
            }
        }

        // Store filtered addOns into holder
        filteredAddOns.forEach {
            holder[it.id()] = it
        }

        // Take the backed AddOns, update with matching addOn ID with the quantity information
        backedAddOns.forEach { backedAddOn ->
            val match = holder[backedAddOn.id()]
            if (match != null) {
                val updated = match.toBuilder().quantity(backedAddOn.quantity()).build()
                holder[backedAddOn.id()] = updated
            }
            currentSelection[backedAddOn.id()] = backedAddOn.quantity() ?: 0
        }

        return holder.values.toList()
    }
}
