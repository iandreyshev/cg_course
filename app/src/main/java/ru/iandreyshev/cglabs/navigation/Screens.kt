package ru.iandreyshev.cglabs.navigation

import kotlinx.serialization.Serializable

@Serializable
object Menu

object Lab1 {
    @Serializable
    object Initials

    @Serializable
    object House

    @Serializable
    object BresenhamCircle

    @Serializable
    object Hangman
}

object Lab2 {
    @Serializable
    object ViewImages

    @Serializable
    object AlchemistryCraft

    @Serializable
    object AlchemistryList

    @Serializable
    object StoryEditor
}

object Lab3 {
    @Serializable
    object Guide

    @Serializable
    object Bezier

    @Serializable
    object Asteroids
}

object Lab4 {
    @Serializable
    object Figure
}
