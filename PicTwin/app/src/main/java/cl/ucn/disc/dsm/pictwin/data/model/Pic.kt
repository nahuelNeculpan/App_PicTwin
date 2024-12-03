/*
 * Copyright (c) 2024. Desarrollo de Soluciones Moviles, DISC.
 */

package cl.ucn.disc.dsm.pictwin.data.model

import java.time.Instant

data class Pic (
    val latitude: Double,
    val longitude: Double,
    val date: Instant,
    val photo: String,
)