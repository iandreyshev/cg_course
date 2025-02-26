package ru.iandreyshev.cglab2.domain.craft

val ELEMENTS_MAP = mapOf(
    Element.FIRE + Element.WATER creates Element.STEAM,
    Element.FIRE + Element.GROUND creates Element.LAVA,
    Element.AIR + Element.GROUND creates Element.DUST,
    Element.FIRE + Element.DUST creates Element.POWDER,
    Element.POWDER + Element.FIRE creates listOf(Element.EXPLOSION, Element.SMOKE),
    Element.AIR + Element.FIRE creates Element.ENERGY,
    Element.LAVA + Element.WATER creates listOf(Element.STEAM, Element.STONE),
    Element.AIR + Element.ENERGY creates Element.STORM,
    Element.FIRE + Element.STONE creates Element.METAL,
    Element.METAL + Element.ENERGY creates Element.ELECTRICITY,
    Element.ELECTRICITY + Element.WATER creates listOf(Element.HYDROGEN, Element.OXYGEN),
    Element.ELECTRICITY + Element.OXYGEN creates Element.OZONE,
    Element.DUST + Element.WATER creates Element.DIRT,
    Element.STEAM + Element.GROUND creates Element.GEYSER,
    Element.STEAM + Element.METAL creates Element.STEAM_BOILER,
    Element.STEAM_BOILER + Element.STEAM creates Element.PRESSURE,
    Element.LAVA + Element.PRESSURE creates Element.VOLCANO,
    Element.HYDROGEN + Element.OXYGEN creates Element.EXPLOSIVE_GAS,
    Element.WATER + Element.GROUND creates Element.SWAMP,
    Element.FIRE + Element.WATER creates Element.ALCOHOL,
    Element.ALCOHOL + Element.FIRE creates Element.MOLOTOV_COCKTAIL,
    Element.SWAMP + Element.ENERGY creates Element.LIFE,
    Element.LIFE + Element.SWAMP creates Element.BACTERIA,
    Element.ALCOHOL + Element.WATER creates Element.VODKA
)

val DEFAULT_ELEMENTS = setOf(Element.FIRE, Element.AIR, Element.WATER, Element.GROUND)

operator fun Element.plus(other: Element) = setOf(this, other)

infix fun Set<Element>.creates(result: List<Element>) =
    this to result

infix fun Set<Element>.creates(result: Element) =
    this to listOf(result)
