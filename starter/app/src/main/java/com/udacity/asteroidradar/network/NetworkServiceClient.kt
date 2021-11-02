package com.udacity.asteroidradar.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.Constants
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiNASAService {

    @GET(Constants.ASTEROIDS)
    fun getAsteroidsAsync(
        @Query("api_key") api_key: String = Constants.API_KEY,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Deferred<String>

    @GET(Constants.PICTURE_OF_THE_DAY)
    fun getPictureOfDayAsync(@Query("api_key") api_key: String = Constants.API_KEY): Deferred<PictureOfDay>
}

object Network {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val nasa: ApiNASAService = retrofit.create(ApiNASAService::class.java)
}
