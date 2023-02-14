package com.singapore.trafficcamera.interfaces

import com.singapore.trafficcamera.models.TrafficItem

interface SubscribeTrafficListener {
    fun onSubscribeSuccess(data: ArrayList<TrafficItem>)

    fun onSubscribeError(error: String)
}