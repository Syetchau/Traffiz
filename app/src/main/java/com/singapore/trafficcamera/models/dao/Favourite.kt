package com.singapore.trafficcamera.models.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity("favourite")
class Favourite: Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var username: String = ""
    var favouriteCamera: String = ""
}