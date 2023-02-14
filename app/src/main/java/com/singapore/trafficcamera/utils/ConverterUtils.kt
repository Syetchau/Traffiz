package com.singapore.trafficcamera.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.singapore.trafficcamera.models.ImageMetaData
import com.singapore.trafficcamera.models.Location

class ConverterUtils {
    @TypeConverter
    fun listToJsonString(value: List<String>?): String = Gson().toJson(value)

    @TypeConverter
    fun jsonStringToList(value: String) = Gson().fromJson(value, Array<String>::class.java).toList()
}