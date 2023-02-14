package com.singapore.trafficcamera.repository

import com.singapore.trafficcamera.api.Api
import javax.inject.Inject

class TrafficRepository @Inject constructor(private val api: Api) {

    suspend fun getTraffic(dateTime: String) = api.getTrafficData(dateTime = dateTime)

    fun listenTrafficData(dateTime: String) = api.listenTrafficData(dateTime = dateTime)
}