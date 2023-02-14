package com.singapore.trafficcamera.interfaces

import com.singapore.trafficcamera.models.TrafficItem

interface TrafficListener {
    fun onGetTrafficSuccess(data: ArrayList<TrafficItem>)

    fun onGetTrafficFailed(error: String)
}