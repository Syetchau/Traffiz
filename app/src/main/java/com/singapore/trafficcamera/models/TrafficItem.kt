package com.singapore.trafficcamera.models

import java.io.Serializable

class TrafficItem: Serializable {
    var timestamp: String = ""
    var cameras: ArrayList<CameraItem> = ArrayList()
}