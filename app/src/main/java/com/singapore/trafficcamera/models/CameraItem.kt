package com.singapore.trafficcamera.models

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import java.io.Serializable

class CameraItem() : Serializable, Parcelable, ClusterItem {
    var camera_id: String = ""
    var timestamp: String = ""
    var image: String = ""
    var location: Location?= null
    var image_metadata: ImageMetaData?= null

    constructor(parcel: Parcel) : this() {
        camera_id = parcel.readString()!!
        timestamp = parcel.readString()!!
        image = parcel.readString()!!
        location = parcel.readParcelable(Location::class.java.classLoader)
        image_metadata = parcel.readParcelable(ImageMetaData::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(camera_id)
        parcel.writeString(timestamp)
        parcel.writeString(image)
        parcel.writeParcelable(location, flags)
        parcel.writeParcelable(image_metadata, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CameraItem> {
        override fun createFromParcel(parcel: Parcel): CameraItem {
            return CameraItem(parcel)
        }

        override fun newArray(size: Int): Array<CameraItem?> {
            return arrayOfNulls(size)
        }
    }

    override fun getPosition(): LatLng {
        return LatLng(location?.latitude!!, location?.longitude!!)
    }

    override fun getTitle(): String {
        return camera_id
    }

    override fun getSnippet(): String {
        return image
    }
}