package ru.iandreyshev.cglab2_android.domain.craft

enum class Element {
    WATER,
    FIRE,
    GROUND,
    AIR,
    STEAM,
    LAVA,
    DUST,
    POWDER,
    EXPLOSION,
    SMOKE,
    ENERGY,
    STONE,
    STORM,
    METAL,
    ELECTRICITY,
    HYDROGEN,
    OXYGEN,
    OZONE,
    DIRT,
    GEYSER,
    STEAM_BOILER,
    PRESSURE,
    VOLCANO,
    EXPLOSIVE_GAS,
    SWAMP,
    ALCOHOL,
    MOLOTOV_COCKTAIL,
    LIFE,
    BACTERIA,
    VODKA;
}

infix fun Element.combineWith(other: Element): Element? {
    return null
}
