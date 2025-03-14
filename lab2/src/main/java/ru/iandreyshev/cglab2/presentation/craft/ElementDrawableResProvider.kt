package ru.iandreyshev.cglab2.presentation.craft

import ru.iandreyshev.cglab2.R
import ru.iandreyshev.cglab2.domain.craft.Element
import ru.iandreyshev.cglab2.domain.craft.Element.*

class ElementDrawableResProvider {

    operator fun get(element: Element): Int = when (element) {
        FIRE -> R.drawable.ic_element_fire
        WATER -> R.drawable.ic_element_water
        GROUND -> R.drawable.ic_element_ground
        AIR -> R.drawable.ic_element_air
        STEAM -> R.drawable.ic_element_steam
        LAVA -> R.drawable.ic_element_lava
        DUST -> R.drawable.ic_element_dust
        POWDER -> R.drawable.ic_element_powder
        EXPLOSION -> R.drawable.ic_element_explosion
        SMOKE -> R.drawable.ic_element_smoke
        ENERGY -> R.drawable.ic_element_energy
        STONE -> R.drawable.ic_element_stone
        STORM -> R.drawable.ic_element_storm
        METAL -> R.drawable.ic_element_metal
        ELECTRICITY -> R.drawable.ic_element_electricity
        HYDROGEN -> R.drawable.ic_element_hydrogen
        OXYGEN -> R.drawable.ic_element_oxygen
        OZONE -> R.drawable.ic_element_ozone
        DIRT -> R.drawable.ic_element_dirt
        GEYSER -> R.drawable.ic_element_geyser
        STEAM_BOILER -> R.drawable.ic_element_steam_boiler
        PRESSURE -> R.drawable.ic_element_pressure
        VOLCANO -> R.drawable.ic_element_volcano
        EXPLOSIVE_GAS -> R.drawable.ic_element_explosive_gas
        SWAMP -> R.drawable.ic_element_swamp
        ALCOHOL -> R.drawable.ic_element_alcohol
        MOLOTOV_COCKTAIL -> R.drawable.ic_element_molotov_cocktail
        LIFE -> R.drawable.ic_element_life
        BACTERIA -> R.drawable.ic_element_bacteria
        VODKA -> R.drawable.ic_element_vodka
    }

}
