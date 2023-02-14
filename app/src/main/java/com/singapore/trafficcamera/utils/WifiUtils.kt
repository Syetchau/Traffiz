package com.singapore.trafficcamera.utils

class WifiUtils private constructor(){

    var isConnectedInternet: Boolean = false

    companion object {
        @get:Synchronized
        var wifi: WifiUtils? = null
            get() {
                if (field == null) {
                    field = WifiUtils()
                }
                return field
            }
            private set
    }
}