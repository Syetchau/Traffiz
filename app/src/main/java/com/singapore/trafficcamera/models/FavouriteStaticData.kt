package com.singapore.trafficcamera.models

class FavouriteStaticData private constructor() {

    var favouriteList: MutableList<String> = ArrayList()

    companion object {
        @get:Synchronized
        var data: FavouriteStaticData? = null
            get() {
                if (field == null) {
                    field = FavouriteStaticData()
                }
                return field
            }
            private set
    }
}