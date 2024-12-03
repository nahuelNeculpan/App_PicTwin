/*
 * Copyright (c) 2024. Desarrollo de Soluciones Moviles, DISC.
 */

package cl.ucn.disc.dsm.pictwin.data.network

import cl.ucn.disc.dsm.pictwin.data.model.Persona
import cl.ucn.disc.dsm.pictwin.data.model.PicTwin
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * The API Service.
 */
interface ApiService {

    /**
     * Authenticate a user with the given credentials.
     */
    @FormUrlEncoded
    @POST("api/personas")
    suspend fun authenticate(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<Persona>


    /**
     * Retrieve the user with the given ULID.
     */
    @GET("api/personas/{ulid}/pictwins")
    suspend fun getPicTwins(@Path("ulid") ulid: String): Response<List<PicTwin>>
}