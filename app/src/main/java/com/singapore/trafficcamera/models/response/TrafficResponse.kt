package com.singapore.trafficcamera.models.response

import com.singapore.trafficcamera.models.ApiInfo
import com.singapore.trafficcamera.models.TrafficItem
import java.io.Serializable

class TrafficResponse: Serializable {
    var api_info: ApiInfo?= null
    var items: ArrayList<TrafficItem> = ArrayList()
    var message: String = ""
}