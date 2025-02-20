package ru.iandreyshev.cglab2_android.presentation.common

import android.content.res.Resources
import ru.iandreyshev.cglab2_android.R
import ru.iandreyshev.cglab2_android.domain.Element
import ru.iandreyshev.cglab2_android.domain.Element.*
import ru.iandreyshev.cglab2_android.domain.IElementNameProvider

class ResourcesNameProvider(
    private val resources: Resources
) : IElementNameProvider {

    override fun get(element: Element): String =
        resources.getString(when (element) {
            WATER -> R.string.element_water
            FIRE -> R.string.element_fire
            GROUND -> R.string.element_ground
            AIR -> R.string.element_air
            STEAM -> R.string.element_steam
            LAVA -> R.string.element_lava
            DUST -> R.string.element_dust
            POWDER -> R.string.element_powder
            EXPLOSION -> R.string.element_explosion
            SMOKE -> R.string.element_smoke
            ENERGY -> R.string.element_energy
            STONE -> R.string.element_stone
            STORM -> R.string.element_storm
            METAL -> R.string.element_metal
            ELECTRICITY -> R.string.element_electricity
            HYDROGEN -> R.string.element_hydrogen
            OXYGEN -> R.string.element_oxygen
            OZONE -> R.string.element_ozone
            DIRT -> R.string.element_dirt
            GEYSER -> R.string.element_geyser
            STEAM_BOILER -> R.string.element_steam_boiler
            PRESSURE -> R.string.element_pressure
            VOLCANO -> R.string.element_volcano
            EXPLOSIVE_GAS -> R.string.element_explosive_gas
            SWAMP -> R.string.element_swamp
            ALCOHOL -> R.string.element_alcohol
            MOLOTOV_COCKTAIL -> R.string.element_molotov_cocktail
            LIFE -> R.string.element_life
            BACTERIA -> R.string.element_bacteria
            VODKA -> R.string.element_vodka
        })

}
