package com.singapore.trafficcamera.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class ImageMetaData(): Serializable, Parcelable {
    var height: Int = 0
    var width: Int = 0
    var md5: String = ""

    constructor(parcel: Parcel) : this() {
        height = parcel.readInt()
        width = parcel.readInt()
        md5 = parcel.readString()!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(height)
        parcel.writeInt(width)
        parcel.writeString(md5)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImageMetaData> {
        override fun createFromParcel(parcel: Parcel): ImageMetaData {
            return ImageMetaData(parcel)
        }

        override fun newArray(size: Int): Array<ImageMetaData?> {
            return arrayOfNulls(size)
        }
    }
}