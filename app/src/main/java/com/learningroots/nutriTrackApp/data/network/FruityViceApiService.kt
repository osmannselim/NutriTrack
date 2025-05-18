package com.learningroots.nutriTrackApp.data.network

import com.learningroots.nutriTrackApp.data.model.FruityViceFruitResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface FruityViceApiService {
    @GET("api/fruit/{fruitname}")
    suspend fun getFruitByName(@Path("fruitname") fruitName: String): Response<FruityViceFruitResponse>
} 