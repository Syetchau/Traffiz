package com.singapore.trafficcamera.api

import com.singapore.trafficcamera.models.response.TrafficResponse
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {

    /***
     * GET TRAFFIC
     */
    @GET(ApiConstants.getTraffic)
    suspend fun getTrafficData(@Query(ApiConstants.DATE_TIME) dateTime: String): Response<TrafficResponse>

    /***
     * SUBSCRIBE TO TRAFFIC
     */
    @GET(ApiConstants.getTraffic)
    fun listenTrafficData(@Query(ApiConstants.DATE_TIME) dateTime: String): Observable<TrafficResponse>
}