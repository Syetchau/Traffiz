package com.singapore.trafficcamera.interfaces

import com.singapore.trafficcamera.models.CameraItem

interface CameraItemListener {
    fun onCameraItemSelected(cameraItem: CameraItem, position: Int)

    fun onCameraItemAddToFavourite(cameraItem: CameraItem, position: Int)

    fun onCameraItemRemoveFromFavourite(cameraItem: CameraItem, position: Int)
}