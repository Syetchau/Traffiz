package com.singapore.trafficcamera.tools

object Constants {
    //RETROFIT TIME OUT SECONDS
    //ADJUSTABLE
    const val CONNECTION_TIMEOUT: Long = 8
    const val READ_TIMEOUT: Long = 8
    const val WRITE_TIMEOUT: Long = 8

    //subscribe api constants
    const val SUBSCRIBE_INITIAL_DELAY: Long = 60
    const val SUBSCRIBE_INTERVAL: Long = 60

    //delay favourite timing
    const val DELAY_SEC: Long = 250

    //image height, width
    const val IMAGE_WIDTH: Int = 375
    const val IMAGE_HEIGHT: Int = 375

    const val HEALTHY = "healthy"
    const val API_KEY = "{GOOGLE_API_KEY}"

    const val TAG_MAP = "MAP"
    const val TAG_CAMERA = "CAMERA"

    //act as key to parse to fragment
    const val CAMERA_ITEM_LIST = "CAMERA_ITEM_LIST"
    const val CAMERA_ITEM = "CAMERA_ITEM"
    const val CAMERA_ITEM_POS = "CAMERA_ITEM_POS"
}
