/*
 * Copyright (c) 2024. Desarrollo de Soluciones Moviles, DISC.
 */

package cl.ucn.disc.dsm.pictwin.data.model

data class Persona (
    val ulid: String,
    val email: String,
    val picTwins: List<PicTwin>,
)